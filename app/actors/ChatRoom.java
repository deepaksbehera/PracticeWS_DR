
package actors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actors.OnlineActor.Cron;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import models.AppUser;
import models.Message;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import utils.Constants;

public class ChatRoom extends UntypedActor {

	// Default room.
	static ActorRef defaultRoom = Akka.system().actorOf(Props.create(ChatRoom.class));

	/**
	 * Join the default room.
	 */
	static {
		Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
				Duration.create(10,TimeUnit.SECONDS),     //Frequency seconds
				defaultRoom, 
				new Cron(),
				Akka.system().dispatcher(),
				null
				);
	}
	public static void join(final Long msgById, final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) throws Exception{
		// Send the Join message to the room
		final String result = (String)Await.result(Patterns.ask(defaultRoom,new Join(msgById, out),1000),Duration.create(1,TimeUnit.SECONDS));
		if("OK".equals(result)) {
			// For each event received on the socket,
			in.onMessage(new Callback<JsonNode>() {
				@Override
				public void invoke(final JsonNode event) {
					Logger.info("on message is called");					
					String messageContent = event.get("content").asText(); 
	            	String msgType = event.get("msgType").asText().trim();
	            	Long msgById = event.get("msgById").asLong();
	            	Long msgToId = event.get("msgToId").asLong();
	            	Logger.info("event---"+messageContent+">>>"+msgType+">>"+msgToId+ ">>"+msgById);
	            		defaultRoom.tell(new Talk(msgById, messageContent,msgToId,msgType),null);
				} 
			});
			// When the socket is closed.
			in.onClose(new Callback0() {
				@Override
				public void invoke() {
					// Send a Quit message to the room.
					defaultRoom.tell(new Quit(msgById), null);
				}
			});
	  }else 
		{
			// Cannot connect, create a Json error.
			final ObjectNode error = Json.newObject();
			error.put("error", result);
			// Send the error to the socket.
			out.write(error);
		}
	}
	
	// Inner Classes
	public static class Join {
		Long msgById;
		final WebSocket.Out<JsonNode> channel;
		public Join(Long msgById1, final WebSocket.Out<JsonNode> channel1) {
			msgById = msgById1;
			channel = channel1;
		}
	}
	public static class Talk {
		Long msgById;
		String content;
		Long msgToId;
		String msgType;
		/*public Talk(final Long username1, final String text1,final Long username2,final String role1) {*/
			public Talk(Long msgById1,  String content1 ,Long msgToId1,String msgType1) {
			Logger.info("talk is called");
			msgById = msgById1;
			content = content1;
			msgToId = msgToId1;
			msgType = msgType1;
		}
	}
	public static class Quit {
		Long msgById;
		public Quit(Long msgById1) {
			msgById = msgById1;
			Logger.info("quit is called");
		}
	}
	
	private static List<WebSocket.Out<JsonNode>> connections = new ArrayList<WebSocket.Out<JsonNode>>();
	public static Map<Long, WebSocket.Out<JsonNode>> individualConnectionMap = new HashMap<Long, WebSocket.Out<JsonNode>>();
	public static Set<Long> onlineAppusers = new HashSet<Long>();
	public static Map<Long, WebSocket.Out<JsonNode>> groupConnectionMap = new HashMap<Long, WebSocket.Out<JsonNode>>();
	   
	@Override
	public void onReceive(final Object message) throws Exception {
		Logger.info("Cron is Running");
		if(message instanceof Join) {
			// Received a Join message
			final Join join = (Join)message;
			// Check if this username is free.
			if(individualConnectionMap.containsKey(join.msgById)) {
				getSender().tell("OK",getSelf());
			} else {
				individualConnectionMap.put(join.msgById, join.channel);
				getSender().tell("OK",getSelf());
			}

		} else if(message instanceof Talk)  {
			// Received a Talk message
			final Talk talk = (Talk)message;
			if(talk.msgType.equals(Constants.DIRECT_MESSAGE)){
				notifyIndividual(talk.content,talk.msgToId,talk.msgById);
			}
			if(talk.msgType.equals(Constants.GROUP_MESSAGE)){
				//notifyIndividual(talk.content,talk.msgToId,talk.msgById);
				
			}
		} else if(message instanceof Quit)  {
			// Received a Quit message
			final Quit quit = (Quit)message;
			individualConnectionMap.remove(quit.msgById);
			//	notifyAll("quit", quit.userId, "has left the room");
		} else if(message instanceof Cron)  {
			runCron();
		}  else {
			unhandled(message);
		}
	}
	
	  // Iterate connection list and write incoming message
    public static void notifyAll(JsonNode event){
        for (WebSocket.Out<JsonNode> out : connections) {
        	//Logger.info("notify all is called----"+message);
        	Logger.info("notify all is called----");
        	//out.write(message);
        }
    }
    
	public static void notifyIndividual(String content, Long toId, Long byId){
		Logger.info("notify Individual is called----");
			Message message = new Message();
			message.messgae = content.trim();
			message.sendOn = new Date();
			message.sendTo = AppUser.find.byId(toId);
			message.sendBy = AppUser.find.byId(byId);
			message.isMessagePersonal = Boolean.TRUE;
			message.save();
			if(individualConnectionMap.containsKey(toId)){
				//To user
				WebSocket.Out<JsonNode> out  = individualConnectionMap.get(toId);
				String messageDiv = views.html.messageTemplate.render(message, false).toString();
				final ObjectNode returnEevent = Json.newObject();
				//returnEevent.put("messageKind", "reply");
				returnEevent.put("messageContent", messageDiv);
				out.write(returnEevent);
			}
			if(individualConnectionMap.containsKey(byId)){
				//From User
				WebSocket.Out<JsonNode> outReverse  = individualConnectionMap.get(byId);
				String messageDivRev = views.html.messageTemplate.render(message, true).toString();
				
				final ObjectNode returnEeventRev = Json.newObject();
				//returnEeventRev.put("messageKind", "reply");
				returnEeventRev.put("messageContent", messageDivRev);
				outReverse.write(returnEeventRev);
			}
	    }

		// Members of this room.
	public void runCron(){
		for(final WebSocket.Out<JsonNode> channel: individualConnectionMap.values()) {
			final ObjectNode event = Json.newObject();
			event.put("kind", "cron");
			event.put("message", "Cron to keep chat connection alive");
			channel.write(event);
		}
	}
	
	public static Date getDateWithoutTime(Date date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    return cal.getTime();
	}

	public static Date getYesterdayDate(Date date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	
	public static List<Message> getMessages(Boolean isMsgPersonal, AppUser loginUser, AppUser requestForUser){
		List<Message> msgList = new LinkedList<Message>();
		if(isMsgPersonal){
			msgList = Message.find.where().or(
					Expr.and(Expr.eq("sendBy", loginUser), Expr.eq("sendTo", requestForUser)), 
					Expr.and(Expr.eq("sendBy", requestForUser), Expr.eq("sendTo", loginUser))).orderBy("sendOn").findList();
		}else{
			
		}
		return msgList;
	}
	/*public void notifyTo(final Message message){
	final WebSocket.Out<JsonNode> channel = members.get(message.sendTo.id);
	//String returnMessage = views.html.patient.returnMessage.render(message).toString();
	play.Logger.info("toUser not available (webscoket not created)");
	// message.messageTo.id=null it means to user not available(isviewed=flase)
	if(channel!=null){
		//message.messageTo.id!=null means touser is available (must check active clientID)
		final ObjectNode event = Json.newObject();
		event.put("messageId",message.id);
		event.put("kind", "message");
		event.put("direction", "messageTo");
		//event.put("fromUserId",String.valueOf(message.messageBy.id));
	//	event.put("toUserId",String.valueOf(message.messageTo.id));
		//event.put("user",AppUser.find.byId(message.messageBy.id).userId);
		//event.put("role",String.valueOf(message.role.toString()));
		//event.put("message", messagePage);
		//event.put("messageBody",message.description);
		channel.write(event);
	}
//	String returnMessage1 = views.html.patient.returnMessage.render(message).toString();
	//play.Logger.debug("myid:"+message.messageBy.id);
	final WebSocket.Out<JsonNode> mychannel = members.get(message.sendTo.id);
	if(mychannel!=null){
		final ObjectNode event = Json.newObject();
		event.put("messageId",message.id);
		event.put("kind", "message");
		event.put("direction", "messageBy");
		//event.put("fromUserId",String.valueOf(message.messageBy.id)); //this i think not required
		//event.put("user",AppUser.find.byId(message.messageBy.id).userId);
		//event.put("role",String.valueOf(message.role.toString()));
		//event.put("message", messagePage);
		mychannel.write(event);
	}
}
public void notifyAll(final Message message){
	// Send a Json event to all members
	//String returnMessage = views.html.patient.returnMessage.render(message).toString();
	
        for(WebSocket.Out<JsonNode> channel: members.values()) {
        	Logger.info("each channel group message"+channel);
        	final ObjectNode event = Json.newObject();
			event.put("messageId",message.id);
			event.put("kind", "message");
			event.put("direction", "messageTo");
			event.put("fromUserId",String.valueOf(message.messageBy.id));
			event.put("groupId",String.valueOf(message.groups.id));
		//	event.put("groupName",Groups.find.byId((message.groups.id)).name);
		//	event.put("toUserId",String.valueOf(message.messageTo.id));
		//	event.put("user",AppUser.find.byId(message.messageTo.id).userId);
			event.put("role",String.valueOf(message.role.toString()));
			event.put("message", messagePage);
			event.put("messageBody",message.description);
            
            ArrayNode m = event.putArray("members");
            for(Long u: members.keySet()) {
                m.add(u);
            }
            channel.write(event);
        }//for
}*/	
private Date getDateBeforeMessageSaved(Long userId, Long toUserId,String role){
		
		Date previousConversationDateWithoutTime=null;
		/*if(role != null && "USER".equalsIgnoreCase(role.toString())){
		  Query<Message> fromMessageQuery = Message.find.where().eq("messageBy.id", userId).eq("messageTo.id", toUserId).eq("role",Role.USER).order("createdOn desc").setMaxRows(1);
		  Query<Message> toMessageQuery = Message.find.where().eq("messageBy.id", toUserId).eq("messageTo.id", userId).eq("role",Role.USER).order("createdOn desc").setMaxRows(1);
		
		  
		  Message fromMessage = fromMessageQuery!=null ? fromMessageQuery.findUnique():null;
		  Message toMessage =  toMessageQuery!=null ? toMessageQuery.findUnique():null;
		  Logger.info("fromMessage------------->"+fromMessage);
		  Logger.info("toMessage--------------->"+toMessage);
		  if(fromMessage!=null || toMessage!=null ) {//means no conversation message between two usres
			Date previousConversationDateWithTime = null;
				Date fromDate = null;
				Date toDate = null;
				if (fromMessage != null) {
					// Logger.info("fromMessage createdOn"+fromMessage.createdOn);
					toDate = fromMessage.createdOn;
				}
				if (toMessage != null) {
					// Logger.info("fromMessage createdOn"+toMessage.createdOn);
					fromDate = toMessage.createdOn;
				}
				
				if (fromDate != null && toDate!=null ) {
					if(fromDate.before(toDate)){
						previousConversationDateWithTime = toDate;
					}else{
						previousConversationDateWithTime = fromDate;
					}
				}else{
					if (fromDate != null ) {
						previousConversationDateWithTime = fromDate;
					}
					if (toDate != null ) {
						previousConversationDateWithTime = toDate;
					}
				}

				

				Logger.info("previousConversationDateWithTime------------------------------->"+ previousConversationDateWithTime);
				previousConversationDateWithoutTime = getDateWithoutTime(previousConversationDateWithTime);
			  return previousConversationDateWithoutTime;
		  }else{
			return previousConversationDateWithoutTime=getDateWithoutTime(getYesterdayDate(new Date()));  
		  }
		
		}else 	if(role != null && "GROUP".equalsIgnoreCase(role.toString())){
			 Query<Message> messageQuery = Message.find.where().eq("groups.id",toUserId).eq("role",Role.GROUP).order("createdOn desc").setMaxRows(1);
			 Message message = messageQuery.findUnique();
			// Logger.info("GROUP messge"+message);;
			 if(message!=null){ //means no conversation message between two Users
			  Date previousConversationDateWithTime = message.createdOn;
			//  Logger.info(previousConversationDateWithTime+"before convert without time");
			  previousConversationDateWithoutTime =getDateWithoutTime(previousConversationDateWithTime) ;
			//  Logger.info(previousConversationDateWithoutTime+"previousConversationDateWithoutTime in Group");
			 }else{
				 return previousConversationDateWithoutTime=getDateWithoutTime(getYesterdayDate(new Date()));  
			  }
		}*/
		return previousConversationDateWithoutTime;
	
	}	
	private String getResultPage(Date beforeDate,Message lastMessage) {
		String returnHtmlPage="";
	/*	Date todayDateWithoutTime=getDateWithoutTime(new Date()) ;
		//Logger.info(beforeDate+"----**************************8----"+todayDateWithoutTime);
		// compare previousConversationDate and today date .
		if(beforeDate.before(todayDateWithoutTime)){
			//two dates are different means create map with message showMessagePage 
			final ArrayList<Message> messageSet = new ArrayList<Message>();
			LinkedHashMap<Date, ArrayList<Message>> dateWiseMessageMap = new LinkedHashMap<Date, ArrayList<Message>>();
			messageSet.add(lastMessage);
			try {
				dateWiseMessageMap.put(new SimpleDateFormat("yyyy-MM-dd").parse(lastMessage.createdOn.toString()),messageSet);
				//Logger.info(dateWiseMessageMap+"getDateWiseMessageMap:ChatRoom");
				returnHtmlPage = views.html.showMessage.render(dateWiseMessageMap,1l).toString();
			} catch (ParseException e) {
				Logger.info("date not parse :getDateWiseMessageMap:ChatRoom");
				e.printStackTrace();
			}
		}else{
			returnHtmlPage = views.html.patient.returnMessage.render(lastMessage).toString();
		}*/
		return returnHtmlPage;
	}

	 public void saveGroupNotification(Message message){
	    	/*List<AppUser> appUserList  = message.groups.userList;
	    	for(AppUser appUser : appUserList){
	    		if(!(appUser.id == message.sendBy.id)){
	    		Notification notification = new Notification();
	    		notification.messageBy = message.messageBy;
	    		notification.messageTo = appUser;
	    		notification.toGroup = 	Groups.find.byId(message.groups.id);
	    		
	    		notification.isViewed = false;
	    		notification.role = Role.GROUP;
	    		notification.message=message;
	    		notification.save();
	    	}
	    	}*/
	    }
	 /*Message message1 = new Message();
		message1.messgae = talk.content;
		
		message1.sendTo = AppUser.find.byId(talk.msgToId);
		message1.save();
		Logger.info(message1+"<<<<<<<<<after msg saved");*/
//		notifyTo(message1);
			/*Messages message1 = new Messages();
			message1.description = talk.text;
			message1.description.trim();
			message1.messageBy = AppUser.find.byId(talk.userId);
		
			
			if(talk.role.equalsIgnoreCase(String.valueOf(Role.USER))){
				message1.messageTo = AppUser.find.byId(talk.toUserId);
				message1.role = Role.USER;
			 }else if(talk.role.equalsIgnoreCase(String.valueOf(Role.GROUP))){
				 message1.groups = Groups.find.byId(talk.toUserId);
				 message1.role = Role.GROUP;
			}
			
			//before we must write Line()
			Date date=getDateBeforeMessageSaved(talk.userId,talk.toUserId,talk.role);
			Logger.info("message saved");
			message1.save();
			play.Logger.info("message sucessfully stored  message id :"+message1);
			String messagePage=getResultPage(date,message1);
			if(message1.role !=null && "USER".equalsIgnoreCase(message1.role.toString())){
			 notifyTo(message1,messagePage);
			}
			else if("GROUP".equalsIgnoreCase(message1.role.toString())){
				notifyAll(message1,messagePage);
				saveGroupNotification(message1);
			}

		} else if(message instanceof Quit)  {
			// Received a Quit message
			final Quit quit = (Quit)message;
			members.remove(quit.userId);
			//	notifyAll("quit", quit.userId, "has left the room");
		} else if(message instanceof Cron)  {
			runCron();
		}  else {
			unhandled(message);
		}*/
	 // Send a Json event to all members
/*    public void notifyAll(String kind, String user, String text) {
        for(WebSocket.Out<JsonNode> channel:members.values()) {
            ObjectNode event = Json.newObject();
            
            event.put("kind", kind);
            event.put("user", AppUser.find.byId(Long.parseLong(user)).name);
            event.put("message",text);
     
            ArrayNode m = event.putArray("members");
            for(Long member: members.keySet()) {
                m.add(member);
            }
            channel.write(event);
        }
    }*/
		
		/*public void runCron(){
			for(final WebSocket.Out<JsonNode> channel: members.values()) {
				final ObjectNode event = Json.newObject();
				event.put("kind", "cron");
				event.put("message", "Cron to keep chat connection alive");
				channel.write(event);
			}
		}*/
		
	/*	@Override
		public void onReceive(final Object message) throws Exception {
			Logger.info(message+"<<<<<<<<<<<<<on receive is called");
			if(message instanceof Join) {
				// Received a Join message
				final Join join = (Join)message;
				// Check if this username is free.
				if(members.containsKey(join.msgById)) {
					getSender().tell("OK",getSelf());
				} else {
					members.put(join.msgById, join.channel);
					getSender().tell("OK",getSelf());
				}

			} else if(message instanceof Talk)  {
				// Received a Talk message
				final Talk talk = (Talk)message;
				
				notifyIndividual(talk.content,talk.msgToId,talk.msgById);
				
				} else if(message instanceof Quit)  {
					// Received a Quit message
					final Quit quit = (Quit)message;
					members.remove(quit.userId);
					//	notifyAll("quit", quit.userId, "has left the room");
				} else if(message instanceof Cron)  {
					runCron();
				}  else {
					unhandled(message);
				}
		}*/


	
}


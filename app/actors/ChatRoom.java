
package actors;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actors.OnlineActor.Cron;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import models.AppUser;
import models.Messages;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;


/**
 * A chat room is an Actor.
 */


public class ChatRoom extends UntypedActor 
{

	// Default room.
	static ActorRef defaultRoom = Akka.system().actorOf(Props.create(ChatRoom.class));

	/*// Create a Robot, just for fun.
	 static {
        new Robot(defaultRoom);
    }*/

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

	public static void join(final Long userId, final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) throws Exception{
		
		Logger.info("join() method "); 
		// Send the Join message to the room
		final String result = (String)Await.result(Patterns.ask(defaultRoom,new Join(userId, out),1000),Duration.create(1,TimeUnit.SECONDS));
		if("OK".equals(result)) {
			// For each event received on the socket,
			Logger.info("on message is called");
			in.onMessage(new Callback<JsonNode>() {
				@Override
				public void invoke(final JsonNode event) {
					Logger.info("on message is called");					
					Logger.debug(event.get("messageText")+"<<<<<<<<<<<");
					//Logger.info("invoke method"+event.get("messageText"));
					// Send a Talk message to the room.
					//Logger.info("fsdfsdfsdfsd ==============="+event.get("text").asText()+"======"+Long.parseLong(event.get("toUserId").asText())+"========"+event.get("role").asText());
					//defaultRoom.tell(new Talk(userId, event.get("messageText").asText(),Long.parseLong(event.get("toUserId").asText()),event.get("role").asText()), null);
					defaultRoom.tell(new Talk(userId, event.get("messageText").asText()),null);
				} 
			});

			// When the socket is closed.
			in.onClose(new Callback0() {
				@Override
				public void invoke() {
					// Send a Quit message to the room.
					defaultRoom.tell(new Quit(userId), null);
				}
			});

		}
		else 
		{
			// Cannot connect, create a Json error.
			final ObjectNode error = Json.newObject();
			error.put("error", result);

			// Send the error to the socket.
			out.write(error);

		}

	}

	/*@Override
	public void onReceive(Object arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}*/

	// -- Messages
	public static class Join {
		final Long userId;
		final WebSocket.Out<JsonNode> channel;
		public Join(final Long username1, final WebSocket.Out<JsonNode> channel1) {
			userId = username1;
			channel = channel1;
		}

	}
	public static class Talk {

		final Long userId;
		final String text;
		//final Long toUserId;
		//final String role;

		/*public Talk(final Long username1, final String text1,final Long username2,final String role1) {*/
			public Talk(final Long username1, final String text1) {
				Logger.info("talk is called");
			userId = username1;
			text = text1;
			//toUserId = username2;
			//role = role1;
		}
	}

	public static class Quit {
		final Long userId;
		public Quit(final Long username1) {
			userId = username1;
			Logger.info("quit is called");
		}

	}
		// Members of this room.
	   public static Map<Long, WebSocket.Out<JsonNode>> members = new HashMap<Long, WebSocket.Out<JsonNode>>();
	   public static Set<Long> onlineAppusers = new HashSet<Long>();
	
	public void runCron(){
		for(final WebSocket.Out<JsonNode> channel: members.values()) {
			final ObjectNode event = Json.newObject();
			event.put("kind", "cron");
			event.put("message", "Cron to keep chat connection alive");
			channel.write(event);
		}
	}
	public void notifyTo(final Messages message){
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
	public void notifyAll(final Messages message){
		// Send a Json event to all members
		//String returnMessage = views.html.patient.returnMessage.render(message).toString();
		
	        for(WebSocket.Out<JsonNode> channel: members.values()) {
	        	Logger.info("each channel group message"+channel);
	        	final ObjectNode event = Json.newObject();
				/*event.put("messageId",message.id);
				event.put("kind", "message");
				event.put("direction", "messageTo");
				event.put("fromUserId",String.valueOf(message.messageBy.id));
				event.put("groupId",String.valueOf(message.groups.id));
			//	event.put("groupName",Groups.find.byId((message.groups.id)).name);
			//	event.put("toUserId",String.valueOf(message.messageTo.id));
			//	event.put("user",AppUser.find.byId(message.messageTo.id).userId);
				event.put("role",String.valueOf(message.role.toString()));
				event.put("message", messagePage);
				event.put("messageBody",message.description);*/
	            
	            ArrayNode m = event.putArray("members");
	            for(Long u: members.keySet()) {
	                m.add(u);
	            }
	            channel.write(event);
	        }//for
	}

	
	 // Send a Json event to all members
    public void notifyAll(String kind, String user, String text) {
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
    }



	@Override
	public void onReceive(final Object message) throws Exception {
		Logger.info(message+"<<<<<<<<<<<<<on receive is called");
		if(message instanceof Join) {

			// Received a Join message
			final Join join = (Join)message;

			// Check if this username is free.
			if(members.containsKey(join.userId)) {
				getSender().tell("OK",getSelf());
			} else {
				members.put(join.userId, join.channel);
				getSender().tell("OK",getSelf());
			}

		} else if(message instanceof Talk)  {
			// Received a Talk message
			final Talk talk = (Talk)message;
			Messages message1 = new Messages();
			message1.messgae = talk.text;
			message1.sendTo = AppUser.find.byId(talk.userId);
			message1.save();
			Logger.info(message1+"<<<<<<<<<after msg saved");
			notifyTo(message1);
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

		}  else {
			unhandled(message);
		}
	}

	
    public void saveGroupNotification(Messages message){
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

	
	private String getResultPage(Date beforeDate,Messages lastMessage) {
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
	
	
	
	}



package actors;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import models.AppUser;
import models.GroupChannel;
import models.Messages;
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
				Duration.create(21,TimeUnit.SECONDS),     //Frequency seconds
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
	
	public static Map<Long, WebSocket.Out<JsonNode>> individualConnectionMap = new HashMap<Long, WebSocket.Out<JsonNode>>();
	public static Set<Long> onlineAppusers = new HashSet<Long>();
	  public static Map<Long, List<Long>> groupConnectionMap = ChatRoom.getGroupMap();
	    
	    public static Map<Long, List<Long>> getGroupMap(){
	    	Map<Long, List<Long>> groupConnectionMap = new HashMap<Long, List<Long>>();
	    	GroupChannel.find.all().stream().forEach(group -> {
	    		List<Long> appUserIdList = group.appUserList.stream().map(appUser -> appUser.id).collect( Collectors.toList() );
	    		groupConnectionMap.put(group.id, appUserIdList);
	    	});
	    	return groupConnectionMap;
	    }
	   
	@Override
	public void onReceive(final Object message) throws Exception {
		Logger.info("on recive is called");
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
				Logger.info("message type is direct");
				notifyIndividual(talk.content,talk.msgToId,talk.msgById);
			}
			if(talk.msgType.equals(Constants.GROUP_MESSAGE)){
				Logger.info("message type is group");
				notifyGroupMembers(talk.content,talk.msgToId,talk.msgById);
				
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
    public static void notifyGroupMembers(String content, Long toGroupId, Long byId){
    	GroupChannel group = GroupChannel.find.byId(toGroupId);
    	AppUser loginUser = AppUser.find.byId(byId);
    	Logger.info("notify All is called----"+group.name+" By User : "+loginUser.name);
    	Messages message = new Messages();
		message.messgae = content.trim();
		message.sendOn = new Date();
		message.sendBy = loginUser;
		message.isMessagePersonal = Boolean.FALSE;
		message.groupChannel = group;
		message.save();
    	
		if(groupConnectionMap.containsKey(toGroupId)){
			Logger.info("Group Available");
			groupConnectionMap.get(toGroupId).stream().forEach(groupMemberId -> {
				if(individualConnectionMap.containsKey(groupMemberId)){
					if(groupMemberId.equals(loginUser.id)){
						//Notify to messageBy user
						WebSocket.Out<JsonNode> outReverse  = individualConnectionMap.get(groupMemberId);
						String messageDivRev = views.html.messageTemplate.render(message, true).toString();
						
						final ObjectNode returnEeventRev = Json.newObject();
						//returnEeventRev.put("messageKind", "reply");
						returnEeventRev.put("toId", toGroupId);
						returnEeventRev.put("byId", byId);
						returnEeventRev.put("messageKind", "myMsg");
						returnEeventRev.put("messageType", Constants.GROUP_MESSAGE);
						returnEeventRev.put("messageContent", messageDivRev);
						outReverse.write(returnEeventRev);
					}else{
						//To other user
						WebSocket.Out<JsonNode> out  = individualConnectionMap.get(groupMemberId);
						String messageDiv = views.html.messageTemplate.render(message, false).toString();
						
						final ObjectNode returnEevent = Json.newObject();
						//returnEevent.put("messageKind", "reply");
						returnEevent.put("toId", toGroupId);
						returnEevent.put("byId", byId);
						returnEevent.put("messageKind", "othersMsg");
						returnEevent.put("messageType", Constants.GROUP_MESSAGE);
						returnEevent.put("messageContent", messageDiv);
						out.write(returnEevent);
					}
				}
			});
		}
    }
	/*public static void notifyIndividual(String content, Long toId, Long byId){
		Logger.info("notify Individual is called----");
			Messages message = new Messages();
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
	    }*/
		public static void notifyIndividual(String content, Long toId, Long byId){
			Logger.info("notify Individual is called----");
			Messages message = new Messages();
			message.messgae = content.trim();
			message.sendOn = new Date();
			message.sendTo = AppUser.find.byId(toId);
			message.sendBy = AppUser.find.byId(byId);
			message.isMessagePersonal = Boolean.TRUE;
			message.save();
			
			if(individualConnectionMap.containsKey(toId)){
				//To other user
				WebSocket.Out<JsonNode> out  = individualConnectionMap.get(toId);
				String messageDiv = views.html.messageTemplate.render(message, false).toString();
				
				final ObjectNode returnEevent = Json.newObject();
				returnEevent.put("messageKind", "othersMsg");
				returnEevent.put("toId", toId);
				returnEevent.put("byId", byId);
				returnEevent.put("messageType", Constants.DIRECT_MESSAGE);
				returnEevent.put("messageContent", messageDiv);
				out.write(returnEevent);
			}
			if(individualConnectionMap.containsKey(byId)){
				//Notify to messageBy user
				WebSocket.Out<JsonNode> outReverse  = individualConnectionMap.get(byId);
				String messageDivRev = views.html.messageTemplate.render(message, true).toString();
				
				final ObjectNode returnEeventRev = Json.newObject();
				returnEeventRev.put("messageKind", "myMsg");
				returnEeventRev.put("toId", toId);
				returnEeventRev.put("byId", byId);
				returnEeventRev.put("messageType", Constants.DIRECT_MESSAGE);
				returnEeventRev.put("messageContent", messageDivRev);
				outReverse.write(returnEeventRev);
				
			}
	    }

		// Members of this room.
	public void runCron(){
		for(final WebSocket.Out<JsonNode> channel: individualConnectionMap.values()) {
			Logger.info("Cron");
			final ObjectNode event = Json.newObject();
			event.put("messageType", Constants.CRON_MESSAGE);
			event.put("messageKind", "cron");
			event.put("messageContent", "Cron to keep chat connection alive");
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
	
	public static List<Messages> getMessages(Boolean isMsgPersonal, AppUser loginUser, AppUser requestForUser){
		List<Messages> msgList = new LinkedList<Messages>();
		if(isMsgPersonal){
			msgList = Messages.find.where().or(
					Expr.and(Expr.eq("sendBy", loginUser), Expr.eq("sendTo", requestForUser)), 
					Expr.and(Expr.eq("sendBy", requestForUser), Expr.eq("sendTo", loginUser))).orderBy("sendOn").findList();
		}else{
			
		}
		return msgList;
	}
	
	public static class Cron{

	}
	
}


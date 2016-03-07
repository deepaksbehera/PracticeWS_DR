package actors;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.ManyToOne;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.AppUser;
import models.GroupChannel;
import models.MessageNotification;
import models.Messages;
import play.Logger;
import play.libs.Json;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.WebSocket;
import utils.Constants;

public class ChatRoom {

	/**
	 * Websocket and utility methods
	 */
	
    // collect all websockets here
   // private static List<WebSocket.Out<JsonNode>> connections = new ArrayList<WebSocket.Out<JsonNode>>();
    
    public static Map<Long, WebSocket.Out<JsonNode>> onlineUserConnectionMap = new HashMap<Long, WebSocket.Out<JsonNode>>();
    public static Map<Long, List<Long>> groupConnectionMap = ChatRoom.getGroupMap();
    
    public static Map<Long, List<Long>> getGroupMap(){
    	Map<Long, List<Long>> groupConnectionMap = new HashMap<Long, List<Long>>();
    	GroupChannel.find.all().stream().forEach(group -> {
    		List<Long> appUserIdList = group.appUserList.stream().map(appUser -> appUser.id).collect( Collectors.toList() );
    		groupConnectionMap.put(group.id, appUserIdList);
    	});
    	return groupConnectionMap;
    }
    
    public static void start(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, Long appUserId){
    	
        //connections.add(out);
    	onlineUserConnectionMap.put(appUserId, out);
        
        in.onMessage(new Callback<JsonNode>(){
            public void invoke(JsonNode event){
            	//Logger.info("event---"+event.toString());
            	String messageContent = event.get("content").asText().trim(); 
            	String msgType = event.get("msgType").asText().trim();
            	Long msgById = event.get("msgById").asLong();
            	Long msgToId = event.get("msgToId").asLong();
            	
            	Logger.info("event---"+messageContent+">>>"+msgType+">>"+msgToId+ ">>"+msgById);
            	if(msgType.equals(Constants.DIRECT_MESSAGE)){
            		ChatRoom.notifyIndividual(messageContent, msgToId, msgById);
            	}else{
            		ChatRoom.notifyGroupMembers(messageContent, msgToId, msgById);
            	}
            }
        });
        
        in.onClose(new Callback0(){
            public void invoke(){
            	Logger.info("A connection closed");
            	onlineUserConnectionMap.remove(appUserId);
              // Messages.notifyAll("A connection closed");
            	
            }
        });
    }
    // Iterate connection list and write incoming message
    public static void notifyGroupMembers(String content, Long toGroupId, Long byId){
    	GroupChannel group = GroupChannel.find.byId(toGroupId);
    	AppUser loginUser = AppUser.find.byId(byId);
    	Logger.info("notify All is called----"+group.name+" By User : "+loginUser.name);
    	Messages message = new Messages();
		message.setEncodedMessage(content.trim());
		message.sendOn = new Date();
		message.sendBy = loginUser;
		message.isMessagePersonal = Boolean.FALSE;
		message.groupChannel = group;
		message.save();
    	
		if(groupConnectionMap.containsKey(toGroupId)){
			Logger.info("Group Available");
			groupConnectionMap.get(toGroupId).stream().forEach(groupMemberId -> {
				if(onlineUserConnectionMap.containsKey(groupMemberId)){
					if(groupMemberId.equals(loginUser.id)){
						//Notify to messageBy user
						WebSocket.Out<JsonNode> outReverse  = onlineUserConnectionMap.get(groupMemberId);
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
						WebSocket.Out<JsonNode> out  = onlineUserConnectionMap.get(groupMemberId);
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
				}else{
					MessageNotification.createGroupNotification(AppUser.find.byId(groupMemberId), group);
				}
			});
		}else{
			Logger.debug("Group is not available ");
		}
    }
    
    public static void notifyIndividual(String content, Long toId, Long byId){
		Logger.info("notify Individual is called----");
		final AppUser sendTo = AppUser.find.byId(toId);
		final AppUser sendBy = AppUser.find.byId(byId);
		Messages message = new Messages();
		message.setEncodedMessage(content.trim());
		message.sendOn = new Date();
		message.sendTo = sendTo;
		message.sendBy = sendBy;
		message.isMessagePersonal = Boolean.TRUE;
		message.save();
		
		if(onlineUserConnectionMap.containsKey(toId)){
			//To other user
			WebSocket.Out<JsonNode> out  = onlineUserConnectionMap.get(toId);
			String messageDiv = views.html.messageTemplate.render(message, false).toString();
			
			final ObjectNode returnEevent = Json.newObject();
			returnEevent.put("messageKind", "othersMsg");
			returnEevent.put("toId", toId);
			returnEevent.put("byId", byId);
			returnEevent.put("messageType", Constants.DIRECT_MESSAGE);
			returnEevent.put("messageContent", messageDiv);
			out.write(returnEevent);
		}else{
			MessageNotification.createPersonalNotification(sendTo, sendBy);
		}
		if(onlineUserConnectionMap.containsKey(byId)){
			//Notify to messageBy user
			WebSocket.Out<JsonNode> outReverse  = onlineUserConnectionMap.get(byId);
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
}

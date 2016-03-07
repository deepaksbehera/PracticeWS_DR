package controllers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import actors.ChatRoom;
import models.AppUser;
import models.GroupChannel;
import models.MessageNotification;
import models.Messages;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.Constants;

public class WebSocketController extends Controller {
	

    public Result loadChatWSJs(final Long appUserId) {
    	Logger.info("ws js is loaded");
        return ok(views.js.chatws.render(appUserId));
    }
    
    // Websocket interface
    public WebSocket<JsonNode> wsInterface(Long appUserId){
    	Logger.info("WS iNTERFACE : "+AppUser.find.byId(appUserId).name);
        return new WebSocket<JsonNode>(){
            
            // called when websocket handshake is done
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
            	Logger.info("websocket handshake is done");
            	ChatRoom.start(in, out, appUserId);
            }
        };   
    } 
    
    public Result getMessages(String type, Long id){
    	List<Messages> msgList = new LinkedList<Messages>();
    	AppUser loginUser  = LoginController.getLoggedInUser();
    	if(type.trim().equals(Constants.DIRECT_MESSAGE)){
    		msgList = Messages.getPersonalMessages(loginUser, AppUser.find.byId(id));
    	}else{
    		msgList = Messages.getGroupMessages(loginUser, GroupChannel.find.byId(id));
    	}
    	Map<Long,String> messageMap = new LinkedHashMap<Long,String>();
    	msgList.stream().forEach(message -> {
    		if(message.sendBy.id.equals(loginUser.id)){
    			messageMap.put(message.id, views.html.messageTemplate.render(message, true).toString());
    		}else{
    			messageMap.put(message.id, views.html.messageTemplate.render(message, false).toString());
    		}
    	});
    	return ok(Json.toJson(messageMap));
    }
    
    public Result makeAllMessagesAsSeen(String type, Long id){
    	List<MessageNotification> notificationList = new LinkedList<MessageNotification>();
    	AppUser loginUser  = LoginController.getLoggedInUser();
    	
    	if(type.trim().equals(Constants.DIRECT_MESSAGE)){
    		notificationList = MessageNotification.getPersonalUnSeenMessages(loginUser, AppUser.find.byId(id));
    	}else{
    		notificationList = MessageNotification.getGroupUnSeenMessages(loginUser, GroupChannel.find.byId(id));
    	}
    	notificationList.forEach(notification -> {
    		notification.isSeen = Boolean.TRUE;
    		notification.update();
    	});
    	return ok("");
    }
    
    public Result createMessageNotification(final String msgType, final Long msgToId , final Long msgById){
    	if(msgType.equals(Constants.DIRECT_MESSAGE)){
    		MessageNotification.createPersonalNotification(AppUser.find.byId(msgToId), AppUser.find.byId(msgById));
    	}else{
    		MessageNotification.createGroupNotification(AppUser.find.byId(msgToId), GroupChannel.find.byId(msgById));
    	}
    	return ok("");
    }
	
	
}

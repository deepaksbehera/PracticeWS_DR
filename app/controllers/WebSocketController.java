package controllers;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import actors.ChatRoom;
import models.AppUser;
import models.GroupChannel;
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
    
	 public WebSocket<JsonNode> wsInterface(final Long loginAppUserId) {
		 Logger.info("---entring chatRoom method -"+loginAppUserId);
		// Logger.info(" client ID from Bean "+messageBean.clientId);
		// Long toAppUserId=Long.parseLong(messageBean.clientId);
	        return new WebSocket<JsonNode>() {
	            // Called when the Websocket Handshake is done.
	            @Override
	            public void onReady(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out){

	                // Join the chat room.
	                try {
	                	Logger.info("---join method calling -"+loginAppUserId+"==========");
	                    ChatRoom.join(loginAppUserId,in,out);
	                   

	                } catch (final Exception ex) {
	                    ex.printStackTrace();
	                }
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
	
}

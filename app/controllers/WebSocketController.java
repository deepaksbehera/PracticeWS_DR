package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import actors.ChatRoom;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

public class WebSocketController extends Controller {
	
	public Result loadChatWSJs(final Long appUserId) {
    	Logger.info("ws js is loaded");
        return ok(views.js.chatws.render(appUserId));
    }
    
   /* // Websocket interface
    public WebSocket<String> wsInterface(Long appUserId){
        return new WebSocket<String>(){
            
            // called when websocket handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out){
            	Logger.info("websocket handshake is done");
            	Messages.start(in, out);
            }
        };   
    }*/   
    
    
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
	
}

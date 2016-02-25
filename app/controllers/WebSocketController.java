package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import models.AppUser;
import models.Message;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

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
            	Message.start(in, out, appUserId);
            }
        };   
    }   
	
	
}

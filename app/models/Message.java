package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.LoginController;
import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.WebSocket;
import utils.Constants;

@Entity
public class Message extends BaseEntity{
	
	public String messgae;
	
	public Date sendOn;
	
	@ManyToOne
	public AppUser sendTo;
	
	@ManyToOne
	public AppUser sendBy;
	
	public Boolean isMessagePersonal = Boolean.TRUE;
	
	public Boolean isseen = Boolean.FALSE;
	
	@ManyToOne
	public GroupChannel groupChannel;
	
	public static Model.Finder<Long, Message> find = new Model.Finder<Long, Message>(Message.class);
	
	
	/**
	 * Websocket and utility methods
	 */
	
    // collect all websockets here
    private static List<WebSocket.Out<JsonNode>> connections = new ArrayList<WebSocket.Out<JsonNode>>();
    
    public static Map<Long, WebSocket.Out<JsonNode>> individualConnectionMap = new HashMap<Long, WebSocket.Out<JsonNode>>();
    public static Map<Long, List<Long>> groupConnectionMap = new HashMap<Long, List<Long>>();
    
    public static void start(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, Long appUserId){
    	
        connections.add(out);
        individualConnectionMap.put(appUserId, out);
        
        in.onMessage(new Callback<JsonNode>(){
            public void invoke(JsonNode event){
            	//Logger.info("event---"+event.toString());
            	String messageContent = event.get("content").asText().trim(); 
            	String msgType = event.get("msgType").asText().trim();
            	Long msgById = event.get("msgById").asLong();
            	Long msgToId = event.get("msgToId").asLong();
            	
            	Logger.info("event---"+messageContent+">>>"+msgType+">>"+msgToId+ ">>"+msgById);
            	if(msgType.equals(Constants.DIRECT_MESSAGE)){
            		Message.notifyIndividual(messageContent, msgToId, msgById);
            	}else{
            		Message.notifyAll(event);
            	}
            }
        });
        
        in.onClose(new Callback0(){
            public void invoke(){
            	Logger.info("A connection closed");
              // Messages.notifyAll("A connection closed");
            }
        });
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
    
	
}

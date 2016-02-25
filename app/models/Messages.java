package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.WebSocket;

@Entity
public class Messages extends BaseEntity{
	
	
	public String messgae;
	
	@ManyToOne
	public AppUser sendTo;
	
	@ManyToOne
	public AppUser sendBy;
	
	public Boolean isMessagePersonal = Boolean.TRUE;
	
	@ManyToOne
	public GroupChannel groupChannel;
	
	public static Model.Finder<Long, Messages> find = new Model.Finder<Long, Messages>(Messages.class);
	
	
	
    // collect all websockets here
    private static List<WebSocket.Out<String>> connections = new ArrayList<WebSocket.Out<String>>();
    
    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out){
        
        connections.add(out);
        
        in.onMessage(new Callback<String>(){
            public void invoke(String event){
            	Logger.info("event---"+event);
            	
            	
            	Messages.notifyAll(event);
            }
        });
        
        in.onClose(new Callback0(){
            public void invoke(){
            	Logger.info("A connection closed");
                Messages.notifyAll("A connection closed");
            }
        });
    }
    // Iterate connection list and write incoming message
    public static void notifyAll(String message){
        for (WebSocket.Out<String> out : connections) {
        	Logger.info("notify all is called----"+message);
            out.write(message);
        }
    }
	
	
    /*// collect all websockets here
    private static List<WebSocket.Out<JsonNode>> connections = new ArrayList<WebSocket.Out<JsonNode>>();
    
    public static Map<Long, WebSocket.Out<JsonNode>> individualConnectionMap = new HashMap<Long, WebSocket.Out<JsonNode>>();
    
    public static void start(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
        
        connections.add(out);
        //individualConnectionMap.put(appUserId, out);
        
        in.onMessage(new Callback<JsonNode>(){
            public void invoke(JsonNode event){
            	//Logger.info("event---"+event);
            	Messages.notifyAll(event);
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
        	Logger.info("notify all is called----"+event);
        	Logger.info("notify all is called----");
        	out.write(event);
        }
    }
	*/
}

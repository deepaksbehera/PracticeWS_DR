package actors;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class OnlineActor  extends UntypedActor{

	// Default room.
	static ActorRef defaultRoom = Akka.system().actorOf(Props.create(OnlineActor.class));

	static {

		Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
				Duration.create(10, TimeUnit.SECONDS),     //Frequency seconds
				defaultRoom, 
				new Cron(),
				Akka.system().dispatcher(),
				null
				);
	}
	/**
	 * Join the default room.
	 */


	public static void join(final Long userId, final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) throws Exception{


		// Send the Join message to the room
		final String result = (String)Await.result(Patterns.ask(defaultRoom,new Join(userId, out), 1000), Duration.create(1, TimeUnit.SECONDS));
		Logger.info(result+"=====");
		if("OK".equals(result)) {

			// For each event received on the socket,
			in.onMessage(new Callback<JsonNode>() {

				@Override
				public void invoke(final JsonNode event) {
					//  Logger.info(event + "=====");
					// Send a Talk message to the room.

					if(event.get("type").asText().equals("notification")){
						defaultRoom.tell(new SocketNotification(userId, event.get("talkToId").asLong(),event.get("text").asText(),event.get("type").asText(),event.get("url").asText()), null);
					}	

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

	// Members of this room.
	Map<Long, WebSocket.Out<JsonNode>> members = new HashMap<Long, WebSocket.Out<JsonNode>>();

	@Override
	public void onReceive(final Object message) throws Exception {
		Logger.info(message+"msg");
		if(message instanceof Join) {

			// Received a Join message
			final Join join = (Join)message;

			// Check if this username is free.
			if(members.containsKey(join.userId)) {
				getSender().tell("This username is already used", getSelf());
			} else {
				members.put(join.userId, join.channel);
				Logger.info(join.userId+"--");
				//notifyAll("join", join.userId, "has entered the room");
				getSender().tell("OK", getSelf());
			}

		} else if(message instanceof Talk)  {

			// Received a Talk message
			final Talk talk = (Talk)message;
			//TODO
			//notifiyToAppUser(talk);
		}else if(message instanceof SocketNotification)  {

			// Received a Talk message
			final SocketNotification talk = (SocketNotification)message;
			//TODO
			notifiyToAppUser(talk);
		} else if(message instanceof Quit)  {

			// Received a Quit message
			final Quit quit = (Quit)message;

			members.remove(quit.userId);


			notifyAll("quit", quit.userId, "has left the room");

		} else if(message instanceof Cron)  {
			runCron();
		} 
		else {
			unhandled(message);
		}

	}





	public void notifiyToAppUser(final SocketNotification notification){

		Logger.debug("Exectuing from method call");

		Logger.debug("notified to id:"+notification.talkToId);
		if(members.containsKey(notification.talkToId)){
			final Out<JsonNode> channel = members.get(notification.talkToId);
			Logger.debug("comming to the action");
			final ObjectNode event = Json.newObject();
			event.put("kind",notification.type);
			event.put("user", notification.userId);
			//event.put("fromUser", AppUser.find.byId(notification.userId).);
			event.put("message", notification.text);
			event.put("url", notification.url);
			channel.write(event);
		}

	}


	// Send a Json event to all members
	public void notifyAll(final String kind, final Long user, final String text) {

		for(final WebSocket.Out<JsonNode> channel: members.values()) {
			final ObjectNode event = Json.newObject();
			event.put("kind", kind);
			event.put("user", user);
			event.put("message", text);

			final ArrayNode m = event.putArray("members");
			for(final Long u: members.keySet()) {
				m.add(u);
			}

			channel.write(event);
		}
	}

	public void runCron(){

		for(final WebSocket.Out<JsonNode> channel: members.values()) {
			final ObjectNode event = Json.newObject();
			event.put("kind", "cron");
			event.put("message", "Cron to keep connection alive");
			channel.write(event);
		}
	}
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
		final Long talkToId;
		final String text;
		final String type;

		public Talk(final Long username1,final Long user2,final String text1,final String type2) {
			userId = username1;
			text = text1;
			talkToId = user2;
			type = type2;
		}

	}

	public static class Quit {

		final Long userId;

		public Quit(final Long username1) {
			userId = username1;
		}

	}

	public static class SocketNotification{

		final Long userId;
		final Long talkToId;
		final String text;
		final String type;
		final String url;
		public SocketNotification(final Long username1,final Long user2,final String text1,final String type2,final String url2) {
			userId = username1;
			text = text1;
			talkToId = user2;
			type = type2;
			url = url2;
		}

	}
	public static class Cron{

	}


}


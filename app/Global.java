import java.util.concurrent.TimeUnit;

import actors.ChatRoom;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Promise;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import scala.concurrent.duration.Duration;

public class Global extends GlobalSettings{

	@Override
	public Promise<Result> onBadRequest(RequestHeader arg0, String arg1) {
		return super.onBadRequest(arg0, arg1);
	}

	@Override
	public Promise<Result> onError(RequestHeader arg0, Throwable arg1) {
		return super.onError(arg0, arg1);
	}

	@Override
	public Promise<Result> onHandlerNotFound(RequestHeader arg0) {
		return super.onHandlerNotFound(arg0);
	}

	@Override
	public void onStart(Application application) {
		final ActorRef defaultChatRoom = Akka.system().actorOf(Props.create(ChatRoom.class));
		
		Akka.system().scheduler().schedule( Duration.create(0,TimeUnit.MILLISECONDS), 
											Duration.create(10, TimeUnit.SECONDS), 
											defaultChatRoom, "DUMMY_MESSAGE", Akka.system().dispatcher(), 
											null );

		Logger.info("Project Started");
	}

	@Override
	public void onStop(Application arg0) {
		super.onStop(arg0);
	}

	
}

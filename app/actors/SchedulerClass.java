package actors;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

public class SchedulerClass extends UntypedActor{
	
	static ActorRef myActor = Akka.system().actorOf(Props.create(SchedulerClass.class));
	
	static {

		Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
				Duration.create(10, TimeUnit.SECONDS),     //Frequency seconds
				myActor, "dummyMsg", Akka.system().dispatcher(), null );
		
	}

	@Override
	public void onReceive(Object obj) throws Exception {
		if(obj.equals("dummyMsg")){
			//ChatRoom.notifyAllWithDummyMsg();
		}
		//Logger.info("onReceive called : "+new Date());
	}

}

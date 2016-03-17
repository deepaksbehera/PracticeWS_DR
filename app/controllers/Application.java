package controllers;

import actors.ChatRoom;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
    	//return ok(newTemplate.render());
    	return ok(index.render("Your new application is ready."));
    }

}

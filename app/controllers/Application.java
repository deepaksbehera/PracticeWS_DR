package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
		if(LoginController.getLoggedInUser() != null ){
			return redirect(routes.LoginController.getDashBoard());
		}else{
			return ok(index.render("Your new application is ready."));
		}
    }

}

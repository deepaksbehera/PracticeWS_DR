package controllers;

import models.AppUser;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;

public class LoginController extends Controller{

	public Result processLogin(){
		session().remove(Constants.LOGGED_IN_USER_ID);
		DynamicForm form = Form.form().bindFromRequest();
		AppUser appUser = AppUser.find.where().ieq("name",form.get("name").trim()).findUnique();
		if(appUser != null){
			session().put(Constants.LOGGED_IN_USER_ID, appUser.id+"");
			return redirect(routes.LoginController.getDashBoard());
		}else{
			return redirect(routes.Application.index());
		}
	}
	
	public Result getDashBoard(){
		final AppUser appUsr = LoginController.getLoggedInUser();
		if(appUsr != null){
		return ok(views.html.chatWindow.render(appUsr));
		}else{
			session().remove(Constants.LOGGED_IN_USER_ID);
			return redirect(routes.Application.index());
		}	
	}
	
	public static AppUser getLoggedInUser() {
		final String idStr = session(Constants.LOGGED_IN_USER_ID);
		if (idStr != null) {
			return AppUser.find.byId(Long.parseLong(idStr));
		}
		return null;
	}
	
	public Result processLogout() {
		session().remove(Constants.LOGGED_IN_USER_ID);
		return redirect(routes.Application.index());
	}
	
	
}

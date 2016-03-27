package controllers;

import models.AppUser;
import models.GroupChannel;
import play.mvc.Controller;
import play.mvc.Result;

public class SampleController extends Controller{

	public Result sampleData(){
		if(! (AppUser.find.findRowCount() > 0)){
		
			AppUser appUser1  = new AppUser();
			appUser1.firstName = "Deepak";
			appUser1.email = "Deepak";
			appUser1.save();
			
			AppUser appUser2  = new AppUser();
			appUser2.firstName = "Rupesh";
			appUser2.email = "Rupesh";
			appUser2.save();
			
			AppUser appUser3  = new AppUser();
			appUser3.firstName = "Manas";
			appUser3.email = "Manas";
			appUser3.save();
			
			AppUser appUser4  = new AppUser();
			appUser4.firstName = "Bubu";
			appUser4.email = "Bubu";
			appUser4.save();
			
			AppUser appUser5  = new AppUser();
			appUser5.firstName = "Jeet";
			appUser5.email = "Jeet";
			appUser5.save();
			
			AppUser appUser6  = new AppUser();
			appUser6.firstName = "Monti";
			appUser6.email = "Monti";
			appUser6.save();
			
			GroupChannel channel = new GroupChannel();
			channel.name = "GENERAL";
			channel.isGroupGeneral = Boolean.TRUE;
			channel.appUserList.addAll(AppUser.find.all());
			channel.save();
			
			return ok("Done");
		}else{
			return ok("Already Available");	
		}
	}
	
	
}

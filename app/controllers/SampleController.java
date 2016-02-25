package controllers;

import models.AppUser;
import models.GroupChannel;
import play.mvc.Controller;
import play.mvc.Result;

public class SampleController extends Controller{

	public Result sampleData(){
		if(! (AppUser.find.findRowCount() > 0)){
		
			AppUser appUser1  = new AppUser();
			appUser1.name = "Deepak";
			appUser1.save();
			
			AppUser appUser2  = new AppUser();
			appUser2.name = "Rupesh";
			appUser2.save();
			
			AppUser appUser3  = new AppUser();
			appUser3.name = "SunilKumar";
			appUser3.save();
			
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

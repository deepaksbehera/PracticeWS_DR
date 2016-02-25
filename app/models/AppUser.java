package models;

import java.util.List;

import javax.persistence.Entity;

import com.avaje.ebean.Model;

@Entity
public class AppUser extends BaseEntity{
	
	public String name;
	
	public static Model.Finder<Long, AppUser> find = new Model.Finder<Long, AppUser>(AppUser.class);
	
	public List<GroupChannel> getAppUserGroups(){
		return GroupChannel.find.where().in("appUserList", this).findList();
	}
}

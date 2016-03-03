package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.avaje.ebean.Model;

@Entity
public class GroupChannel extends BaseEntity{
	
	public String name;

	@ManyToMany
	@JoinTable(name="app_user_group_channel")
	public List<AppUser> appUserList = new ArrayList<AppUser>();
	
	@ManyToMany
	@JoinTable(name="app_user_admin")
	public List<AppUser> adminList = new ArrayList<AppUser>();
	
	public Boolean isGroupGeneral = Boolean.FALSE;
	
	public static Model.Finder<Long, GroupChannel> find = new Model.Finder<Long, GroupChannel>(GroupChannel.class);
	
	public static GroupChannel getGeneralGroup(){
		return GroupChannel.find.where().eq("isGroupGeneral", Boolean.TRUE).findUnique();
	}
	
}

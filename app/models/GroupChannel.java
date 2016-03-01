package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import com.avaje.ebean.Model;

@Entity
public class GroupChannel extends BaseEntity{
	
	public String name;

	@ManyToMany
	public List<AppUser> appUserList = new ArrayList<AppUser>();
	
	public Boolean isGroupGeneral = Boolean.FALSE;
	
	public static Model.Finder<Long, GroupChannel> find = new Model.Finder<Long, GroupChannel>(GroupChannel.class);
	
	public static GroupChannel getGeneralGroup(){
		return GroupChannel.find.where().eq("isGroupGeneral", Boolean.TRUE).findUnique();
	}
	
}

package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import com.avaje.ebean.Model;

@Entity
public class AppUser extends BaseEntity{
	
	public String name;
	
	@Column(unique = true)
	public String userName;
	
	public String password;
	
	@Lob
	public byte[] photo;
	
	public static Model.Finder<Long, AppUser> find = new Model.Finder<Long, AppUser>(AppUser.class);
	
	public List<GroupChannel> getAppUserGroups(){
		return GroupChannel.find.where().in("appUserList", this).findList();
	}
}

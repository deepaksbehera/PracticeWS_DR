package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import com.avaje.ebean.Model;

import beans.AppUserBean;

@Entity
public class AppUser extends BaseEntity{
	
	public String firstName;
	
	public String lastName;
	
	@Column(unique = true)
	public String email;
	
	public Gender gender;
	
	public String password;
	
	@Lob
	public byte[] image;
	
	@Lob
	public byte[] thumbnailImage;
	
	public static Model.Finder<Long, AppUser> find = new Model.Finder<Long, AppUser>(AppUser.class);
	
	public List<GroupChannel> getAppUserGroups(){
		return GroupChannel.find.where().in("appUserList", this).findList();
	}
	
	public String getFullName(){
		return  (lastName != null) ? firstName+" "+lastName : firstName;
	}
	
	public AppUserBean toBean(){
		AppUserBean bean = new AppUserBean();
			bean.id = this.id;
			bean.firstName = this.firstName;
			bean.lastName = this.lastName;
			bean.email = this.email;
		return bean;
	}
}

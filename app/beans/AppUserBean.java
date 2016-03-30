package beans;

import models.AppUser;

public class AppUserBean {

	public Long id;
	
	public String firstName;
	
	public String lastName;
	
	public String email;
	
	public String password;
	
	public String newPassword;
	
	public String confirmPassword;
	
	public AppUser toEntity(){
			AppUser appUser = new AppUser();
			if(this.id != null){
				appUser = AppUser.find.byId(this.id);
			}
			if(this.firstName != null && !this.firstName.trim().isEmpty()){
				appUser.firstName = this.firstName.trim();
			}
			if(this.lastName != null && !this.lastName.trim().isEmpty()){
				appUser.lastName = this.lastName.trim();
			}
			if(this.email != null && !this.email.trim().isEmpty()){
				appUser.email = this.email.trim();
			}
		return appUser;
	}
}

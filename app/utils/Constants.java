package utils;

import play.Play;

public class Constants {

	public static final String LOGGED_IN_USER_ID = "userId";
	public static final String LOGGED_IN_USER_ROLE = "userRole";
	public static final String EMAIL_ID = "rupesh@thrymr.net";
	public static final String EMAIL_USER_NAME ="Rupesh Patil";
	public static final String HOST_NAME = Play.application().configuration().getString("ip.address");

}

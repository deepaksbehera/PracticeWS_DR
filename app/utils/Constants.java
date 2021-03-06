package utils;

import play.Play;

public class Constants {

	public static final String LOGGED_IN_USER_ID = "userId";
	public static final String LOGGED_IN_USER_ROLE = "userRole";
	public static final String EMAIL_ID = "rupesh@thrymr.net";
	public static final String EMAIL_USER_NAME ="Rupesh Patil";
	public static final String HOST_NAME = Play.application().configuration().getString("ip.address");
	
	//For Message use
	public static final String GROUP_MESSAGE = "GROUP";
	public static final String DIRECT_MESSAGE = "DIRECT";
	public static final String DUMMY_MESSAGE = "DUMMY";
	public static final String ENCRIPTION_KEY = "dAtAbAsE98765432";

}

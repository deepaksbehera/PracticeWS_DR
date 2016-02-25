package models;

import com.avaje.ebean.annotation.EnumValue;

public enum MessageType {
	
	@EnumValue("GROUP") GROUP,
	@EnumValue("INDIVIDUAL") INDIVIDUAL
}

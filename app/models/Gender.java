package models;

import com.avaje.ebean.annotation.EnumValue;

public enum Gender {
	@EnumValue("MALE") MALE ,
	@EnumValue("FEMALE") FEMALE,
	@EnumValue("OTHER") OTHER
}

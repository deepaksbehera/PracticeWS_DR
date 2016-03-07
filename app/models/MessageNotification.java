package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class MessageNotification extends BaseEntity{
	
	@ManyToOne
	public AppUser notificationTo;
	
	@ManyToOne
	public AppUser notificationFrom;
	
	@ManyToOne
	public GroupChannel notificationOfGroup;
    
	public Boolean isSeen = Boolean.FALSE;
	
	public Boolean isViewed = Boolean.FALSE;
	
	public static Finder<Long,MessageNotification> find = new Finder<Long,MessageNotification>(MessageNotification.class);
	
	public static Integer getPersonalUnSeenMsgCount(final AppUser notifiTo, final AppUser notifiFrom){
		return MessageNotification.find.where().eq("notificationTo", notifiTo).eq("notificationFrom", notifiFrom)
												.eq("isSeen", Boolean.FALSE).findRowCount();
	}
	
	public static Integer getGroupUnSeenMsgCount(final AppUser notifiTo, final GroupChannel notifiFromGroup){
		return MessageNotification.find.where().eq("notificationTo", notifiTo).eq("notificationOfGroup", notifiFromGroup)
												.eq("isSeen", Boolean.FALSE).findRowCount();
	}
	
	public static List<MessageNotification> getPersonalUnSeenMessages(final AppUser notifiTo, final AppUser notifiFrom){
		return MessageNotification.find.where().eq("notificationTo", notifiTo).eq("notificationFrom", notifiFrom)
												.eq("isSeen", Boolean.FALSE).findList();
	}
	
	public static List<MessageNotification> getGroupUnSeenMessages(final AppUser notifiTo, final GroupChannel notifiFromGroup){
		return MessageNotification.find.where().eq("notificationTo", notifiTo).eq("notificationOfGroup", notifiFromGroup)
												.eq("isSeen", Boolean.FALSE).findList();
	}
	
	public static void createPersonalNotification(final AppUser notificationFor, final AppUser notificationFrom){
		MessageNotification notification = new MessageNotification();
		notification.notificationTo = notificationFor;
		notification.notificationFrom =notificationFrom;
		notification.save();
	}
	
	public static void createGroupNotification(final AppUser notificationFor, final GroupChannel fromGroup){
		MessageNotification notification = new MessageNotification();
		notification.notificationTo = notificationFor;
		notification.notificationOfGroup = fromGroup;
		notification.save();
	}

}

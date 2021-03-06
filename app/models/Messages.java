package models;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;

@Entity
public class Messages extends BaseEntity{
	
	@Column(columnDefinition = "TEXT")
	public String messgae;
	
	public Date sendOn;
	
	@ManyToOne
	public AppUser sendTo;
	
	@ManyToOne
	public AppUser sendBy;
	
	public Boolean isMessagePersonal = Boolean.TRUE;
	
	public Boolean isDeleted = Boolean.FALSE;
	
	@ManyToOne
	public GroupChannel groupChannel;
	
	public static Model.Finder<Long, Messages> find = new Model.Finder<Long, Messages>(Messages.class);
	
	/**
	 * Service Methods
	 */
	
	public static List<Messages> getPersonalMessages(AppUser loginUser, AppUser requestForUser){
		List<Messages> msgList = Messages.find.where().or(
					Expr.and(Expr.eq("sendBy", loginUser), Expr.eq("sendTo", requestForUser)), 
					Expr.and(Expr.eq("sendBy", requestForUser), Expr.eq("sendTo", loginUser))).orderBy("sendOn").findList();
		return msgList;
	}
	
	public static List<Messages> getGroupMessages(AppUser loginUser, GroupChannel group){
		List<Messages> msgList = new LinkedList<Messages>();
		if(group.appUserList.contains(loginUser)){
			msgList = Messages.find.where().eq("groupChannel", group).orderBy("sendOn").findList();
		}
		return msgList;
	}
	
	public void setEncodedMessage(final String messageText){
		try {
			this.messgae = Base64.getEncoder().encodeToString(messageText.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public String getDecoadedMessage(){
		try{
	         byte[] base64decodedBytes = Base64.getDecoder().decode(this.messgae);
	         return new String(base64decodedBytes, "utf-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
}

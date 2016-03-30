package controllers;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.google.common.io.Files;

import beans.AppUserBean;
import models.AppUser;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

public class UserController extends Controller{
	public static final Form<AppUserBean> appUserForm = Form.form(AppUserBean.class);
	
	public Result getUserForm(final Long appUserId){
			AppUser appUser = AppUser.find.byId(appUserId);
			AppUserBean bean = appUser.toBean();
			Form<AppUserBean> form = appUserForm.fill(bean);
		return ok(views.html.profile.render(form));
	}
	
	public Result saveUser(){
		Form<AppUserBean> filledForm = appUserForm.bindFromRequest();
		if(! filledForm.hasErrors()){
			try{
				AppUserBean bean = filledForm.get();
				AppUser appUser = bean.toEntity();
				MultipartFormData body = request().body().asMultipartFormData();
				if(body != null){
					try{
						FilePart filePart = body.getFile("photo");
						File file = filePart.getFile();
						byte[] bytes = Files.toByteArray(file);
						
						 InputStream inStreamCrop = new ByteArrayInputStream(bytes);
				         BufferedImage bufferedImage = ImageIO.read(inStreamCrop);
				         BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
				         newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
				         ByteArrayOutputStream baos=new ByteArrayOutputStream();
				         ImageIO.write(newBufferedImage, "jpg", baos);
				         baos.flush();
				         if((bytes.length/1024) > 100){
				        	 if(ImageIO.read(file).getHeight() > 400 & ImageIO.read(file).getWidth() > 500){
				        		 appUser.image = buffereImagetoByteArray(Scalr.resize(createImageFromBytes(baos.toByteArray()), Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH,500,400, Scalr.OP_ANTIALIAS));
				        	 }else{
				        		 appUser.image = baos.toByteArray();
				        	 }
				         }else{
				        	 appUser.image = bytes;
				         }
				         if(ImageIO.read(file).getHeight() > 100 & ImageIO.read(file).getWidth() > 100){
				        	 appUser.thumbnailImage = buffereImagetoByteArray(Scalr.resize(createImageFromBytes(baos.toByteArray()), Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH,80,70, Scalr.OP_BRIGHTER)); 
				         }else{
				        	 appUser.thumbnailImage = baos.toByteArray();
				         }
				         
				         Logger.info(">>>Original Size : "+bytes.length/1024 +"kb. New Size : "+appUser.image.length/1024 +"kb.Thumbnail size : "+appUser.thumbnailImage.length/1024 +"kb.");
				         BufferedImage img = ImageIO.read(new ByteArrayInputStream(appUser.image));
				         BufferedImage img2 = ImageIO.read(new ByteArrayInputStream(appUser.thumbnailImage));
				         Logger.info(">>>Original Width : "+ImageIO.read(file).getWidth()+" New Width : "+img.getWidth()+" Thumbnail Width : "+img2.getWidth());						         
				         Logger.info(">>>Original Height : "+ImageIO.read(file).getHeight()+" New Height : "+img.getHeight()+" Thumbnail Height : "+img2.getHeight());
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				if(appUser.id != null){
					appUser.update();
				}else{
					appUser.save();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return redirect(routes.LoginController.getDashBoard());
		}else{
			return badRequest("please fill the form correctly");
		}
	}
	
	public Result showAppUserImage(final Long id) {
		final AppUser user = AppUser.find.byId(id);
		if (user != null && user.image != null) {
			response().setContentType("image/jpeg/png");
			return ok(user.image);
		}else{
			return notFound("Image Not Found");
		}
	}
	
	public Result showAppUserImageAsThumbnail(final Long id) {
		final AppUser user = AppUser.find.byId(id);
		if (user != null && user.thumbnailImage != null) {
			response().setContentType("image/jpeg/png");
			return ok(user.thumbnailImage);
		}else{
			return notFound("Image Not Found");
		}
	}
	
	private BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	private byte[] buffereImagetoByteArray(BufferedImage originalImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
			ImageIO.write( originalImage, "jpeg", baos );
			baos.flush();
	        baos.close();
			} catch (Exception e) {
				e.printStackTrace();
			} 
        return baos.toByteArray();
    }

}

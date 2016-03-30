@(appUserId : Long)
var messageObj;

if ("WebSocket" in window){
$(function(){
    // get websocket class, firefox has a different way to get it for firefox use MozWebSocket
    var WS = window['WebSocket'] ? window['WebSocket'] : WebSocket;
    
    // open pewpew with websocket
    var chatSocket = new WS('@routes.WebSocketController.wsInterface(appUserId).webSocketURL(request)');
    
    var writeMessages = function(event){
    	var data = JSON.parse(event.data);
    	
    	var name = $(this).text();
    	var msgType = data.messageType;
    	var msgToId = data.toId;
    	var msgById = data.byId;
    	var msgKind = data.messageKind;
    	var msgContent = data.messageContent;
    	var msgContForNotf = data.msgContForNotf;
    	var msgByName = data.msgByName;
    	//console.log(JSON.stringify(data));
    	//console.log("");
    	if(msgType == "DUMMY"){
    		showOnlineAndOffline(data.onlineUserList, data.toUserId);
    	}else{
    		if(msgType == "DIRECT"){
    			if(msgKind == "myMsg"){
    				appendMsgLi(msgById, msgContent);
    			}else{
    				var activeSelect = $('.directMsgs .clickSection.active');
    				if(activeSelect.attr('msg-to-id') == msgById){
    					appendMsgLi(msgById, msgContent);
    				}else{
    					var count = parseInt($('.directMsgs .clickSection[msg-to-id="'+msgById+'"]').find('.badge').html());
    					count++;
    					$('.directMsgs .clickSection[msg-to-id="'+msgById+'"]').find('.badge').html(count);
    					
    					showDesktopNotification(msgContForNotf, msgByName, msgType, msgById);
    					
    					createNotification(msgType, msgToId, msgById)
    				}
    			}
    		}else{
    			if(msgKind == "myMsg"){
    				appendMsgLi(msgById, msgContent);
    			}else{
    				var activeSelect = $('.groupMsgs .clickSection.active');
    				if(activeSelect.attr('msg-to-id') == msgToId){
    					appendMsgLi(msgById, msgContent);
    				}else{
    					var count = parseInt($('.groupMsgs .clickSection[msg-to-id="'+msgToId+'"]').find('.badge').html());
    					count++;
    					$('.groupMsgs .clickSection[msg-to-id="'+msgToId+'"]').find('.badge').html(count);
    					
    					showDesktopNotification(msgContForNotf, msgByName, msgType, msgById);
    					
    					createNotification(msgType, msgToId, msgById);
    				}
    			}
    			
    		}
    		$(".chat-history").scrollTop($(".chat-history")[0].scrollHeight);
    	}
    }
    
    //message from server comes here
    chatSocket.onmessage = writeMessages;
    
    $('#messageBox').keyup(function(event){
        var charCode = (event.which) ? event.which : event.keyCode ;
        // if enter (charcode 13) is pushed, send message, then clear input field
        if(charCode === 13){
        	sendMessage($(this).val());   
        }
    }); 
    /*$(document).on('click','#sendMessage',function(){
    	sendMessage($('#messageBox').val());
    });*/
    
    function sendMessage(message){
    	//alert(message+">"+type+">"+toUserId)
    	messageObj = new Object();
		messageObj.content = message;
		messageObj.msgType = $('#msgTypeVal').val();
		messageObj.msgById = $('#msgById').val();
		messageObj.msgToId = $('#msgToId').val();
		var jsonText = JSON.stringify(messageObj);
    	//alert(jsonText);
    	chatSocket.send(jsonText);
    	$('#messageBox').val(''); 
    }
    
    function createNotification(msgType, msgToId, msgById){
    	$.get('/create-notification/'+msgType+'/'+msgToId+'/'+msgById,function(data){}); 
    }
    
    function appendMsgLi(msgById, messageContent){
    	var lastMsgId = $('#message-data .message-li').last().attr('messageOfUser');
    	console.log(lastMsgId+ ">append>>"+msgById);
    	if(lastMsgId == msgById){
    		console.log("same");
    		var htmlElements = $(messageContent);
    		//alert(htmlElements);
    		var found = htmlElements.find('.message');
    		//alert(found);
    		$('#message-data .message-li').last().append(found);
    		//$('#message-data').append(messageContent);
    		//$('#message-data .message-li').last().find('.dateAndName').remove();
    		//message
    	}else{
    		$('#message-data').append(messageContent);
    	}
    }
    
    function showOnlineAndOffline(onlineUserList, toUserId){
		console.log(onlineUserList);
		$('.status i').removeClass('online');
		$('.status i').addClass('offline');
		//$('.status i').html('&nbsp; Offline');
		$.each(onlineUserList,function(index, ouser){
			//if(!(ouser == toUserId)){
				//console.log('#status-'+ouser);
				$('#status-'+ouser).removeClass('offline');
				$('#status-'+ouser).addClass('online');
				//$('#status-'+ouser).html('&nbsp; Online');
			//}
		});
		console.log("dummy message");
    }
    
    function showDesktopNotification(msgContent, msgByName, msgType, msgById){
    	var isFirefox = typeof InstallTrigger !== 'undefined';   // Firefox 1.0+
    	var nOptions;
    	var notification;
		 if(isFirefox==true){
			  nOptions = {
					  body: msgContent
				     // iconUrl: 'https://s3-us-west-2.amazonaws.com/s.cdpn.io/195612/chat_avatar_01_green.jpg'
			  }
		  }else{
			  nOptions = {
					  body: msgContent
					  //icon: 'https://s3-us-west-2.amazonaws.com/s.cdpn.io/195612/chat_avatar_01_green.jpg'
			  }
		  }
		console.log(nOptions);
		if (!("Notification" in window)) {
    	    alert("This browser does not support desktop notification");
    	} else {
    		 	if (Notification.permission === "granted") {	
    	    		notification = new Notification(msgByName, nOptions);
    	  		} else {
    		  			if (Notification.permission !== 'denied') {
    	    				Notification.requestPermission(function (permission) {
    					     	if (permission === "granted") {
    					        	notification = new Notification(msgByName, nOptions);
    					      	}
    	    				});
    		  			}
    	  		}
    	  }
		
		notification.onclick = function () {
			//window.location.href = '/secure-hms/connect';
		};
		notification.onerror = function () {
		};
		notification.onshow = function () {
		};
		notification.onclose = function () {
		};
		setTimeout(notification.close.bind(notification),3000);
    }
    
    
});


}else{
	alert("Websocket is not supported by ur browser");
}
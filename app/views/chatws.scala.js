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
    	//console.log(JSON.stringify(data));
    	console.log("");
    	if(msgType == "DUMMY"){
    		console.log("dummy message");
    	}else{
    		if(msgType == "DIRECT"){
    			if(msgKind == "myMsg"){
    				appendMsgLi(msgById, data.messageContent);
    			}else{
    				var activeSelect = $('.directMsgs .clickSection.active');
    				if(activeSelect.attr('msg-to-id') == msgById){
    					appendMsgLi(msgById, data.messageContent);
    				}else{
    					var count = parseInt($('.directMsgs .clickSection[msg-to-id="'+msgById+'"]').find('.badge').html());
    					count++;
    					$('.directMsgs .clickSection[msg-to-id="'+msgById+'"]').find('.badge').html(count);
    					
    					createNotification(msgType, msgToId, msgById)
    				}
    			}
    		}else{
    			if(msgKind == "myMsg"){
    				appendMsgLi(msgById, data.messageContent);
    			}else{
    				var activeSelect = $('.groupMsgs .clickSection.active');
    				if(activeSelect.attr('msg-to-id') == msgToId){
    					appendMsgLi(msgById, data.messageContent);
    				}else{
    					var count = parseInt($('.groupMsgs .clickSection[msg-to-id="'+msgToId+'"]').find('.badge').html());
    					count++;
    					$('.groupMsgs .clickSection[msg-to-id="'+msgToId+'"]').find('.badge').html(count);
    					
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
    $(document).on('click','#sendMessage',function(){
    	sendMessage($('#messageBox').val());
    });
    
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
    	//console.log(lastMsgId+ ">append>>"+msgById);
    	if(lastMsgId == msgById){
    		//console.log("same");
    		
    		$('#message-data').append(messageContent);
    		$('#message-data .message-li').last().find('.dateAndName').remove();
    	}else{
    		$('#message-data').append(messageContent);
    	}
    	
    }
    
});


}else{
	alert("Websocket is not supported by ur browser");
}
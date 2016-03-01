@(appUserId : Long)

//Rupesh code
var messageObj;

if ("WebSocket" in window){
$(function(){
    // get websocket class, firefox has a different way to get it for firefox use MozWebSocket
    var WS = window['WebSocket'] ? window['WebSocket'] : WebSocket;
    
    // open pewpew with websocket
    var chatSocket = new WS('@routes.WebSocketController.wsInterface(appUserId).webSocketURL(request)');
    
    var writeMessages = function(event){
    	//var data = JSON.parse(event.data)
      //  $('#message-data').prepend(data.messageContent);
    	var data = JSON.parse(event.data);
    	var name = $(this).text();
    	var msgType = data.messageType;
    	var msgToId = data.toId;
    	var msgById = data.byId;
    	var msgKind = data.messageKind;
    	if(msgType == "DIRECT"){
    		if(msgKind == "myMsg"){
    			$('#message-data').append(data.messageContent);
    		}else{
    			var activeSelect = $('.directMsgs .clickSection.active');
    			if(activeSelect.attr('msg-to-id') == msgById){
    				$('#message-data').append(data.messageContent);
    			}else{
    				var count = parseInt($('.directMsgs .clickSection[msg-to-id="'+msgById+'"]').find('.badge').html());
    				count++;
    				$('.directMsgs .clickSection[msg-to-id="'+msgById+'"]').find('.badge').show().html(count).css({'background-color':'rgb(201, 73, 73)'});
    			}
    		}
    	 }
    	if(msgType == "GROUP"){
    		 if(msgKind == "myMsg"){
     			$('#message-data').append(data.messageContent);
     		}else{
     			var activeSelect = $('.groupMsgs .clickSection.active');
    			if(activeSelect.attr('msg-to-id') == msgToId){
    				$('#message-data').append(data.messageContent);
    			}else{
    				var count = parseInt($('.groupMsgs .clickSection[msg-to-id="'+msgToId+'"]').find('.badge').html());
    				count++;
    				$('.groupMsgs .clickSection[msg-to-id="'+msgToId+'"]').find('.badge').show().html(count).css({'background-color':'rgb(201, 73, 73)'});	
    			}
     		}
    		 
    	 }
    	if(msgType == "CRON"){
    		console.log(data.messageContent);
    	}
    	$(".chat-history").scrollTop($(".chat-history")[0].scrollHeight);
    	
    }
    
    chatSocket.onmessage = writeMessages;
    
    $('#messageBox').keyup(function(event){
        var charCode = (event.which) ? event.which : event.keyCode ;
        // if enter (charcode 13) is pushed, send message, then clear input field
        if(charCode === 13){
        	if($(this).val().trim().length > 0){
        		sendMessage($(this).val());
        	}
        }
    }); 
    $(document).on('click','#sendMessage',function(){
    	var msg = $('#messageBox').val();
    	if(msg.trim().length > 0){
    		sendMessage(msg);
    	}
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
		console.log(jsonText);
    	chatSocket.send(jsonText);
    	$('#messageBox').val(''); 
    }
    
});


}else{
	alert("Websocket is not supported by ur browser");
}
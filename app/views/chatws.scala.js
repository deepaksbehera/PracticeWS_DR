@(appUserId : Long)
var messageObj;
if ("WebSocket" in window){
$(function(){
    // get websocket class, firefox has a different way to get it for firefox use MozWebSocket
    var WS = window['WebSocket'] ? window['WebSocket'] : WebSocket;
    
    // open pewpew with websocket
    var chatSocket = new WS('@routes.WebSocketController.wsInterface(appUserId).webSocketURL(request)');
    
    chatSocket.onmessage = receiveEventOn;
    
    var receiveEventOn = function(event){
    	alert(event);
		var data = JSON.parse(event.data)
    }
    
    
    var writeMessages = function(event){
    	alert(event);
        $('#message-data').prepend('<p>'+event.data+'</p>');
    }
    
    chatSocket.onmessage = receiveEventOn;
    
    $('#messageBox').keyup(function(event){
        var charCode = (event.which) ? event.which : event.keyCode ;
        // if enter (charcode 13) is pushed, send message, then clear input field
        if(charCode === 13){
        	sendMessage($(this).val());   
        }
    }); 
    $(document).on('click','#sendMessage',function(){
    	sendMessage($('#messageBox').val())
    });
    
    //var JSONObj = { "senderId" : @appUserId, "isMessagePersonal"  : true , "message": message};
    //alert(JSONObj.senderId);
    //alert(JSONObj.messageType);
    
    
    function sendMessage(message){
    	var JSONObj = { "senderId" : @appUserId, "isMessagePersonal"  : true , "messageText": message};
    	messageObj = new Object();
		messageObj.messageText = message;
		messageObj.toUserId = @appUserId;
		messageObj.isMessagePersonal= true;
    	chatSocket.send(JSON.stringify(messageObj));
    	$('#messageBox').val(''); 
    }
    
});


}else{
	alert("Websocket is not supported by ur browser");
}
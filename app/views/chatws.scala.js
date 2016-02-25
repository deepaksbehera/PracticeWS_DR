@(appUserId : Long)
var messageObj;

if ("WebSocket" in window){
$(function(){
    // get websocket class, firefox has a different way to get it for firefox use MozWebSocket
    var WS = window['WebSocket'] ? window['WebSocket'] : WebSocket;
    
    // open pewpew with websocket
    var chatSocket = new WS('@routes.WebSocketController.wsInterface(appUserId).webSocketURL(request)');
    
    var writeMessages = function(event){
    	var data = JSON.parse(event.data)
        $('#message-data').prepend(data.messageContent);
    }
    
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
    
});


}else{
	alert("Websocket is not supported by ur browser");
}
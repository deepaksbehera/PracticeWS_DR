@(appUserId : Long)

if ("WebSocket" in window){
$(function(){
    // get websocket class, firefox has a different way to get it for firefox use MozWebSocket
    var WS = window['WebSocket'] ? window['WebSocket'] : WebSocket;
    
    // open pewpew with websocket
    var chatSocket = new WS('@routes.WebSocketController.wsInterface(appUserId).webSocketURL(request)');
    
    var writeMessages = function(event){
        $('#message-data').prepend('<p>'+event.data+'</p>');
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
    	sendMessage($('#messageBox').val())
    });
    
    function sendMessage(message){
    	chatSocket.send(message);
    	$('#messageBox').val(''); 
    }
    
});


}else{
	alert("Websocket is not supported by ur browser");
}
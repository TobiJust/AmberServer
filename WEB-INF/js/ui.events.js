/**
 * ui event handler reference for 
 * amber client web application, based on 
 * jQuery event handling 
 */
// bind all event handlers to their elements 
// if document is completely rendered
$(document).ready(function(){
	// login
	$(document).on("click", ".loginsubmit", function(){
		// determine sockets readyState: 1 is ready
		if(amber.net.AmberSocket.readyState == 1)
			amber.net.reqLogin(); // connect if ready
		else // inform user if its not ready
			amber.ui.logBootStatus("Keine Verbindung zum Server vorhanden, bitte warten...");
	});
	// toggle notification center
	$(document).on("click", "#btnnotifier", function(){
		amber.ui.toggleNotifier();
	});
	// toggle car picker dialog
	$(document).on("click", "#btncarpicker", function(){
		amber.ui.openCarPicker();
	});
	// switch video/map view
	$(document).on("click", "#btnswitchview", function(){
		amber.ui.switchLiveView();
	});
	// record a video on server side
	$(document).on("click", "#btnrecord", function(){
		if(!amber.media.recording)
			amber.net.startRecord();
		else
			amber.net.stopRecord();
	});
	// choose a car for live control
	$(document).on("click", ".car", function(){
		amber.ui.carPicked($(this));
	});
	// make a screenshot from the latest image blob sent
	$(document).on("click","#btnscreenshot",function(){
		amber.media.screenshot();
	});
	// open commands view
	$(document).on("click","#btncommands",function(){
		
	});
	// logout from Amber
	$(document).on("click","#btnlogout",function(){
		amber.net.reqLogout();
	});
});















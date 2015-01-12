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
		amber.media.toggleRecording();
	});
	// choose a car for live control
	$(document).on("click", ".car", function(){
		amber.ui.carPicked($(this));
	});
	// make a screenshot from the latest image blob sent
	$(document).on("click","#btnscreenshot",function(){
		amber.net.reqScreenshot();
	});
	// start download of recorded video
	$(document).on("click","#btndownload",function(){
		amber.media.downloadVideo();
		amber.ui.toggleDownloadBtn();
	});
	// start download of screenshot
	$(document).on("click","#btndownloadscreen",function(){
		amber.media.screenshot();
		amber.ui.toggleDownloadBtnScreen();
	});
	// open commands view
	$(document).on("click","#btncommands",function(){
		amber.ui.toggleCommandCenter();
	});
	// logout from Amber
	$(document).on("click","#btnlogout",function(){
		amber.net.reqLogout();
	});
	// choose a command to be sent to the OBU
	$(document).on("click",".command",function(){
		amber.net.reqSendCommand($(this).attr("command"));
		amber.ui.commandPicked();
	});
	// add a car to the database
	$(document).on("click","#btnaddcar",function(){
		amber.ui.toggleAddCar();
	});
	// submit new car obu id to save in DB:
	$(document).on("click",".newcarsubmit",function(){
		amber.net.addCar();
	});
});















// init variables for videocontainer-elements
amber.ui.initVideoContainers = function(){
	this.liveViewL = document.getElementById('liveviewL');
	this.liveViewS = document.getElementById('liveviewS');
};
// set the initial state of mini map and live stream
amber.ui.initLiveView = function(){
	var switchables = $('#map, #videocontainer, #mapview, #videosmall');
	// set initial state: small map, big live view visible, small view and big map hidden
	$(switchables[1]).toggle();
	$(switchables[2]).toggle();
	// draw small map initially
	amber.locals.drawMaps('map');
	this.initVideoContainers();
};
// switch live stream and mini map
amber.ui.switchLiveView = function(){
	this.toggleLiveViews();
	this.toggleMaps();
};
// switch the map views 
amber.ui.toggleMaps = function(){
	var maps = $('#map, #mapview').toggle();
	if($(maps[0]).is(":visible"))
		amber.locals.drawMaps($(maps[0]).attr("id"));
	if($(maps[1]).is(":visible"))
		amber.locals.drawMaps($(maps[1]).attr("id"));
};
// switch the live video shows
amber.ui.toggleLiveViews = function(){
	$('#videosmall, #videocontainer').toggle();
};
// show request stats, connectivity and data during login
amber.ui.logBootStatus = function(message){
	$('.info').text(message);
};
// toggle notification center
amber.ui.toggleNotifier = function(){
	// if center is open:
	if($('.notifier').attr('open')){
		$('.notifier').animate({
			"marginLeft":"-20%" // close not.center
		},"fast", function(){
			$('.notifier').removeAttr('open');
		});
	// if center is closed:
	} else {
		$('.notifier').animate({
			"marginLeft":"0" // open not.center
		},"fast").attr('open','');
		this.notifierToggled = true;
	}
};
// open car picker dialog, stop running data stream
// new car MUST be picked to reenable data streaming, but session is held
amber.ui.openCarPicker = function(){
	amber.net.stopDataStream(amber.cars.Current.id);
	this.closeVideoStream();
	$('.loginfade, .loginmaterial, .carpickerbody').fadeIn("fast");
};
// close car picker dialog
amber.ui.closeCarPicker = function(){
	$('.loginfade, .loginmaterial, .carpickerbody').fadeOut("fast");
};
// close login screen and switch to carpicker
amber.ui.closeLogin = function(){
	$('.loginbody').fadeOut("fast",function(){
		$('.carpickerbody').fadeIn("fast");
	});
};
// open login dialog after logout
amber.ui.openLogin = function(s){
	$('.carpickerbody').fadeOut();
	$('.loginfade, .loginmaterial, .loginbody').fadeIn("fast");
};
// append a notification to the notification center
amber.ui.appendNotification = function(data){
	$('#noticontainer').append('<li class="notification">'+data+'</li>');
};
// append cars to car picker dialog (data is considered a list)
amber.ui.appendCars = function(data){
	console.log("Appending cars to car picker...");
	// clean from older car elements to avoid duplicates
	$('.carcontainer').empty();
	var concat = [];
	// iterate over car list:
	for(var l = 0; l < data.length; l++){
		var car = data[l];
		var imgUrl = "data:image/png;base64,"+car.image;
		var element = '<li class="car"'
					 +'carname="'+car.vehicleName+'"'
					 +'carid="'+car.vehicleID+'">';
		element += '<p>'+car.vehicleName+'</p>';
		element += '<img src="'+imgUrl+'">';
		element += '</li>';
		// push the new elements string representative in a list
		concat.push(element);
	}
	// append the car elements by concatenating the single strings
	// in the list to one big single string and inject it in the 
	// html list
	$('.carcontainer').append(concat.join(""));
	this.closeLogin();
};
// pick a car:
amber.ui.carPicked = function(element){
	// set the current car 
	amber.cars.Current.id = element.attr('carid');
	// start data streaming from the picked car
	amber.net.startDataStream();
	// reset old route viewed in the map 
	amber.locals.resetRoute();
	// close car picker
	this.closeCarPicker();
};
// set ambers armature labels, represented by css-classes
amber.ui.setArmatures = function(data){
	$('.target_kmall').text(data.kmAtAll);
	$('.target_km').text(data.km);
	$('.target_airflow').text(data.airFlow);
	$('.target_airpress').text(data.airPressure);
	$('.target_airtemp').text(data.airPressure);
	$('.target_coolingliq').text(data.coolingLiqTemp);
	$('.target_fuelpress').text(data.fuelPressure);
	$('.target_eviropress').text(data.enviroPressure);
	$('.drivetarget').text(data.drive);
	$('.tachotarget').text(data.speed);
};
// retrieve logindata from login dialog
amber.ui.getLoginData = function(){
	// build an object for in-app data communication
	var loginData = {};
	loginData.email = $('.loginfield[type=email]').val();
	loginData.password = $('.loginfield[type=password]').val();
	return loginData;
};
// clear login fields:
amber.ui.clearLogin = function(){
	$('.loginfield[type=email]').val("");
	$('.loginfield[type=password]').val("");
};
// clear the video stream containers for logout and while chosing new car
amber.ui.closeVideoStream = function(){
	this.liveViewL.src = "";
	this.liveViewS.src = "";
};

/* amber.ui.FX
 * visual effects for the amber control panel
 * powered by CSS3
 */
// switch on "the lights" after login
amber.ui.FX.lightsON = function(){
	
};
// blinking record symbol due video stream recording
amber.ui.FX.recordingON = function(){
	if(amber.media.recording){
		$('#btnrecord').fadeTo('slow', 0.1).fadeTo('slow', 1.0);
		setTimeout(function(){
			amber.ui.FX.recordingON();
		},500);
	}
};
// new notification received!
amber.ui.FX.notificationON = function(){
	if(!amber.ui.notifierToggled){
		$('#btnnotifier').fadeTo('slow', 0.1).fadeTo('slow', 1.0);
		setTimeout(function(){
			amber.ui.FX.notificationON();
		},500);
	}
};








// complete javascript reference for amber client web application
// .done => implemented as function in corresponding name space!
var amber = {};
amber.userID;
amber.userName; 
// amber.locals, containing all osm localization information 
amber.locals = {};
amber.locals.AmberMap;
amber.locals.amberMapContainer;
amber.locals.LayerMapnik = {};
amber.locals.LayerMarkers = {};
amber.locals.Route = [];
amber.locals.RouteLayer;
amber.locals.Marker = {};
amber.locals.zoom = 18;
amber.locals.LongLat = {};
// static for testing
amber.locals.LongLat.lon = 13.63286;
amber.locals.LongLat.lat = 52.31954;
// functions
amber.locals.initRouteLayer;		//.done
amber.locals.resetRoute;			//.done
amber.locals.initLayers;			//.done
amber.locals.drawMap;				//.done
amber.locals.drawMaps;				//.done
amber.locals.updateMarker;			//.done
amber.locals.jumpTo;				//.done

// amber.media, containing all demanded media data like screenshots and videos
amber.media = {};
amber.media.recording = false;
amber.media.scene;
// functions:
amber.media.screenshot; 
// amber.cars, containing all car data available
amber.cars = {};
// list of available cars
amber.cars.carList = [];
// currently viewed car data
amber.cars.Current = {};
amber.cars.Current.id;
amber.cars.Current.drive;
amber.cars.Current.speed;
amber.cars.Current.airFlow;
amber.cars.Current.airPressure;
amber.cars.Current.airTemperature;
amber.cars.Current.coolingLiqTemp;
amber.cars.Current.fuelPressure;
amber.cars.Current.enviroPressure;
amber.cars.Current.kmAtAll;
amber.cars.Current.km;
amber.cars.Current.fuel;
// amber.ui - UI functionalities of AMBER
amber.ui = {};
amber.ui.liveViewL;
amber.ui.liveViewS;
amber.ui.notifierToggled = false;
// ui functions 
amber.ui.initVideoContainers;	//.done
amber.ui.initLiveView; 			//.done
amber.ui.toggleMaps;			//.done
amber.ui.toggleLiveViews;		//.done
amber.ui.switchLiveView;		//.done
amber.ui.openCarPicker;			//.done
amber.ui.closeCarPicker;		//.done
amber.ui.toggleNotifier;		//.done
amber.ui.logBootStatus;			//.done
amber.ui.appendNotification;	//.done	
amber.ui.appendCars;			//.done
amber.ui.carPicked;				//.done
amber.ui.setArmatures;			//.done
amber.ui.closeLogin;			//.done
amber.ui.openLogin;				//.done
amber.ui.getLoginData;			//.done
amber.ui.clearLogin;			//.done
amber.ui.closeVideoStream;		//.done
// FX - namespace for ui visual effects 
amber.ui.FX = {};
// functions:
amber.ui.FX.lightsON;
amber.ui.FX.recordingON;		//.done
amber.ui.FX.notificationON;		//.done
// amber.net - all network properties are stored here 
amber.net = {};
// functions:
amber.net.initSocket; 
amber.net.startDataStream;		//.done
amber.net.stopDataStream;		//.done
amber.net.messageReceived; 		//.done
amber.net.processCockpitData;	//.done
amber.net.processNotification;	//.done
amber.net.processLiveStreamData;//.done
amber.net.reqSendCommand;		//.done
amber.net.commandSuccess;		//.done
amber.net.commandError;			//.done
amber.net.startRecord;			//.done
amber.net.startRecSuccess;		//.done
amber.net.startRecError;		//.done
amber.net.stopRecord;			//.done
amber.net.stopRecSuccess;		//.done
amber.net.stopRecError;			//.done
amber.net.reqLogin;				//.done
amber.net.loginSuccess;			//.done
amber.net.loginError;			//.done
amber.net.reqLogout;			//.done
amber.net.logoutDone;			//.done
amber.net.reqCars;				//.done	
amber.net.carsSuccess;			//.done
amber.net.carsError;			//.done
// amber.net.AmberSocket - WebSocket-based data streaming infrastructure
amber.net.AmberSocket = {};
//amber.net.Param - params for socket.send commands
amber.net.Param = {};
amber.net.Param.SOCKETADRESS = "ws://10.220.6.118:3001/websocket/";
// send commands over socket:
amber.net.Param.STARTSTREAM = "startStream";
amber.net.Param.STOPSTREAM = "stopStream";
amber.net.Param.GETLOGIN = "requestLogin";
amber.net.Param.GETLOGOUT = "requestLogout";
amber.net.Param.GETCARS = "requestCars";
amber.net.Param.SENDCOMMAND = "sendCommand";
amber.net.Param.STARTRECORD = "startRecord";
amber.net.Param.STOPRECORD = "stopRecord";





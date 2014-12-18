// complete javascript reference for amber client web application
var amber = {};
// amber.locals, containing all osm localization information 
amber.locals = {};
amber.locals.AmberMap;
amber.locals.amberMapContainer;
amber.locals.LayerMapnik = {};
amber.locals.LayerMarkers = {};
amber.locals.Marker = {};
amber.locals.zoom = 18;
amber.locals.LongLat = {};
// static for testing
amber.locals.LongLat.lon = 13.63286;
amber.locals.LongLat.lat = 52.31954;
// functions
amber.locals.initLayers;
amber.locals.drawMap;
amber.locals.drawMaps;
amber.locals.updateMarker;
amber.locals.jumpTo;

// amber.media, containing all demanded media data like screenshots and videos
amber.media = {};
// amber.cars, containing all car data available
amber.cars = {};
// list of available cars
amber.cars.carList = [];
// currently viewed car data
amber.cars.Current = {};
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
// ui functions - growing
amber.ui.initLiveView;
amber.ui.toggleMaps;
amber.ui.toggleLiveViews;
amber.ui.switchLiveView;
amber.ui.openCarPicker;
amber.ui.closeCarPicker;
amber.ui.toggleNotifier;
amber.ui.updateUI;
amber.ui.logBootStatus;
amber.ui.appendNotification;
amber.ui.appendCars;
amber.ui.setArmatures;
amber.ui.closeLogin;
amber.ui.openLogin;
// amber.net - all net properties are stored here 
amber.net = {};
// amber.net.AmberSocket - WebSocket-based data streaming infrastructure
amber.net.AmberSocket = {};
amber.net.AmberSocket.onmessage; 
amber.net.AmberSocket.processData;
amber.net.AmberSocket.receiveNotification;
amber.net.AmberSocket.receiveCarData;
//amber.net.Param - params for ajax reqs etc.
amber.net.Param = {};
amber.net.Param.ADDRESS = "http://10.220.3.158:3001/";
amber.net.Param.GETLOGIN = "requestLogin";
amber.net.Param.GETLOGOUT = "requestLogout";
amber.net.Param.GETCARS = "requestCars";
amber.net.Param.GETCARDATA = "requestDataPackage";
amber.net.Param.SENDCOMMAND = "sendCommand";
amber.net.Param.STARTRECORD = "starRecord";
amber.net.Param.STOPRECORD = "stopRecord";
// amber.net.Ajax - ajax calls etc.
amber.net.Ajax = {};
amber.net.Ajax.reqLogin;
amber.net.Ajax.loginSuccess;
amber.net.Ajax.loginError;
amber.net.Ajax.reqLogout;
amber.net.Ajax.reqCars;
amber.net.Ajax.reqCarData;
amber.net.Ajax.reqSendCommand;




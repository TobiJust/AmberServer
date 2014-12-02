//var layerMapnik;
//var layerMarkers;
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
amber.carList = [];
// currently viewed car data
amber.cars.Current;
// amber.ui - UI functionalities of AMBER
amber.ui = {};
// ui functions - growing
amber.ui.initLiveView;
amber.ui.togglMaps;
amber.ui.toggleLiveViews;
amber.ui.switchLiveView;
amber.ui.toggleCarPicker;
amber.ui.toggleNotifier;
amber.ui.updateUI;
amber.ui.logBootStatus;
// amber.net - all net properties are stored here 
amber.net = {};
// amber.net.Param - params for ajax reqs etc.
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
amber.net.Ajax.reqLogout;
amber.net.Ajax.reqCars;
amber.net.Ajax.reqCarData;
amber.net.Ajax.reqSendCommand;




//draw both maps in amber 
amber.locals.drawMaps = function(element){
	$('#'+amber.locals.amberMapContainer).empty();
	amber.locals.amberMapContainer = element;
	$('#'+element).empty();
	amber.locals.initLayers();
	amber.locals.drawMap(element);
};
// draw a particular map for amber
amber.locals.drawMap = function(mapContainer) {
	this.AmberMap = {};
	this.initLayers();
	this.AmberMap = new OpenLayers.Map(mapContainer, {
		projection: new OpenLayers.Projection("EPSG:900913"),
		displayProjection: new OpenLayers.Projection("EPSG:4326"),
		controls: [new OpenLayers.Control.Navigation(),
		           new OpenLayers.Control.LayerSwitcher()],
		maxExtent: new OpenLayers.Bounds(
								-20037508.34,
								-20037508.34,
								 20037508.34, 
								 20037508.34),
		numZoomLevels: 30,
		maxResolution: 156543,
		units: 'meters'
	});
	this.AmberMap.addLayers([this.LayerMapnik,this.LayerMarkers]);
	this.jumpTo(this.AmberMap);
	addMarker(this.AmberMap, this.LayerMarkers, this.LongLat.lon, this.LongLat.lat, "");
};
// init osm layers
amber.locals.initLayers = function(){
	OpenLayers.Lang.setCode('de');
	this.LayerMapnik = {};
	this.LayerMarkers = {};
	this.LayerMapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
	this.LayerMapnik.tileOptions.crossOriginKeyword = null;
	this.LayerMarkers = new OpenLayers.Layer.Markers("Address", { 
		projection: new OpenLayers.Projection("EPSG:4326"), 
	    visibility: true, 
	    displayInLayerSwitcher: false 
	});
};
// move the marker to the long and lat given as parameters
amber.locals.updateMarker = function(){
	// only for testing!//
	this.LongLat.lon += 0.001;
	this.LongLat.lat += 0.001;
	/***************************/
    var newPx = this.AmberMap.getLayerPxFromViewPortPx(
    		this.AmberMap.getPixelFromLonLat(
    				new OpenLayers.LonLat(this.LongLat.lon, 
    									  this.LongLat.lat).transform(
    						this.AmberMap.displayProjection, 
    						this.AmberMap.projection)));
    this.Marker.moveTo(newPx);
    this.zoom = 18;
    this.jumpTo();
};

amber.locals.jumpTo = function() {
    var x = Lon2Merc(this.LongLat.lon);
    var y = Lat2Merc(this.LongLat.lat);
    this.AmberMap.setCenter(new OpenLayers.LonLat(x, y), this.zoom);
    return false;
};
 
//functions supplied by OSM tutorial
function Lon2Merc(lon) {
    return 20037508.34 * lon / 180;
}
 
function Lat2Merc(lat) {
    var PI = 3.14159265358979323846;
    lat = Math.log(Math.tan( (90 + lat) * PI / 360)) / (PI / 180);
    return 20037508.34 * lat / 180;
}
 

function addMarker(map, layer, lon, lat, popupContentHTML) {
    var ll = new OpenLayers.LonLat(Lon2Merc(lon), Lat2Merc(lat));
    var feature = new OpenLayers.Feature(layer, ll); 
    amber.locals.Marker = new OpenLayers.Marker(ll);
    var marker = amber.locals.Marker;
    feature.closeBox = true;
    feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(300, 180) } );
    feature.data.popupContentHTML = popupContentHTML;
    feature.data.overflow = "hidden";
 
    
    marker.feature = feature;
 
    var markerClick = function(evt) {
        if (this.popup == null) {
            this.popup = this.createPopup(this.closeBox);
            map.addPopup(this.popup);
            this.popup.show();
        } else {
            this.popup.toggle();
        }
        OpenLayers.Event.stop(evt);
    };
    marker.events.register("mousedown", feature, markerClick);
 
    layer.addMarker(marker);
}
 
function getCycleTileURL(bounds) {
   var res = this.Map.getResolution();
   var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
   var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
   var z = this.Map.getZoom();
   var limit = Math.pow(2, z);
 
   if (y < 0 || y >= limit)
   {
     return null;
   }
   else
   {
     x = ((x % limit) + limit) % limit;
 
     return this.url + z + "/" + x + "/" + y + "." + this.type;
   }
}
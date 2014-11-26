//var layerMapnik;
//var layerMarkers;
var amber = {};
// amber.locals, containing all osm localization information 
amber.locals = {};
amber.locals.AmberMap = {};
amber.locals.LayerMapnik = {};
amber.locals.LayerMarkers = {};
amber.locals.Marker = {};
// highest zoom level possible, further initiation on a 
// lower level to load more map content, renders marker 
// updates more stable (without 404s of map tile pictures)
amber.locals.zoom = 14;
amber.locals.LongLat = {};
// static for testing
amber.locals.LongLat.lon = 13.63286;
amber.locals.LongLat.lat = 52.31954;
// functions
amber.locals.drawMap;
amber.locals.updateMarker;
amber.locals.jumpTo;

// amber.media, containing all demanded media data like screenshots and videos
amber.media = {};
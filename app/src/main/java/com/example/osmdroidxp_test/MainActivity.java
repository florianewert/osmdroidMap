package com.example.osmdroidxp_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.util.TileSystemWebMercator;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;


import org.osmdroid.views.overlay.ClickableIconOverlay;
import org.osmdroid.views.overlay.GroundOverlay;
import org.osmdroid.views.overlay.GroundOverlay2;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private MapView map = null;
    public double zoomlvl;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    List<Seat> seatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = this.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        map = findViewById(R.id.mapview);

        map.setTileSource(new OnlineTileSourceBase("Bagarino Saalplan a", 0, 5, 256, ".png",
                new String[] { "https://tickets.demo.bagarino.de/img/plan/tiles/map/139_102/" }) { //8_1487
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex)
                        + "_" + MapTileIndex.getX(pMapTileIndex)
                        + "_" + MapTileIndex.getY(pMapTileIndex)
                        + mImageFilenameEnding;
            }
        });

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET
        });
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);


        map.setHorizontalMapRepetitionEnabled(false);
        map.setVerticalMapRepetitionEnabled(false);
        map.setTileSystem(new TileSys(1600/100*128, 1600/100*128));
        IMapController mapController = map.getController();
        mapController.setZoom(2.0);



        Log.d("XXX", "BB -> " + map.getProjection().getBoundingBox().toString());

        new getSeats().execute();


        MapEventsReceiver mReceive = new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                boolean sitz = false;
                String s = "";
                for(Seat seat : seatList){
                    if((p.getLongitude() >= Double.parseDouble(seat.getX()))
                            && (p.getLongitude() <= Double.parseDouble(seat.getX())+16)
                            && (p.getLatitude() >= Double.parseDouble(seat.getY()))
                            && (p.getLatitude() <= Double.parseDouble(seat.getY())+16)

                    ) {
                        sitz = true;
                        s = seat.getB2() + " " + seat.getB3() + "[" + seat.getX() + "/" + seat.getY()+"]";
                    }
                }
                Log.d("XXX", "POSITION -> " + p.getLongitude() + " " + p.getLatitude());
                if(sitz){
                    Log.d("XXX", "SEAT -> " + s + " " + p.getLongitude() + " " + p.getLatitude());
                }else{
                    Log.d("XXX", "SEAT -> Kein Sitz" );
                }

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }

        };



        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);

        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    class getSeats extends AsyncTask<String, String, String>{
        JSONArray jsonArray;
        @Override
        protected String doInBackground(String... uri) {
            String responseString = null;
            try {
                URL url = new URL("http://sale.demo.bagarino.de/data2.txt");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){

                    String result = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                            .lines().collect(Collectors.joining("\n"));

                    jsonArray = new JSONArray(result);
                    Log.d("XXX", "Alles läuft -> " + jsonArray.get(1));
                }
                else {
                    responseString = "FAILED"; // See documentation for more info on response handling
                    Log.d("XXX", "ERROR");
                }
            } catch (IOException | JSONException e) {
                //TODO Handle problems..
            }
            return responseString;
        }
        Marker m;
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            seatList = new ArrayList<>();
            m = new Marker(map);
            for (int i=0; i < jsonArray.length(); i++) {
                try {
                    seatList.add(new Seat(
                            jsonArray.getJSONObject(i).get("i").toString(),
                            jsonArray.getJSONObject(i).get("b1").toString(),
                            jsonArray.getJSONObject(i).get("b2").toString(),
                            jsonArray.getJSONObject(i).get("b3").toString(),
                            jsonArray.getJSONObject(i).get("x").toString(),
                            jsonArray.getJSONObject(i).get("y").toString(),
                            jsonArray.getJSONObject(i).get("s").toString(),
                            jsonArray.getJSONObject(i).get("p").toString(),
                            jsonArray.getJSONObject(i).get("k").toString(),
                            ""
                    ));
                    setMarker(Double.parseDouble(jsonArray.getJSONObject(i).get("y").toString()), Double.parseDouble(jsonArray.getJSONObject(i).get("x").toString()), jsonArray.getJSONObject(i).get("s").toString());
                    Log.d("JSONOBJECT", "Y=" + jsonArray.getJSONObject(i).get("y").toString() + " x=" + jsonArray.getJSONObject(i).get("x").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setMarker(double y, double x, String s){

            GroundOverlay myGroundOverlay = new GroundOverlay();

            myGroundOverlay.setPosition(new GeoPoint(y , x), new GeoPoint(y+16 , x  +16));
            switch(s){
                case "0":
                    myGroundOverlay.setImage(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.platzfrei));
                    break;
                case "1":
                    myGroundOverlay.setImage(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.platztemp));
                    break;
                case "-1":
                    myGroundOverlay.setImage(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.platztempfremd));
                    break;
                case "14":
                    myGroundOverlay.setImage(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.platzblocked));
                    break;
                default:
                    myGroundOverlay.setImage(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.platznichtbuchbar));
                    break;
            }

            map.getOverlays().add(myGroundOverlay);





        }
    }

}
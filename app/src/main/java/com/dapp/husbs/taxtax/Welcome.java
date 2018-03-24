package com.dapp.husbs.taxtax;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dapp.husbs.taxtax.Common.Common;
import com.dapp.husbs.taxtax.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlacesOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import android.location.LocationListener;

public class Welcome extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;
//    Play service
    private static final int MY_PERMISSION_REQUEST_CODE  = 7000 ;
    private static final int PLAY_SERVIC_RES_REQUEST = 7001;

    private LocationRequest mlocationRequest ;
    private GoogleApiClient mGoogleApiClient ;
    private Location mLastLocation    ;
    private static int UPDATE_INTERVAL = 5000 ;
    private static int FATEST_INTERVAL =3000 ;
    private static int DISOKACEMENT = 10;

    DatabaseReference drivers ;
    GeoFire geoFire ;
    Marker mCurrent  ;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment ;

//    Car animation
    private List<LatLng> polyLineList ;
    private Marker carMarker ;
    private Marker pickupLocationMarker ;
    private float v ;
    private double lat,lng ;
    private Handler handler ;
    private LatLng startPosition,endPosition, currentPosition ;
    private int index,next ;
    private Button btnGo  ;
    private EditText edtPlace ;
    private String destintaion  ;
    private PolylineOptions polylineOptions,blackPolyLineOptions ;
    private Polyline blackPolyline,greyPolyLine ;
    private IGoogleAPI mService ;
    Runnable drawPathrunnble = new Runnable() {
        @Override
        public void run() {
            if(index<polyLineList.size()-1){
                index++ ;
                next= index+1 ;
            }
            if (index < polyLineList.size()-1){
                startPosition = polyLineList.get(index) ;
                endPosition = polyLineList.get(next) ;

            }
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1) ;
            valueAnimator.setDuration(3000) ;
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                        v= valueAnimator.getAnimatedFraction();
                        lng = v*endPosition.longitude+(1-v)*startPosition.longitude ;
                        lat  = v*endPosition.latitude+(1-v)*startPosition.latitude ;
                    LatLng newPos = new LatLng(lat,lng) ;
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                            .target(newPos)
                            .zoom(15.5f)
                            .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    } ;

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat= Math.abs(startPosition.latitude - endPosition.longitude) ;
        double lng= Math.abs(startPosition.longitude - endPosition.longitude) ;

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude){
            return (float)(Math.toDegrees(Math.atan(lng/lat))) ;}
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude){
            return (float)((90-Math.toDegrees(Math.atan(lng/lat))))+90;}
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude){
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180) ;}
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude){
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat))+180)+270 );}
    return -1 ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();

//        init View
        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline){
                    startLocationUpdate();
                    displayLcation() ;
                    Snackbar.make(mapFragment.getView(),"You Are Online",Snackbar.LENGTH_SHORT).show();

                }else{
                    stopLocationUpdate() ;
                    mCurrent.remove();
                    mMap.clear() ;
                    handler.removeCallbacks(drawPathrunnble);
                    Snackbar.make(mapFragment.getView(),"You Are Offline",Snackbar.LENGTH_SHORT).show();


                }
            }
        });

        polyLineList = new ArrayList<>();
        btnGo = (Button) findViewById(R.id.btnGo) ;
        edtPlace = (EditText) findViewById(R.id.editPlace) ;
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             destintaion = edtPlace.getText().toString() ;
             destintaion = destintaion.replace(" ", "+") ;
             Log.d("EDMTDEV",destintaion) ;
             getDirection();
            }
        });
//        Geo Fire
        drivers = FirebaseDatabase.getInstance().getReference("Drivers") ;
        geoFire = new GeoFire(drivers) ;

        setUpdateLocation() ;
        mService = Common.getGoogleAPI();
    }

    private void getDirection() {
        currentPosition = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()) ;
        String requestApi = null ;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
            "mode=driving&"+
                "transit_routing_preference=less_driving&"+
            "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
            "destination="+destintaion+"&"+
            "key="+getResources().getString(R.string.google_direction_api) ;

            Log.d("Direction :", " Current Position: "+currentPosition.latitude + " destination "+ destintaion) ;

/*
* https://maps.googleapis.com/maps/api/directions/json?origin=75+9th+Ave+New+York,
* +NY&destination=MetLife+Stadium+1+MetLife+Stadium+Dr+East+Rutherford,+NJ+07073&key
*
* */
            Log.d("EDMTDEV", requestApi) ;  // print URL debug
            mService.getPath(requestApi)
            .enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString()) ;
                        Log.d("taaaaaaaaag","this is ths object: "+ jsonObject);
                        JSONObject jsonArray = jsonObject.getJSONObject("routes") ;
                        for (int i =0 ; i<jsonArray.length() ; i++){
//                            TODO: "i" should be int
                            JSONObject route = jsonArray.getJSONObject(String.valueOf(i));
                            JSONObject poly = route.getJSONObject("overview_polyline") ;
                            String polyline =  poly.getString("point") ;
                            polyLineList = decodePoly(polyline);
                        }
//                    Adjusting bounds
                        LatLngBounds.Builder  builder = new LatLngBounds.Builder() ;
                        for (LatLng latLng:polyLineList){
                            builder.include(latLng) ;
                        }
                        LatLngBounds bounds = builder.build() ;
                        CameraUpdate mCameraUpdate  = CameraUpdateFactory.newLatLngBounds(bounds,2) ;
                        polylineOptions = new PolylineOptions() ;
                        polylineOptions.color(Color.GRAY) ;
                        polylineOptions.width(5) ;
                        polylineOptions.startCap(new SquareCap()) ;
                        polylineOptions.endCap(new SquareCap()) ;
                        polylineOptions.jointType(JointType.ROUND) ;
                        polylineOptions.addAll(polyLineList) ;
                        greyPolyLine = mMap.addPolyline(polylineOptions) ;


                        blackPolyLineOptions = new PolylineOptions() ;
                        blackPolyLineOptions.color(Color.BLACK) ;
                        blackPolyLineOptions.width(5) ;
                        blackPolyLineOptions.startCap(new SquareCap()) ;
                        blackPolyLineOptions.endCap(new SquareCap()) ;
                        blackPolyLineOptions.jointType(JointType.ROUND) ;
                        blackPolyline = mMap.addPolyline(blackPolyLineOptions) ;

                        mMap.addMarker(new MarkerOptions()
                                .position(polyLineList.get(polyLineList.size()-1))
                        .title("pickup Location")) ;

//                        Animation
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100) ;
                        polyLineAnimator.setDuration(2000) ;
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                List<LatLng> points = greyPolyLine.getPoints()  ;
                                int precentValue = (int) animation.getAnimatedValue() ;
                                int size  = points.size() ;
                                int newPoints = (int)(size*(precentValue/100.0f)) ;
                                List<LatLng> p = points.subList(0,newPoints) ;
                                blackPolyline.setPoints(p);
                            }
                        });
                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))) ;
                        handler = new Handler() ;
                        index = -1 ;
                        next = 1 ;
                        handler.postDelayed(drawPathrunnble,3000) ;

                        polyLineAnimator.start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(Welcome.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            ;

        }catch (Exception e ){
            e.printStackTrace();
        }
    }


    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case    MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){
                        buildGoogleApiClient() ;
                        createLocationRequest() ;
                        if (location_switch.isChecked()){
                            displayLcation();
                        }
                }
        }
    }
    }

    private void setUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
//            request runtime premission
            ActivityCompat.requestPermissions(this ,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                },MY_PERMISSION_REQUEST_CODE);
        }
        else {
            if (checkPlayServices()){
                buildGoogleApiClient() ;
                createLocationRequest() ;
                if (location_switch.isChecked()){
                    displayLcation();
                }
            }

        }

    }

    private void createLocationRequest() {
        mlocationRequest = new LocationRequest() ;
        mlocationRequest.setInterval(UPDATE_INTERVAL) ;
        mlocationRequest.setFastestInterval(FATEST_INTERVAL) ;
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) ;
        mlocationRequest.setSmallestDisplacement(DISOKACEMENT) ;

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode  = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ;
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVIC_RES_REQUEST).show() ;
            else{
                Toast.makeText(this, "This device not supported ", Toast.LENGTH_SHORT).show();
                    finish();
        }return false  ; }return  true ;
    }

    private void stopLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
//        Deprecated
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this) ;


    }

    private void displayLcation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) ;
        if (mLastLocation !=null){
            if (location_switch.isChecked()){
                final double latitude = mLastLocation.getLatitude() ;
                final double longitude = mLastLocation.getLongitude() ;

//                Update to firebase
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {


                    @Override
                    public void onComplete(String key, DatabaseError error) {


                        //                Add Marker
                        if (mCurrent == null){
                            mCurrent = mMap.addMarker(new MarkerOptions()
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                                        .position(new LatLng(latitude,longitude))
                                                        .title("Your location"));

//                                            Move camera to this position
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));


                        }else if(mCurrent != null){
                         //   mCurrent.remove(); // remove already marker
                        }
                    }
                });
            }
        }
        else{
            Log.d("Error","Cannot get your location ") ;

        }
    }

    private void roteteMarker( final  Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler() ;
        final long start = SystemClock.uptimeMillis() ;
        final float startRotation   = mCurrent.getRotation() ;
        final long duration  = 1500 ;
        final Interpolator interpolator  =  new LinearInterpolator() ;
        handler.post(new Runnable() {
            @Override
            public void run() {
             long elapsed = SystemClock.uptimeMillis() - start ;
             float t = interpolator.getInterpolation((float)elapsed/duration) ;
             float rot  = t*i+(1-t)*startRotation ;
                mCurrent.setRotation(-rot > 180?rot/2:rot);
                if (t<1.0) {
                    handler.postDelayed(this,16) ;
                }
            }
        }) ;
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
         return;
        }
//        Deprecated
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mlocationRequest,this) ;


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL) ;
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

//        #add
        mCurrent = null ;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location ;
        displayLcation();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLcation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

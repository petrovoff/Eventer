package com.example.eventer2.GoogleMapAndPlaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.example.eventer2.activities.NewEventActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener {


    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUESTED_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71,136));

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mPlacePicker;
    private ImageView mChangeMap;
    private ImageView mSearchClear;
    private ImageView mTerrain, mSatellite, mNormal;
    private Button mSaveLocation;
    private CardView mChangeCard;

    //vars
    private ApplicationData mData;
    private Boolean changeOpen = false;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    private PlaceInfo placeInfo;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mData = (ApplicationData) getApplication();
        mSearchText = findViewById(R.id.map_input_search);
        mPlacePicker = findViewById(R.id.map_place_picker);
        mChangeMap = findViewById(R.id.map_change);
        mTerrain = findViewById(R.id.map_terrain_btn);
        mSatellite = findViewById(R.id.map_satellite_btn);
        mNormal = findViewById(R.id.map_normal_btn);
        mSaveLocation = findViewById(R.id.map_save);
        mChangeCard = findViewById(R.id.map_change_card);
        mSearchClear = findViewById(R.id.map_search_clear);


        mSearchClear.setOnClickListener(v -> {
            mSearchText.setText("");
        });


        getLocationPermission();

        mChangeMap.setOnClickListener(v -> {
            if(!changeOpen){
                mChangeCard.setVisibility(View.VISIBLE);
                changeOpen = true;
            }else {
                mChangeCard.setVisibility(View.INVISIBLE);
                changeOpen = false;
            }
        });

        mSaveLocation.setOnClickListener(v -> {
            onSaveBtn();
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        mTerrain.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        });
        mSatellite.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        });
        mNormal.setOnClickListener(v -> {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        });

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            init();
        }
    }

    private void init(){

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this,this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null );

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnEditorActionListener((v, actionId, keyEvent) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                //execute our method for searching
                geoLocate();
            }
            return false;
        });


        mMap.setOnInfoWindowClickListener(marker -> {
            mLocation = marker.getSnippet();
            mSearchText.setText(mLocation);
        });



        mPlacePicker.setOnClickListener(v -> {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e(TAG, "onClick: GooglePlayServicesRepairableException" + e.getMessage());
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException" + e.getMessage());
            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(this,data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            }
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

        try{
            if(mLocationPermissionsGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.wtf(TAG, task.getResult().toString());

                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");

                    }
                });

            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this)); //pozivamo windowadapter
        if(placeInfo != null){
            try {
                String snippet = placeInfo.getName() + "\n"
                        + placeInfo.getAddress() + "\n"
                        + placeInfo.getPhoneNumber() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);

                mMap.addMarker(options);
            }catch(NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage());
            }
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );

        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //prikazivanje opisa na lokaciju
        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);

            mMarker = mMap.addMarker(options);
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geoLocating");

        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString,10);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }
        if(list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location" + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            mLocation = address.getAddressLine(0);
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUESTED_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUESTED_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUESTED_CODE:{
                if(grantResults.length > 0 ){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void onSaveBtn(){

        if(mLocation!= null) {
            String location = mLocation.trim();
            Toast.makeText(this, "Location:" + mLocation, Toast.LENGTH_SHORT).show();

            mData.setEventLocation(location);
        }else {
            Toast.makeText(this, "Error.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();

        double lat = latLng.latitude;
        double lng = latLng.longitude;

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this,Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            String address = addresses.get(0).getAddressLine(0);

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title("Custom Location")
                    .snippet(address);
            mMarker = mMap.addMarker(options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---------------Api places---------

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                placeInfo = new PlaceInfo();
                placeInfo.setName(place.getName().toString());
                placeInfo.setAddress(place.getAddress().toString());
//                mPlace.setAttributions(place.getAttributions().toString());
                placeInfo.setId(place.getId());
                placeInfo.setLatlang(place.getLatLng());
                placeInfo.setPhoneNumber(place.getPhoneNumber().toString());
                placeInfo.setRating(place.getRating());
                placeInfo.setWebsiteUri(place.getWebsiteUri());

                Log.d(TAG, "onResult: place details:" + placeInfo.getAttributions());

            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude),DEFAULT_ZOOM, placeInfo);
            places.release();



        }
    };

}

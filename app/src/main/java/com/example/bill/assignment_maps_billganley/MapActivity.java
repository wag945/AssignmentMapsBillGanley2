package com.example.bill.assignment_maps_billganley;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String LOG_MAP = "GOOGLE_MAPS";
    private DatabaseReference dbRef = null;
    private ArrayList<MapLocation> mapLocations = null;

    // Google Maps
    private LatLng currentLatLng;
    private MapFragment mapFragment;
    private Marker currentMapMarker;
    private GoogleMap myGoogleMap;

    // Broadcast Receiver
    private IntentFilter intentFilter = null;
    private BroadcastReceiverMap broadcastReceiverMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        // Instantiating a new IntentFilter to support BroadcastReceivers
        intentFilter = new IntentFilter("com.example.bill.assignment_maps_billganley.NEW_MAP_LOCATION_BROADCAST");
        broadcastReceiverMap = new BroadcastReceiverMap();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register the Broadcast Receiver.
        registerReceiver(broadcastReceiverMap, intentFilter);
    }

    @Override
    protected void onStop() {
        // Unregister the Broadcast Receiver
        unregisterReceiver(broadcastReceiverMap);
        super.onStop();



    }

    // Step 1 - Set up initial configuration for the map.
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Intent intent = getIntent();
        Double latiude = intent.getDoubleExtra("LATITUDE", Double.NaN);
        Double longitude = intent.getDoubleExtra("LONGITUDE", Double.NaN);
        String location = intent.getStringExtra("LOCATION");
        //Save the GoogleMap reference
        myGoogleMap = googleMap;

        // Set initial positionning (Latitude / longitude)
        currentLatLng = new LatLng(latiude, longitude);

        googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title(location)
        );

        // Set the camera focus on the current LatLtn object, and other map properties.
        mapCameraConfiguration(googleMap);
        useMapClickListener(googleMap);
        useMarkerClickListener(googleMap);
        createMarkersFromFirebase(googleMap);

        //Implement two different listeners as part of the lab assignment
        useMapLongClickListener(googleMap);
        useOnCameraIdleListener(googleMap);
    }

    /** Step 2 - Set a few properties for the map when it is ready to be displayed.
     Zoom position varies from 2 to 21.
     Camera position implements a builder pattern, which allows to customize the view.
     Bearing - screen rotation ( the angulation needs to be defined ).
     Tilt - screen inclination ( the angulation needs to be defined ).
     **/
    private void mapCameraConfiguration(GoogleMap googleMap){

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLatLng)
                .zoom(8)
                .bearing(0)
                .build();

        // Camera that makes reference to the maps view
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                Log.i(LOG_MAP, "googleMap.animateCamera:onFinish is active");
            }

            @Override
            public void onCancel() {
                Log.i(LOG_MAP, "googleMap.animateCamera:onCancel is active");
            }});
    }

    /** Step 3 - Reusable code
     This method is called everytime the use wants to place a new marker on the map. **/
    private void createCustomMapMarkers(GoogleMap googleMap, LatLng latlng, String title, String snippet){

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng) // coordinates
                .title(title) // location name
                .snippet(snippet); // location description

        // Update the global variable (currentMapMarker)
        currentMapMarker = googleMap.addMarker(markerOptions);

        triggerBroadcastMessageFromFirebase(latlng,title);

    }

    private void triggerBroadcastMessageFromFirebase(LatLng latlng, String title) {
        // Broadcast Receiver
        Intent explicitIntent = new Intent(this, BroadcastReceiverMap.class);
        explicitIntent.putExtra("LATITUDE", latlng.latitude);
        explicitIntent.putExtra("LONGITUDE", latlng.longitude);
        explicitIntent.putExtra("LOCATION", title);

        sendBroadcast(explicitIntent);
    }

    // Step 4 - Define a new marker based on a Map click (uses onMapClickListener)
    private void useMapClickListener(final GoogleMap googleMap){

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latltn) {
                Log.i(LOG_MAP, "setOnMapClickListener");

                if(currentMapMarker != null){
                    // Remove current marker from the map.
                    currentMapMarker.remove();
                }
                // The current marker is updated with the new position based on the click.
                createCustomMapMarkers(
                        googleMap,
                        new LatLng(latltn.latitude, latltn.longitude),
                        "New Marker",
                        "Listener onMapClick - new position"
                                +"lat: "+latltn.latitude
                                +" lng: "+ latltn.longitude);
            }
        });
    }

    // Step 5 - Use OnMarkerClickListener for displaying information about the MapLocation
    private void useMarkerClickListener(GoogleMap googleMap){
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            // If FALSE, when the map should have the standard behavior (based on the android framework)
            // When the marker is clicked, it wil focus / centralize on the specific point on the map
            // and show the InfoWindow. IF TRUE, a new behavior needs to be specified in the source code.
            // However, you are not required to change the behavior for this method.
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(LOG_MAP, "setOnMarkerClickListener");

                return false;
            }
        });
    }

    public void createMarkersFromFirebase(GoogleMap googleMap){
        // FIXME Call loadData() to gather all MapLocation instances from firebase.
        firebaseLoadData(googleMap);
        // FIXME Call createCustomMapMarkers for each MapLocation in the Collection
        //createCustomMapMarkers called from firebaseLoadData after data is retrieved from firebase
    }

    void firebaseLoadData(GoogleMap googleMap) {
        dbRef = FirebaseDatabase.getInstance().getReference();

        mapLocations = new ArrayList<>();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("MainActivity","There are " + dataSnapshot.getChildrenCount() + " items");
                for (DataSnapshot locationSnapshot: dataSnapshot.getChildren()) {
                    String location = locationSnapshot.child("location").getValue(String.class);
                    Log.d("MainActivity","location = " + location);
                    Double latitude = locationSnapshot.child("latitude").getValue(Double.class);
                    Log.d("MainActivity","latitude = " + latitude);
                    Double longitude = locationSnapshot.child("longitude").getValue(Double.class);
                    Log.d("MainActivity","longitude = " + longitude);
                    mapLocations.add(new MapLocation(location,"", String.valueOf(latitude), String.valueOf(longitude)));
                }

                //Loop through MapLocation array retrieved from firebase
                for (int i = 0; i < mapLocations.size(); i++) {
                    MapLocation mapLocation = mapLocations.get(i);

                    LatLng latLng = new LatLng( Double.parseDouble(mapLocation.getLatitude()),
                            Double.parseDouble(mapLocation.getLongitude()));

                    //Draw a marker for each MapLocation from firebase
                    createCustomMapMarkers(myGoogleMap,latLng,mapLocation.getTitle(),mapLocation.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this,
                        "onCanceled error",
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void useMapLongClickListener(final GoogleMap googleMap){

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latltn) {
                Log.i(LOG_MAP, "onMapLongClick");

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latltn)
                        .zoom(6)
                        .bearing(0)
                        .build();

                // Camera that makes reference to the maps view
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

                googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() {

                    @Override
                    public void onFinish() {
                        Log.i(LOG_MAP, "googleMap.animateCamera:onFinish is active");
                    }

                    @Override
                    public void onCancel() {
                        Log.i(LOG_MAP, "googleMap.animateCamera:onCancel is active");
                    }});
            }
        });
    }

    public void useOnCameraIdleListener(GoogleMap googleMap) {
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Toast.makeText(MapActivity.this,"camera is idle",Toast.LENGTH_SHORT).show();
            }
        });
    }
}

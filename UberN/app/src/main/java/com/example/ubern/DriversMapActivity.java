package com.example.ubern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ubern.databinding.ActivityDriversMapBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import java.util.Objects;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private DatabaseReference AssignedCustomerRef,AssignedCustomerPickUpRef;
    private String driverID,customerID="";
    Button btnDriverSetting;
    Button btnDriverLogout;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Marker PickUpMaker;
    private boolean logoutStatus=false;

    private ValueEventListener AssignedCustomerPickUpRefListner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDriver);
        mapFragment.getMapAsync(this);

        btnDriverLogout=findViewById(R.id.btnDriverLogout);
        btnDriverSetting=findViewById(R.id.btnDriverSetting);

        mAuth=FirebaseAuth.getInstance();

        currentUser=mAuth.getCurrentUser();

        driverID = mAuth.getCurrentUser().getUid();




        btnDriverSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriversMapActivity.this, SettingActivity.class);
                intent.putExtra("type","Drivers");
                startActivity(intent);
            }
        });

        btnDriverLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutStatus = true;
                DisconnectTheDriver();
                mAuth.signOut();
                LogoutDriver();
            }
        });

        GetAssignedCustomerRequest();
    }

    private void GetAssignedCustomerRequest() {
        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference
                ("Users/Drivers/" + driverID + "/CustomerRideID");

        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    customerID = snapshot.getValue(String.class);
                    if(customerID!=null){
                        GetAssignedCustomerPickUpLocation();
                    }
                }
                else{
                    customerID="";
                    if (PickUpMaker !=null){
                        PickUpMaker.remove();
                    }
                    if(AssignedCustomerPickUpRefListner !=null ){
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListner);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetAssignedCustomerPickUpLocation() {
        AssignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference
                ("Customer Requests/" + customerID + "/1");

        AssignedCustomerPickUpRefListner = AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<Object> CustomerLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    if(CustomerLocationMap.get(0)!=null){
                        LocationLat = Double.parseDouble(CustomerLocationMap.get(0).toString());
                    }
                    if(CustomerLocationMap.get(1)!=null){
                        LocationLng = Double.parseDouble(CustomerLocationMap.get(1).toString());
                    }
                    LatLng DriverLatLng = new LatLng(LocationLat,LocationLng);
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Customer Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LogoutDriver() {
        Intent welcomIntent=new Intent(DriversMapActivity.this,WelcomeActivity.class);
        welcomIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomIntent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(getApplicationContext() !=null){

            lastLocation=location;

            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

            String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference DriverAvailRef= FirebaseDatabase.getInstance().getReference("Drivers Available");
            GeoFire geoFireAvailability=new GeoFire(DriverAvailRef);

            DatabaseReference DriverWorkingRef =  FirebaseDatabase.getInstance().getReference("Drivers Working");
            GeoFire geoFireWorking=new GeoFire( DriverWorkingRef);

            switch (customerID){
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;

                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;
            }
        }
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!logoutStatus){
            DisconnectTheDriver();
        }
    }

    private void DisconnectTheDriver() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userID = user.getUid();
            DatabaseReference DriverAvailRef = FirebaseDatabase.getInstance().getReference("Drivers Available");
            GeoFire geoFire = new GeoFire(DriverAvailRef);
            geoFire.removeLocation(userID);
        }
    }
}
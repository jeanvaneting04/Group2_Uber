package com.example.ubern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ubern.databinding.ActivityCustomersMapBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private DatabaseReference DriverAvailableRef;
    private  DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;
    Marker DriverMarker,PickupMarker;
    private  int radius =1;
    private Boolean driverFound=false,requestType=false;
    private String  driverFoundID;
    private  ValueEventListener DriverLocationRefListner;
    String customerID;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    LatLng CustomerPickupLocation;
    Button btnCustomerLogout,btnCallCar,btnCustomerSetting;
    DatabaseReference CustomerDataRef;
    GeoQuery geoQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapCustomer);
        mapFragment.getMapAsync(this);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();

        CustomerDataRef= FirebaseDatabase.getInstance().getReference("Customer Requests");
        DriverAvailableRef= FirebaseDatabase.getInstance().getReference("Drivers Available");
        DriverLocationRef=FirebaseDatabase.getInstance().getReference("Drivers Working");

        customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        btnCustomerLogout=findViewById(R.id.btnCustomerLogout);
        btnCustomerSetting=findViewById(R.id.btnCustomerSetting);
        btnCallCar=findViewById(R.id.btnCallCar);

        btnCustomerSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomersMapActivity.this, SettingActivity.class);
                intent.putExtra("type","Customers");
                startActivity(intent);
            }
        });

        btnCustomerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LogoutCustomer();
            }
        });
        btnCallCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestType){
                    requestType=false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListner);

                    if(driverFound !=null){
                        DriverRef = FirebaseDatabase.getInstance().getReference
                                ("Users/Drivers/" + driverFoundID+ "/CustomerRideID");
                        DriverRef.removeValue();
                        driverFoundID=null;
                    }

                    driverFound=false;
                    radius=1;

                    String customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire=new GeoFire(CustomerDataRef);
                    geoFire.removeLocation(customerID);

                    if(PickupMarker !=null){
                        PickupMarker.remove();
                    }
                    if(DriverMarker !=null){
                        DriverMarker.remove();
                    }
                    btnCallCar.setText("Call a Car");

                }
                else{
                    requestType=true;
                    String customerID=FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire=new GeoFire(CustomerDataRef);
                    geoFire.setLocation(customerID,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));

                    CustomerPickupLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(CustomerPickupLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    btnCallCar.setText("Getting your Drivers ......");
                    GetClosestDriver();
                }
            }
        });
    }

    private void GetClosestDriver() {
        GeoFire geoFire=new GeoFire(DriverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickupLocation.latitude,CustomerPickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestType){
                    driverFound =true;
                    driverFoundID=key;

                    DriverRef = FirebaseDatabase.getInstance().getReference("Users/Drivers/" + driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID",customerID);
                    DriverRef.updateChildren(driverMap);

                    GettingDriverLocation();
                    btnCallCar.setText("Looking for driver location");
                }
            }
            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius =radius+1;
                    GetClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private void GettingDriverLocation() {
        DriverLocationRefListner = DriverLocationRef.child(driverFoundID).child("1")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && requestType){
                            List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                            double LocationLat = 0;
                            double LocationLng = 0;
                            btnCallCar.setText("Driver Found");

                            if(driverLocationMap.get(0)!=null){
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if(driverLocationMap.get(1)!=null){
                                LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }
                            LatLng DriverLatLng = new LatLng(LocationLat,LocationLng);
                            if(DriverMarker != null){
                                DriverMarker.remove();
                            }
                            Location location1 = new Location("");
                            location1.setLatitude(CustomerPickupLocation.latitude);
                            location1.setLongitude(CustomerPickupLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLng.latitude);
                            location2.setLongitude(DriverLatLng.longitude);

                            float  Distance = location1.distanceTo(location2);

                            if(Distance<90){
                                btnCallCar.setText("Driver Reached");
                            }
                            else{
                                btnCallCar.setText("Driver Found: " + String.valueOf(Distance));
                            }
                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is here ").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void LogoutCustomer() {
        Intent welcomIntent=new Intent(CustomersMapActivity.this,WelcomeActivity.class);
        welcomIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomIntent);
        finish();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng center = mMap.getCameraPosition().target;
                Log.d("MapDrag", "New center: " + center.latitude + ", " + center.longitude);
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
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
        lastLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
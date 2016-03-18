package pt.ulisboa.tecnico.grupo11.ubibike;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int LOCATION_INTERVAL = 1000;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        // in case we want to check location periodically
      /*  new Thread(new Runnable() {
            public void run() {
                setLastLocationOnMap();
                SystemClock.sleep(LOCATION_INTERVAL);
            }
        }).start();*/

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setLastLocationOnMap();
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    public void onConnected(Bundle bundle) {
        // TODO: fix last location
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            setLastLocationOnMap();
        }

    }

    public void onConnectionSuspended(int i) {

    }

    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onLocationChanged(Location location) {
        setLastLocationOnMap();
    }
    private void setLastLocationOnMap() {
        Log.d("SET_LOCATION", "Map: " + map + "; Location: " + lastLocation);

        if (map != null && lastLocation != null) {

            String latitudeStr = String.valueOf(lastLocation.getLatitude());
            double latitude = Double.parseDouble(latitudeStr);

            String longitudeStr = String.valueOf(lastLocation.getLongitude());
            double longitude = Double.parseDouble(longitudeStr);

            LatLng userPosition = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(userPosition).title("You are here"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 18.0f));

        }
    }
}

package pt.ulisboa.tecnico.grupo11.ubibike;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationListener;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;

public class Tab extends TabActivity implements LocationListener {
    public static final int ACCEPTED = 1;

    public static List<List<String>> trajectories = new ArrayList<>();
    public static String numberOfPoints;
    public static String username;
    public static Hashtable<String, Integer> stations = new Hashtable<>();

    private WifiDirectReceiver mReceiver;
    private Messenger mService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tab);

        TabHost mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Contacts").setContent(new Intent(this, Contacts.class)));
        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("Home").setContent(new Intent(this, Home.class)));
        mTabHost.addTab(mTabHost.newTabSpec("info").setIndicator("Info").setContent(new Intent(this, Info.class)));
        mTabHost.addTab(mTabHost.newTabSpec("stations").setIndicator("Stations").setContent(new Intent(this, Stations.class)));
        mTabHost.setCurrentTab(1);
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());
        new Thread(new Runnable() {
            public void run() {
                fetchInfo(username);
            }
        }).start();

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new WifiDirectReceiver(this);
        registerReceiver(mReceiver, filter);
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                ACCEPTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            ;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCEPTED: {
                if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED/* &&
                        ActivityCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED*/) {
                    Log.d("LOCATION", "Requesting location");
                    LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "LOCATION CHANGED",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS turned on", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void fetchInfo(String username) {
        final String baseUrl = Login.serverUrl + "/users/" + username + "/";

        try {
            URL url = new URL(baseUrl + "points");
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setInstanceFollowRedirects(false);
            httpConnection.setRequestMethod("GET");
            int responseCode = httpConnection.getResponseCode();

            if (responseCode == 200) {
                InputStream inputStream = httpConnection.getInputStream();
                byte[] buffer = new byte[httpConnection.getContentLength()];
                inputStream.read(buffer);
                inputStream.close();
                int points = (int) buffer[0];
                numberOfPoints = new String(String.valueOf(points));
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Tab.this, "Server internal error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            url = new URL(baseUrl + "paths");
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setInstanceFollowRedirects(false);
            httpConnection.setRequestMethod("GET");
            responseCode = httpConnection.getResponseCode();

            if (responseCode == 200) {
                InputStream inputStream = httpConnection.getInputStream();
                byte[] buffer = new byte[httpConnection.getContentLength()];
                inputStream.read(buffer);
                inputStream.close();
                String concatenatedPaths = new String(buffer, "UTF-8");
                String[] paths = concatenatedPaths.split("#");
                for (String path : paths) {
                    String[] coordinates = path.split(";");
                    List<String> coordsList = new ArrayList<String>();
                    coordsList.addAll(Arrays.asList(coordinates));
                    trajectories.add(coordsList);
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Tab.this, "Server internal error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            url = new URL(Login.serverUrl + "/stations");
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setInstanceFollowRedirects(false);
            httpConnection.setRequestMethod("GET");
            responseCode = httpConnection.getResponseCode();

            try {
                if (responseCode == 200) {
                    InputStream inputStream = httpConnection.getInputStream();
                    byte[] buffer = new byte[httpConnection.getContentLength()];
                    inputStream.read(buffer);
                    inputStream.close();
                    String concatenatedStations = new String(buffer, "UTF-8");
                    String[] stationsInfo = concatenatedStations.split(";");
                    for (String stationInfo : stationsInfo) {
                        String[] infoParts = stationInfo.split(":");

                        if (infoParts.length != 2)
                            throw new InputMismatchException();

                        stations.put(infoParts[0], Integer.parseInt(infoParts[1]));
                    }
                } else {
                    throw new NetworkErrorException();
                }
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Tab.this, "Server internal error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CHECK_USER", Log.getStackTraceString(e));
                    Toast.makeText(Tab.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
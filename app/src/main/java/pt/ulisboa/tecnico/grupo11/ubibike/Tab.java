package pt.ulisboa.tecnico.grupo11.ubibike;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class Tab extends TabActivity implements LocationListener {
    public static final int ACCEPTED = 1;

    // user data
    public static List<List<String>> trajectories = new ArrayList<>();
    public static int userPoints = 0;
    public static boolean updatePoints = true;
    public static String username;
    public static Hashtable<String, Integer> stations = new Hashtable<>();
    public static List<Location> currentPath;
    public static Location lastLocation;
    public static Hashtable<String, Boolean> reservations = new Hashtable<>();

    // wifi direct connection data
    public static WifiDirectReceiver mReceiver;
    public static SimWifiP2pManager mManager = null;
    public static SimWifiP2pManager.Channel mChannel = null;
    public static Messenger mService = null;
    public static boolean mBound = false;
    public static SimWifiP2pSocketServer mSrvSocket = null;

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

        new Thread(new Runnable() {
            public void run() {
                fetchInfo(username);
            }
        }).start();


        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());


        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        Intent intent = new Intent(this, SimWifiP2pService.class);

        try {
            unbindService(mConnection);
        } catch (IllegalArgumentException e) {
            ;
        }
        mReceiver = new WifiDirectReceiver(this);
        registerReceiver(mReceiver, filter);

        try {
            bindService(intent, mConnection, Context.MODE_MULTI_PROCESS);
        } catch (Exception e) {
            Log.d("WiFi Direct", e.getMessage(), e);
            Toast.makeText(this, "Initialization error",
                    Toast.LENGTH_SHORT).show();
        }

        // Setup Location manager and receiver
        LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unbindService(mConnection);
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            ;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mConnection);
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
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        if (WifiDirectReceiver.onBike) {

            if (!currentPath.isEmpty()) {
                try {
                    Location lastLocation = currentPath.get(currentPath.size() - 1);
                    int distance = (int)calculateDistance(lastLocation.getLongitude(), lastLocation.getLatitude(), location.getLongitude(), location.getLatitude());
                    userPoints += distance;
                    long dateTime = new Date().getTime();
                    String message = "#P#_#" + Tab.username + "#" + distance + "#" + dateTime;
                    MessageDigest md = null;
                    md = MessageDigest.getInstance("SHA-256");
                    md.update(message.getBytes(Charset.forName("UTF-8")));
                    byte[] messageHash = md.digest();
                    String hash = new String(messageHash);
                    message += "#" + hash;
                    Contacts.madeTransactions += message +";;;";
                    Tab.updatePoints = true;
                    new Thread(new Runnable() {
                        public void run() {
                            while(true) {
                                try {
                                    URL url = new URL(Login.serverUrl + "/transactions");
                                    HttpURLConnection createUserConn = (HttpURLConnection) url.openConnection();
                                    createUserConn.setDoOutput(true);
                                    createUserConn.setRequestMethod("PUT");
                                    byte[] updatedData = Contacts.madeTransactions.getBytes("UTF-8");
                                    DataOutputStream wr = new DataOutputStream(createUserConn.getOutputStream());
                                    wr.write(updatedData);
                                    wr.close();

                                    int responseCode = createUserConn.getResponseCode();
                                    if (responseCode == 200)
                                        return;
                                    else
                                        Thread.sleep(5000);
                                } catch(IOException | InterruptedException  e) {
                                    Log.e("TRANSACTIONS", "IOException", e);
                                }
                            }
                        }
                    }).start();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            currentPath.add(location);
        }

        lastLocation = location;
    }

    //  Returns the distance between the two coordinates in meters
    public static double calculateDistance(double longitude1, double latitude1, double longitude2, double latitude2) {

        //convert decimal degrees to radians
        longitude1 = Math.toRadians(longitude1);
        latitude1 = Math.toRadians(latitude1);
        longitude2 = Math.toRadians(longitude2);
        latitude2 = Math.toRadians(latitude2);

        // haversine formula
        double dlon = longitude2 - longitude1;
        double dlat = latitude2 - latitude1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371000; //Radius of earth in meters
        return (c * r);
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

                String stringPoints = new String(buffer, "UTF-8");
                userPoints = Integer.parseInt(stringPoints);
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
            } else if (responseCode == 404) {
                ;
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Tab.this, "Server internal error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            for (List<String> trajectory : trajectories) {
                for (String point : trajectory) {
                    Log.d("TAB", point);
                }
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
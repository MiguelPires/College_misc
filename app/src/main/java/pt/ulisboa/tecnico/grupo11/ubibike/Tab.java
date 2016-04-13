package pt.ulisboa.tecnico.grupo11.ubibike;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class Tab extends TabActivity {

    private WifiDirectReceiver mReceiver;
    private Messenger mService = null;
    public static List<List<String>> listOfTrajectories = new ArrayList<>();
    public static String numberOfPoints;
    public static String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tab);

        TabHost mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Contacts").setContent(new Intent(this, Contacts.class)));
        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("Home").setContent(new Intent(this, Home.class)));
        mTabHost.addTab(mTabHost.newTabSpec("info").setIndicator("Info").setContent(new Intent(this, Info.class)));
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
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
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

    private void fetchInfo (String username) {
        final String url = Login.serverUrl + "/users/" + username + "/";

        try {
            URL usersUrl = new URL(url + "points");
            HttpURLConnection httpConnection = (HttpURLConnection) usersUrl.openConnection();
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
            }

            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Tab.this, "Server internal error.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            usersUrl = new URL(url + "paths");
            httpConnection = (HttpURLConnection) usersUrl.openConnection();
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
                    listOfTrajectories.add(coordsList);
                }
            }

            else {
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

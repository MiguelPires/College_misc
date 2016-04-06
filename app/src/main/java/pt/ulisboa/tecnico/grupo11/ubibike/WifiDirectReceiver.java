package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class WifiDirectReceiver extends BroadcastReceiver {

    private Tab mActivity;
    private MessageReceiver receiver;

    public WifiDirectReceiver(Tab activity) {
        super();
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (receiver == null) {
            receiver = new MessageReceiver(context);
            receiver.executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
        }

        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the Termite service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "WiFi Direct enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, "WiFi Direct disabled",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

            Toast.makeText(mActivity, "Peer list changed",
                    Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mActivity, "Network membership changed",
                    Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mActivity, "Group ownership changed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class MessageReceiver extends AsyncTask<Void, String, Void> {
        private SimWifiP2pSocketServer mSrvSocket = null;
        private Context context;

        MessageReceiver(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d("WiFi Direct", "MessageReceiver started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(Integer.parseInt(context.getString(R.string.port)));
            } catch (IOException e) {
                Log.d("WiFi Direct", "Error - "+e.getMessage());
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    try {
                        BufferedReader sockIn = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()));
                        String st = sockIn.readLine();
                        publishProgress(st);
                        sock.getOutputStream().write(("\n").getBytes());
                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                    } finally {
                        sock.close();
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length < 1)
                return;

            String messageType = values[0].substring(0, 2);
            String message = values[0].substring(2);
            String sender =message.substring(message.indexOf("#")+1, message.lastIndexOf("#"));
            message = message.substring(message.lastIndexOf("#")+1);

            switch (messageType) {
                case "#M":
                    Toast.makeText(mActivity, "Received message '" + message+"' from "+sender,
                            Toast.LENGTH_LONG).show();
                    break;

                case "#P":
                    Toast.makeText(mActivity, "Received "+message+" points from "+sender,
                            Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }
}


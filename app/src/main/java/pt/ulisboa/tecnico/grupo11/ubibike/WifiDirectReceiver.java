package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class WifiDirectReceiver extends BroadcastReceiver implements SimWifiP2pManager.GroupInfoListener {

    private Tab mActivity;
    private MessageReceiver receiver;
    public static boolean onStation = false;
    public static boolean onBike = false;

    public WifiDirectReceiver(Tab activity) {
        super();
        Log.d("WiFi Direct", "Receiver created");
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
                Log.d("WIFI-DIRECT", "WiFi Direct enabled");
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
            mActivity.mManager.requestGroupInfo(mActivity.mChannel, WifiDirectReceiver.this);

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(mActivity, "Group ownership changed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList, SimWifiP2pInfo simWifiP2pInfo) {
        try {
            for (String deviceName : simWifiP2pInfo.getDevicesInNetwork()) {
                if (deviceName.startsWith("Bike")) {
                    if (!onBike) {
                        Tab.currentPath = new ArrayList<Location>();
                        onBike = true;
                        Home.statusTxt.setText("Riding");
                        Home.circleColor = Color.GREEN;
                        Home.circleView.setCircleColorGreen();
                    }
                    return;
                }
            }

            if (onBike) {
                onBike = false;
                Home.statusTxt.setText("Idle");
                Home.circleColor = Color.BLUE;
                Home.circleView.setCircleColorBlue();

                if (Tab.currentPath != null && !Tab.currentPath.isEmpty()) {
                    List<String> coordsList = new ArrayList<String>();
                    String joinedString = "";
                    for (Location loc : Tab.currentPath) {
                        String coordinate = loc.getLatitude() + "," + loc.getLongitude();
                        joinedString += coordinate + ";";
                        coordsList.add(coordinate);
                    }

                    Tab.trajectories.add(coordsList);
                    final String sendPath = joinedString.substring(0, joinedString.length() - 1);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    System.out.println("Uploading points to server");
                                    String pointsUrl = Login.serverUrl + "/users/" + Tab.username + "/points/" + Tab.userPoints;
                                    URL url = new URL(pointsUrl);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setDoOutput(true);
                                    conn.setRequestMethod("PUT");
                                    conn.getInputStream();

                                    System.out.println("Uploading new path to server");
                                    String pathUrl = Login.serverUrl + "/users/" + Tab.username + "/path";
                                    url = new URL(pathUrl);
                                    conn = (HttpURLConnection) url.openConnection();
                                    conn.setDoOutput(true);
                                    conn.setRequestMethod("PUT");

                                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                                    wr.write(sendPath.getBytes());
                                    wr.close();
                                    conn.getInputStream();
                                    System.out.print("Finished uploading: " + sendPath);
                                    return;
                                } catch (Exception e) {
                                    Log.e("Upload path", e.getMessage(), e);
                                    try {
                                        Thread.sleep(5000);
                                    } catch (Exception e1) {
                                        Log.e("Upload path", e1.getMessage(), e1);
                                    }
                                }
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            Log.e("Upload path", e.getMessage(), e);
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
                if (mSrvSocket == null)
                    mSrvSocket = new SimWifiP2pSocketServer(Integer.parseInt(context.getString(R.string.port)));
            } catch (IOException e) {
                Log.d("WiFi Direct", "Error - " + e.getMessage());
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    try {
                        Log.d("RECEIVER", "Receiving message");

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

            try {
                String message = values[0];
                if (message.startsWith("#M", 1)) {
                    final String sender = message.substring(message.indexOf("#", 3) + 1, message.lastIndexOf("#"));
                    final String parseMessage = message.substring(message.lastIndexOf("#") + 1);
                    Toast.makeText(mActivity, "Received message '" + parseMessage + "' from " + sender,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                byte[] decodedMessage = Base64.decode(message, Base64.DEFAULT);
                final String originalMessage = new String(decodedMessage, "UTF-8");

                if (originalMessage.startsWith("#P", 1)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String sender = originalMessage.substring(originalMessage.indexOf("#", 3) + 1, originalMessage.lastIndexOf("#"));
                            String parsedMessage = parseSignedMessage(originalMessage, sender);
                            if (parsedMessage != null) {
                                String message = parsedMessage.substring(2);
                                final String stringPoints = message.substring(message.lastIndexOf("#") + 1);

                                int receivedPoints = Integer.parseInt(stringPoints);
                                Tab.userPoints += receivedPoints;
                                Tab.updatePoints = true;
                                if (receivedPoints == 1) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mActivity, "Received " + stringPoints + " point from " + sender,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mActivity, "Received " + stringPoints + " points from " + sender,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                            } else {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mActivity, "Message authentication failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();
                } else {
                    Log.d("RECEIVER", "Unknown message: " + message);
                }
            } catch (Exception e) {
                Log.e("RECEIVER", e.getMessage(), e);
            }
        }

        private String parseSignedMessage(String signedMessage, String sender) {
            try {
                byte[] data = signedMessage.getBytes("UTF-8");
                int messageLength = (int) data[0];

                byte[] messageAndSize = new byte[1 + messageLength];
                System.arraycopy(data, 0, messageAndSize, 0, 1 + messageLength);

                byte[] signature = new byte[data.length - (1 + messageLength)];
                System.arraycopy(data, (1 + messageLength), signature, 0, signature.length);

                final String keyUrl = Login.serverUrl + "/users/" + sender + "/key";
                URL usersUrl = new URL(keyUrl);
                HttpURLConnection httpConnection = (HttpURLConnection) usersUrl.openConnection();
                httpConnection.setInstanceFollowRedirects(false);
                httpConnection.setRequestMethod("GET");
                int responseCode = httpConnection.getResponseCode();

                if (responseCode == 200) {
                    InputStream inputStream = httpConnection.getInputStream();
                    byte[] publicKey = new byte[httpConnection.getContentLength()];
                    inputStream.read(publicKey);
                    inputStream.close();

                    if (!verifyDataIntegrity(messageAndSize, signature, publicKey)) {
                        Log.d("CRYPTO", "Signature verification failed");
                    }

                    byte[] msg = new byte[messageLength];
                    System.arraycopy(messageAndSize, 1, msg, 0, messageLength);
                    return new String(msg, "UTF-8");
                } else {
                    Log.d("Crypto", "Couldn't obtain " + sender + "'s public key");
                    return null;
                }
            } catch (Exception e) {
                Log.e("CRYPTO", e.getMessage(), e);
            }
            return null;
        }

        private boolean verifyDataIntegrity(byte[] data, byte[] signature, byte[] publicKeyBytes) {
            try {
                // recover key
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

                Signature rsaSignature = Signature.getInstance("SHA512withRSA");

                // verify data integrity
                rsaSignature.initVerify(publicKey);
                rsaSignature.update(data, 0, data.length);
                return rsaSignature.verify(signature);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}



package pt.ulisboa.tecnico.grupo11.ubibike;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class Contacts extends AppCompatActivity implements SimWifiP2pManager.GroupInfoListener {
    private ArrayAdapter<String> listAdapter;

    private SimWifiP2pSocket mCliSocket = null;
    private WifiDirectReceiver mReceiver;
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;

    ProgressDialog progressDialog = null;

    String currentUser = null;
    String message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        progressDialog = new ProgressDialog(Contacts.this);
        progressDialog.setTitle("Sending");
        progressDialog.setMessage("Please wait!");

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayList<String> contacts = new ArrayList<String>() {{
            add("Item 1");
            add("Item 2");
        }};

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        listView.setLongClickable(true);
        registerForContextMenu(listView);
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(listAdapter.getItem(info.position));

            menu.add(0, 0, Menu.NONE, "Send points");
            menu.add(0, 1, Menu.NONE, "Send SMS");
        }
    }


    public class OutgoingCommTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            new SendCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    message);
        }
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            try {
                mCliSocket.getOutputStream().write((msg[0] + "\n").getBytes());
                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                sockIn.readLine();
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // create the alert box and text view
        final EditText edit = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(edit);
        builder.setPositiveButton("Send", null);
        builder.setNegativeButton("Cancel", null);
        builder.create();
        final AlertDialog dialog;

        switch (item.getItemId()) {
            case 0:
                // set the title and switch the keyboard to numeric
                edit.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                builder.setTitle("Send points");

                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Editable value = edit.getText();
                                try {
                                    int points = Integer.parseInt(value.toString());
                                    if (points <= 0 /*|| points > user.points*/) {
                                        Toast.makeText(Contacts.this, "You can't send that amount of points", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    mManager.requestGroupInfo(mChannel, Contacts.this);
                                    message = "#P#"+Home.username+"#"+points;
                                    currentUser = listAdapter.getItem(info.position);

                                    dialog.dismiss();
                                    progressDialog.show();
                                } catch (NumberFormatException e) {
                                    Toast.makeText(Contacts.this, "'" + value.toString() + "' is not a number.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

                dialog.show();
                break;

            case 1:

                // set the title
                builder.setTitle("Send SMS");
                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String smsText = edit.getText().toString();
                                if (!smsText.isEmpty()) {
                                    mManager.requestGroupInfo(mChannel, Contacts.this);
                                    message = "#M#"+Home.username+"#"+smsText;
                                    currentUser = listAdapter.getItem(info.position);

                                    dialog.dismiss();
                                    progressDialog.show();
                                } else {
                                    Toast.makeText(Contacts.this, "The message is empty. Please write something.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();
                break;
        }
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Toast.makeText(Contacts.this, "awldnawufbuiwabfuiwabfwaubfwaubfuwabiufwab.", Toast.LENGTH_SHORT).show();
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {
        Set<String> devicesInNetwork = groupInfo.getDevicesInNetwork();
        for(String name : devicesInNetwork)
        {
            if (name.equals(currentUser)) {
                SimWifiP2pDevice device = devices.getByName(currentUser);
                new OutgoingCommTask().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        device.getVirtIp());
            }
        }
    }
}

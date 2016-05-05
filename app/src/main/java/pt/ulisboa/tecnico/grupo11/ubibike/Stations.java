package pt.ulisboa.tecnico.grupo11.ubibike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stations extends AppCompatActivity implements OnMapReadyCallback {
    static SimpleAdapter adapter;
    // the data used in the list
    static List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    // the list of coordinates
    private List<String> stationsList = new ArrayList<>();
    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations);

        stationsList.clear();
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.stationsMap);
        mapFragment.getMapAsync(this);

        ListView listView = (ListView) findViewById(R.id.listViewStations);

        int id = 1;
        for (String stationCoord : Tab.stations.keySet()) {
            Map<String, String> datum = new HashMap<String, String>(2);
            stationsList.add(stationCoord);
            String title = "Station " + id++ + " - " + stationCoord;
            datum.put("title", title);
            String subtitle = "There are " + Tab.stations.get(stationCoord) + " bikes available";
            datum.put("sub", subtitle);
            data.add(datum);
        }

        adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, new String[]{"title", "sub"},
                new int[]{android.R.id.text1,
                        android.R.id.text2});

        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, final long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Stations.this);
                alert.setTitle("Booking");
                alert.setMessage("Do you want to book a bike?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int listPosition = (int) id;
                        final String coord = stationsList.get(listPosition);
                        int availableBikes = Tab.stations.get(coord);

                        if (availableBikes > 0) {
                            Tab.stations.put(coord, --availableBikes);
                            Map<String, String> listItem = data.get(listPosition);
                            listItem.put("sub", "There are " + availableBikes + " bikes available");

                            new Thread(new Runnable() {
                                public void run() {
                                    while (true) {
                                        try {
                                            URL url = new URL(Login.serverUrl + "/stations");
                                            HttpURLConnection createUserConn = (HttpURLConnection) url.openConnection();
                                            createUserConn.setDoOutput(true);
                                            createUserConn.setRequestMethod("PUT");

                                            byte[] updatedData = (coord + ":-").getBytes("UTF-8");
                                            DataOutputStream wr = new DataOutputStream(createUserConn.getOutputStream());
                                            wr.write(updatedData);
                                            wr.close();

                                            int responseCode = createUserConn.getResponseCode();
                                            if (responseCode == 200) {
                                                Tab.reservations.put(coord, true);
                                                return;
                                            } else
                                                Thread.sleep(5000);
                                        } catch (IOException | InterruptedException e) {
                                            Log.e("UPDATE_STATIONS", "IOException", e);
                                        }
                                    }
                                }
                            }).start();

                            adapter.notifyDataSetChanged();

                            Toast.makeText(Stations.this, "Successfully booked a bike", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(Stations.this, "There are no available bikes", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        int id = 1;
        boolean setCamera = false;

        for (String stationCoord : Tab.stations.keySet()) {
            String[] stationCoordSplited = stationCoord.split(",");
            LatLng position = new LatLng(Double.parseDouble(stationCoordSplited[0]), Double.parseDouble(stationCoordSplited[1]));

            map.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Station " + id++));

            if (!setCamera) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10.0f));
                setCamera = true;
            }
        }
    }
}

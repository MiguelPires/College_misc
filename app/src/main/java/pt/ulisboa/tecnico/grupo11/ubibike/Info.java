package pt.ulisboa.tecnico.grupo11.ubibike;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class Info extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trajectoryMap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        boolean setCamera = false;

        for (List<String> trajectory : Tab.trajectories) {
            int pointNumber = 1;
            List<LatLng> points = new ArrayList<LatLng>();

            for (String coordinates : trajectory) {
                String[] coordSplited = coordinates.split(",");
                LatLng position = new LatLng(Double.parseDouble(coordSplited[0]), Double.parseDouble(coordSplited[1]));
                googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Point number " + pointNumber++));

                points.add(position);
                if (!setCamera) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10.0f));
                    setCamera = true;
                }
            }
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(points);

            lineOptions.width(6);
            lineOptions.color(Color.RED);
            googleMap.addPolyline(lineOptions);
        }
    }
}

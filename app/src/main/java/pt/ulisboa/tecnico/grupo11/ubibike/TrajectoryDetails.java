package pt.ulisboa.tecnico.grupo11.ubibike;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryDetails extends AppCompatActivity implements OnMapReadyCallback {

    private List<String> trajectoryCoordinates = new ArrayList<String>();

    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_details);
        Bundle extras = getIntent().getExtras();
        //mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.trajectoryMap);

        if (extras != null) {
            trajectoryCoordinates = Tab.trajectories.get(extras.getInt("selectedIndex"));
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.trajectoryMap);
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        int pointNumber = 1;
        for (String coordinates : trajectoryCoordinates) {
            String[] coordSplited = coordinates.split(",");
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(coordSplited[0]), Double.parseDouble(coordSplited[1])))
                    .title("Point number " + pointNumber ));
            pointNumber++;
        }
    }
}
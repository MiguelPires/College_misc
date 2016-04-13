package pt.ulisboa.tecnico.grupo11.ubibike;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryDetails extends AppCompatActivity {

    ListView listView;
    List<String> trajectoryCoordinates = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_details);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trajectoryCoordinates = Tab.listOfTrajectories.get(extras.getInt("selectedIndex"));
            listView = (ListView) findViewById(R.id.trajectoryDetailslv);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, trajectoryCoordinates );
            listView.setAdapter(arrayAdapter);
        }

    }

}

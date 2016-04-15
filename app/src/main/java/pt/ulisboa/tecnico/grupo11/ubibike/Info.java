package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Info extends AppCompatActivity {

    private List<String> trajectoriesTextList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ListView listView = (ListView) findViewById(R.id.trajectorieslv);
        TextView pointsTextView = (TextView) findViewById(R.id.numberOfPointstv);
        pointsTextView.setText(Tab.numberOfPoints);
        trajectoriesTextList.clear();

        for(int x = 0; x < Tab.trajectories.size(); x++)
        {
            trajectoriesTextList.add("Trajectory " + (x+1));
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, trajectoriesTextList );
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newActivity = new Intent(Info.this, TrajectoryDetails.class);
                newActivity.putExtra("selectedIndex",position);
                startActivity(newActivity);
            }
        });
    }
}

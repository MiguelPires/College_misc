package pt.ulisboa.tecnico.grupo11.ubibike;

import android.app.TabActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

public class Tab extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tab);

        TabHost mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Contacts").setContent(new Intent(this, Contacts.class)));
        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("Home").setContent(new Intent(this  ,Home.class )));
        mTabHost.addTab(mTabHost.newTabSpec("maps").setIndicator("Maps").setContent(new Intent(this, Maps.class)));
        mTabHost.setCurrentTab(1);
    }
}

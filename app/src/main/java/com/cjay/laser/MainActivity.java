package com.cjay.laser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cjay.laser.communication.LaserConnector;
import com.cjay.laser.communication.OnLaserChangeListener;

public class MainActivity extends AppCompatActivity {

    private LaserConnector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Settings.loadFromSharedPreferences(preferences);

        connector = LaserConnector.getConnector();

        connector.addOnLaserChangeListener(new OnLaserChangeListener() {
            @Override
            public void onReady() {
                Log.i("Server","Ready");
            }

            @Override
            public void onJobEvent(boolean accept) {
                Log.i("Server","Event " + accept);
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                connector.makeJobPostAsync("Test");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: Eventuell über die gesammte Laufzeit der Anwendung horchend?
        connector.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connector.stop();
    }
}

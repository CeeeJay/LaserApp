package com.cjay.laser;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private LaserConnector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Settings.loadFromSharedPreferences(preferences);

        connector = LaserConnector.getConnector();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                LaserJobScheduler scheduler = new LaserJobScheduler(connector);
                scheduler.execute("{json text}");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Server","Start Server");
        connector.startServer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connector.stopServer();
    }
}

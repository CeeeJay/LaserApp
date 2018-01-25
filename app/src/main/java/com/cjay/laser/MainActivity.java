package com.cjay.laser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cjay.laser.communication.LaserConnector;
import com.cjay.laser.communication.LaserJobScheduler;

public class MainActivity extends AppCompatActivity {

    private LaserConnector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

package com.cjay.laser;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Christopher on 12.01.2018.
 */

public class LaserJobScheduler extends AsyncTask<String,Void,Void>{

    private LaserConnector connector;
    public LaserJobScheduler(LaserConnector connector){
        this.connector = connector;
    }

    @Override
    protected Void doInBackground(String... jobs) {
        for(String job : jobs){
            Log.i("Server",job + " gestartet!");
            if(!connector.makeJobPost(job)){
                Log.i("Server",job+ " fehlgeschlagen!");
                break;
            }
        }

        return null;
    }
}

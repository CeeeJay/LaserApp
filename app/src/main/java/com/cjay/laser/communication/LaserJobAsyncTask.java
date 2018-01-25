package com.cjay.laser.communication;

import android.os.AsyncTask;

/**
 * Eine Klasse, welche zur Aufgabe hat asyncron Jobs an den Laser Host zu übermitteln
 */
class LaserJobAsyncTask extends AsyncTask<String,Void,Boolean>{

    private LaserConnector connector;
    public LaserJobAsyncTask(LaserConnector connector){
        this.connector = connector;
    }

    @Override
    protected Boolean doInBackground(String... jobs) {
        String job = jobs[0];
        return connector.makeJobPost(job);
    }
}

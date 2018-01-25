package com.cjay.laser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Christopher on 20.01.2018.
 */

public class LaserConnector implements Settings.OnChangedListener {

    private static LaserConnector instance;

    private NanoHTTPD mOnReadyServer;
    private HashSet<OnLaserReadyListener> mOnLaserReadyListeners;

    @FunctionalInterface
    public interface OnLaserReadyListener{
        void onReady();
    }

    private LaserConnector(){
        mOnLaserReadyListeners = new HashSet<>();
        Settings.setOnChangedListeners(this);
    }

    public static LaserConnector getConnector() {
        if (instance == null) {
            synchronized (LaserConnector.class){
                if(instance == null){
                    instance = new LaserConnector();
                }
            }
        }

        return instance;
    }

    @Override
    public void onSettingsChanged(Settings newSettings) {
        if(mOnReadyServer != null && mOnReadyServer.getListeningPort() != newSettings.getOnReadyServerPort()){
            stopServer();
            startServer();
        }
    }

    private String getLaserHttpAddress(){
        Settings settings = Settings.getCurrentSettings();
        return String.format("http://%s:%d", settings.getLaserAddress(), settings.getLaserPort());
    }

    public void addOnLaserReadyListeners(OnLaserReadyListener listener){
        if(listener != null)
            mOnLaserReadyListeners.add(listener);
    }

    public void removeOnLaserReadyListeners(OnLaserReadyListener listener){
        mOnLaserReadyListeners.remove(listener);
    }

    private void invokeOnLaserReadyListeners(){
        for( OnLaserReadyListener listener : mOnLaserReadyListeners){
            listener.onReady();
        }
    }

    public synchronized boolean makeJobPost(String json) {
        boolean jobAccepted = false;
        HttpURLConnection connection = null;

        try
        {
            connection = openConnection(json.length());

            byte[] jsonBytes = json.getBytes("UTF-8");

            BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(jsonBytes);
            outputStream.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String res = in.readLine();

            jobAccepted = res.equals("Ok");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(connection != null)
            {
                connection.disconnect();
            }
        }

        return jobAccepted;
    }

    private HttpURLConnection openConnection(int contentLength) throws  IOException{
        URL urlToLink = new URL(getLaserHttpAddress());

        //establish connect-parameters and connect
        HttpURLConnection connection = (HttpURLConnection)urlToLink.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
        connection.setDoOutput(true);

        return connection;
    }

    private class OnReadyServer extends NanoHTTPD {

        public OnReadyServer(int port){
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            handleLaserReady(session);
            return newFixedLengthResponse("Ok");
        }

        private void handleLaserReady(IHTTPSession session){
            try{
                Settings settings = Settings.getCurrentSettings();
                String laserIp = settings.getLaserAddress();
                String requestIp = session.getHeaders().get("http-client-ip");

                if(requestIp.equals(laserIp)){
                    invokeOnLaserReadyListeners();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void startServer(){
        if(mOnReadyServer == null){
            try{
                mOnReadyServer = new OnReadyServer(Settings.getCurrentSettings().getOnReadyServerPort());
                mOnReadyServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void stopServer(){
        mOnReadyServer.stop();
        mOnReadyServer = null;
    }
}

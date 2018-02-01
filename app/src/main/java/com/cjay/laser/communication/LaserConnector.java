package com.cjay.laser.communication;

import android.os.AsyncTask;
import android.util.Log;

import com.cjay.laser.Settings;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;

import fi.iki.elonen.NanoHTTPD;

import static android.content.ContentValues.TAG;

/**
 * Eine Schnittstelle, die sich um die Verbindung mit dem Laser kümmert.
 * Sie stellt zum einen die Funktionalität zur verfügung Aufträge dem
 * Laser Host zu senden.
 * Zum Anderen wartet ein HTTP Server auf GET anfragen des Laser Hostes,
 * welche ein Ende eines Jobs signalisieren.
 */
public class LaserConnector implements Settings.OnChangedListener{

    private static LaserConnector sInstance;

    public static LaserConnector getConnector() {
        if (sInstance == null) {
            synchronized (LaserConnector.class){
                if(sInstance == null){
                    sInstance = new LaserConnector();
                }
            }
        }

        return sInstance;
    }

    private NanoHTTPD mOnReadyServer;
    private HashSet<OnLaserChangeListener> mOnLaserChangeListeners;

    /**
     * Erstellen einer Singelton instance
     * Initialisieren des HashSets der Beobachter und eines HTTP Servers für ankommende Ready GET
     * Anfragen
     */
    private LaserConnector(){
        mOnLaserChangeListeners = new HashSet<>();
        Settings.setOnChangedListeners(this);
        initServer();
    }

    private void initServer(){
        Settings settings = Settings.getCurrentSettings();
        mOnReadyServer = new OnReadyServer(settings.getOnReadyServerPort());
    }

    @Override
    public void onSettingsChanged(Settings newSettings) {
        if(newSettings.getOnReadyServerPort() != mOnReadyServer.getListeningPort()){
            stop();
            initServer();
            start();
        }
    }

    /**
     * Baut die HTTP Adresse des Lasers zusammen
     * @return Eine HTTP Adresse zum Ansprechen des Lasers
     */
    private String getLaserHttpAddress(){
        Settings settings = Settings.getCurrentSettings();
        return String.format(Locale.GERMAN, "http://%s:%d/REQUEST_ENGRAVING", settings.getLaserAddress(), settings.getLaserPort());
    }

    /**
     * Fügt einen Beobachter der Schnittstelle hinzu
     * Falls der Laser einen Ready GET sendet,
     * oder einen Job annimmt oder ablehnt gibt der dem Beobachter bescheid.
     * @param listener Ein Beobachter, welcher über Geschehnisse informiert werden will
     */
    public void addOnLaserChangeListener(OnLaserChangeListener listener){
        if(listener != null)
            mOnLaserChangeListeners.add(listener);
    }

    /**
     * Entfernt einen Beobachter aus dem Verteiler
     * @param listener Ein Beobachter, welcher über Geschehnisse nicht mehr informiert werden will
     */
    public void removeOnLaserChangeListener(OnLaserChangeListener listener){
        mOnLaserChangeListeners.remove(listener);
    }

    /**
     * Aufrufen der onReady Methoden aller Beobachter
     */
    private void invokeOnLaserReadyListener(){
        for( OnLaserChangeListener listener : mOnLaserChangeListeners){
            listener.onReady();
        }
    }

    /**
     * Aufrufen der onJobEvent Methoden aller Beobachter
     * @param accept Zeigt an ob eine Job Anfrage erfolgreich oder nicht
     *               erfolgreich war.
     */
    private void invokeOnLaserAcceptJobListener(boolean accept){
        for( OnLaserChangeListener listener : mOnLaserChangeListeners){
            listener.onJobEvent(accept);
        }
    }

    /**
     * Stößt eine asyncrone Jobübertragung an
     * @param job ein Job im json format
     * TODO: job Eventuell ersetzen durch ein representatives Objekt?
     */
    public void makeJobPostAsync( String job ){
        new LaserJobAsyncTask(this).execute(job);
    }

    /**
     * Hier wird verbindung zum Laser aufgebaut und ihm wird versucht einen
     * Job anzubieten
     * Je nachdem on der Laser abblehnt oder einstimmt wird diese Entscheidung
     * allen Beobachtern mitgeteilt
     * @param job Ein Job im Json Format
     * @return true falls der Job angenommen wurde andererseits false
     */
    private synchronized boolean makeJobPost(String job) {
        boolean jobAccepted = false;
        HttpURLConnection connection = null;

        try
        {
            connection = openConnection(job.length());

            Log.i("Server", "try send: " + job);

            byte[] jsonBytes = job.getBytes("UTF-8");

            BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(jsonBytes);
            outputStream.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String res = in.readLine();
            Log.i("Server", "makeJobPost: " + res);
            jobAccepted = res.equals(Settings.getCurrentSettings().getLaserAddress());
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

    /**
     * Öffnet eine Verbindung zum Laser und setzt ein paar Header
     * @param contentLength Die Länge des Contents der übermittelt werden soll
     * @return Eine Verbindung zum Laser
     * @throws IOException Falls verbindung nicht aufgebaut werden konnte wird eine
     *          Exception geworfen
     */
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

    /**
     * Setzt den HTTP server in Gang
     */
    public void start(){
        try {
            mOnReadyServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Stoppt den HTTP Server
     */
    public void stop(){
        mOnReadyServer.stop();
    }

    /**
     * Ein kleiner HTTP Server der GET Anfragen des Lasers annimmt,
     * der durch die GET Anfrage aussagen will, dass er mit dem Job
     * fertig ist.
     */
    private class OnReadyServer extends NanoHTTPD {

        OnReadyServer(int port){
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            handleLaserReady(session);
            return newFixedLengthResponse("Ok");
        }

        /**
         * Bewertet den ankommenden GET Request.
         * Stammt dieser wirklich vom Laser wird jeder Beobachter informiert,
         * dass der Laser mit seinem Job fertig ist
         * @param session Eine Session mit dem momentanen Client
         */
        private void handleLaserReady(IHTTPSession session){
            try{
                String laserIp = Settings.getCurrentSettings().getLaserAddress();
                String requestIp = session.getHeaders().get("http-client-ip");

                //Überprüfen ob Sever IP die geleiche ist, wie die in der App hinterlegte
                if(requestIp.equals(laserIp)){
                    //Informieren, dass Laser mit unserem Job fertig ist
                    invokeOnLaserReadyListener();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Ein asyncroner Task zum asyncronen senden von Job Angeboten
     */
    private static class LaserJobAsyncTask extends AsyncTask<String,Void,Boolean>{

        private LaserConnector connector;
        public LaserJobAsyncTask(LaserConnector connector){
            this.connector = connector;
        }

        @Override
        protected Boolean doInBackground(String... jobs) {
            String job = jobs[0];
            return connector.makeJobPost(job);
        }

        @Override
        protected void onPostExecute(Boolean jobAccepted) {
            connector.invokeOnLaserAcceptJobListener(jobAccepted);
        }
    }

}

package com.cjay.laser;

import android.content.SharedPreferences;

import java.util.HashSet;


/**
 * Eine Klasse die sich interaktiv um die verwaltung von Einstellungen kümmert.
 * Falls eine änderung geschiet werden alle regestirerten Beobachter informiert, welche
 * widerum Änderungen an sich selbst durchführen werden
 * Wenn einstellungen einmal geladen sind, werden sich nicht mehr geändert.
 * Es gibt höchstens eine Neue Version der Einstellungen
 * Dies hat zum vorteil, das man innerhalb eines vorganges keine wechselnden Einstellungen hat
 */
public class Settings {

    private static Settings sInstance;
    private static HashSet<OnChangedListener> sSettingsChangedListeners = new HashSet<>();

    /**
     * Listener für neue Einstellungen zu erhalten
     */
    public interface OnChangedListener {
        void onSettingsChanged(Settings newSettings);
    }

    /**
     * Aktuelle Einstellungen bekommen
     * @return
     */
    public static Settings getCurrentSettings() {
        return sInstance;
    }

    public static void setOnChangedListeners(OnChangedListener listener) {
        if( listener != null )
            sSettingsChangedListeners.add(listener);
    }

    public static void removeOnSettingsChangedListeners(OnChangedListener listener) {
        sSettingsChangedListeners.remove(listener);
    }

    /**
     * Ein anstoß neue Einstellungen zu übernehmen
     * Diese werden nur übernommen falls irgendeine Änderung geschehen ist.
     * Falls dies zutrifft wird jeder informiert
     * @param newSettings
     */
    private static void changeSettings( Settings newSettings ) {
        if(!newSettings.equals(sInstance)){
            sInstance = newSettings;
            for( OnChangedListener listener : sSettingsChangedListeners){
                listener.onSettingsChanged(newSettings);
            }
        }
    }

    public static void loadFromSharedPreferences(SharedPreferences preferences){
        changeSettings(new Settings(preferences));
    }

    private final String mLaserAddress;
    private final int mLaserPort;
    private final int mOnReadyServerPort;

    /**
     * Einstellungen aus einer SharedPreferences laden
     * @param preferences
     */
    private Settings(SharedPreferences preferences){
        mLaserAddress = preferences.getString("laser_adress","10.0.2.2");
        mLaserPort = preferences.getInt("laser_port",1337);
        mOnReadyServerPort = preferences.getInt("on_ready_server_port",1336);
    }

    /**
     * Gibt die IP Adresse des Laser Hostes zurück
     * @return
     */
    public String getLaserAddress() {
        return mLaserAddress;
    }

    /**
     * Gibt den Port auf welchem der Laser Host horcht zurück
     * @return
     */
    public int getLaserPort() {
        return mLaserPort;
    }

    /**
     * Gibt dem Port, welchem der Laser Host GET Ready Nachrichten schickt zurück
     * @return
     */
    public int getOnReadyServerPort() {
        return mOnReadyServerPort;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Settings)) return false;
        Settings other = (Settings) obj;

        return mLaserPort == other.mLaserPort &&
                mOnReadyServerPort == other.mOnReadyServerPort &&
                mLaserAddress.equals(other.mLaserAddress);
    }
}

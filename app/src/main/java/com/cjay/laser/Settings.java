package com.cjay.laser;

import android.content.SharedPreferences;

import java.util.HashSet;

/**
 * Created by Christopher on 20.01.2018.
 */

public class Settings {

    private static Settings sInstance;
    private static HashSet<OnChangedListener> sSettingsChangedListeners = new HashSet<>();

    public interface OnChangedListener {
        void onSettingsChanged(Settings newSettings);
    }

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

    private Settings(SharedPreferences preferences){
        mLaserAddress = preferences.getString("laser_adress","10.0.2.2");
        mLaserPort = preferences.getInt("laser_port",8000);
        mOnReadyServerPort = preferences.getInt("on_ready_server_port",8001);
    }

    public String getLaserAddress() {
        return mLaserAddress;
    }

    public int getLaserPort() {
        return mLaserPort;
    }

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

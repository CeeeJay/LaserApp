package com.cjay.laser.communication;

/**
 * Created by Christopher on 25.01.2018.
 */

public interface OnLaserChangeListener {

    /**
     * Falls der Laser mit einem von uns beauftragten Jobs fertig ist
     * wird und falls der Listener in einem LaserConnector gemeldet ist,
     * wird diese Methode aufgerufen.
     */
    void onReady();

    /**
     * Falls es eine neuigkeit bezüglich von der anwendung verschickte Job
     * Anfragen. Diese kann Positiv oder negativ ausfallen.
     */
    void onJobEvent(boolean accept);
}
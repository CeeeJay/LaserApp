package com.cjay.laser;

/**
 * Ein paar kleine Einstellungen zum Laser und dem Server.
 */
public class Settings {

    /**
     * Adresse des Lasers
     */
    public static final String LASER_ADDRESS = "10.0.2.2";

    /**
     * Port des Lasers
     */
    public static final int LASER_PORT = 8000;

    /**
     * Port des Lokalen Servers, welcher auf GET Anfragen des Lasers wartet.
     */
    public static final int ON_READY_SERVER_PORT = 8001;
}

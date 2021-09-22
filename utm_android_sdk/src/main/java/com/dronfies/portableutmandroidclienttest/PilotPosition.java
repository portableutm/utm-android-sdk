package com.dronfies.portableutmandroidclienttest;

class PilotPosition {

    private int altitude_gps;
    private Location location;
    private String time_sent;
    private String gufi;
    private String drone_id;

    public PilotPosition(int altitude_gps, Location location, String time_sent, String gufi, String droneId) {
        this.altitude_gps = altitude_gps;
        this.location = location;
        this.time_sent = time_sent;
        this.gufi = gufi;
        this.drone_id = droneId;
    }
}

package com.dronfies.portableutmandroidclienttest;

import java.util.Date;

public class TrackerPosition {
    private double latitude;
    private double longitude;
    private double altitude;
    private double heading;
    private Date time_sent;

    public TrackerPosition(double latitude, double longitude, double altitude, double heading, Date time_sent) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.heading = heading;
        this.time_sent = time_sent;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public Date getTime_sent() {
        return time_sent;
    }

    public void setTime_sent(Date time_sent) {
        this.time_sent = time_sent;
    }
}

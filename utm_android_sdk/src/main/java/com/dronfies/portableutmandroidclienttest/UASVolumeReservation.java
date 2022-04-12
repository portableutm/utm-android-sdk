package com.dronfies.portableutmandroidclienttest;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UASVolumeReservation {
    private String message_id;
    private String type;
    private String cause;
    private List<LatLng> polygon;
    private Date effective_time_begin;
    private Date effective_time_end;
    private int min_altitude;
    private int max_altitude;
    private String reason;

    public UASVolumeReservation(String message_id, String type, String cause, List<LatLng> polygon, Date effective_time_begin, Date effective_time_end, int min_altitude, int max_altitude, String reason) {
        this.message_id = message_id;
        this.type = type;
        this.cause = cause;
        this.polygon = polygon;
        this.effective_time_begin = effective_time_begin;
        this.effective_time_end = effective_time_end;
        this.min_altitude = min_altitude;
        this.max_altitude = max_altitude;
        this.reason = reason;
    }

    // Getter Methods

    public String getMessage_id() {
        return message_id;
    }

    public String getType() {
        return type;
    }

    public String getCause() {
        return cause;
    }

    public List<LatLng> getPolygon() {
        return polygon;
    }

    public Date getEffective_time_begin() {
        return effective_time_begin;
    }

    public Date getEffective_time_end() {
        return effective_time_end;
    }

    public int getMin_altitude() {
        return min_altitude;
    }

    public int getMax_altitude() {
        return max_altitude;
    }

    public String getReason() {
        return reason;
    }

    // Setter Methods

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public void setPolygon(List<LatLng> latLang) {
        this.polygon = latLang;
    }

    public void setEffective_time_begin(Date effective_time_begin) {
        this.effective_time_begin = effective_time_begin;
    }

    public void setEffective_time_end(Date effective_time_end) {
        this.effective_time_end = effective_time_end;
    }

    public void setMin_altitude(int min_altitude) {
        this.min_altitude = min_altitude;
    }

    public void setMax_altitude(int max_altitude) {
        this.max_altitude = max_altitude;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
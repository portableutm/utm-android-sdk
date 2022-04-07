package com.dronfies.portableutmandroidclienttest;

import com.google.android.gms.maps.model.LatLng;

public class ExpressOperationData {
    private LatLng location;
    private float radius;
    private int duration;
    private String vehicleId;
    private String phone;

    public ExpressOperationData(LatLng location, float radius, int duration, String vehicleId, String phone) {
        this.location = location;
        this.radius = radius;
        this.duration = duration;
        this.vehicleId = vehicleId;
        this.phone = phone;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

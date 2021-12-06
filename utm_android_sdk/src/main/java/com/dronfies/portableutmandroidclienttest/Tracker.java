package com.dronfies.portableutmandroidclienttest;

import java.util.List;

public class Tracker{
    public String hardware_id;
    public List<Directory> directory;
    public Vehicle vehicle;


    public Tracker(String hardware_id, List<Directory> directory) {
        this.hardware_id = hardware_id;
        this.directory = directory;
    }

    public Tracker(String hardware_id, Vehicle vehicle) {
        this.hardware_id = hardware_id;
        this.vehicle = vehicle;
    }

    public String getHardware_id() {
        return hardware_id;
    }

    public void setHardware_id(String hardware_id) {
        this.hardware_id = hardware_id;
    }

    public List<Directory> getDirectory() {
        return directory;
    }

    public void setDirectory(List<Directory> directory) {
        this.directory = directory;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}


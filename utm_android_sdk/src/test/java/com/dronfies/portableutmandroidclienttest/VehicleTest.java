package com.dronfies.portableutmandroidclienttest;

import org.junit.jupiter.api.BeforeAll;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {


    private static Vehicle vehicle;
    private static String uvin;
    private static Date date;
    private static String nNumber;
    private static String faaNumber;
    private static String vehicleName;
    private static String manufacturer;
    private static String model;
    private static Vehicle.EnumVehicleClass vehicleClass;
    private static String registeredBy;
    private static String owner;

    @BeforeAll
    static void setUp() throws Exception{
        date = new Date();
        uvin = "1";
        nNumber = "12";
        faaNumber = "123";
        vehicleName = "1234";
        manufacturer = "12345";
        model = "123456";
        vehicleClass = Vehicle.EnumVehicleClass.MULTIROTOR;
        registeredBy = "1234567";
        owner = "12345678";
        vehicle = new Vehicle("1",date, "12","123","1234","12345","123456", Vehicle.EnumVehicleClass.MULTIROTOR,"1234567","12345678");
    }


    @org.junit.jupiter.api.Test
    void getUvin() throws Exception {
        assertEquals(vehicle.getUvin(),uvin);
    }

    @org.junit.jupiter.api.Test
    void setUvin() {
        vehicle.setUvin("1TEST");
        uvin = "1TEST";
        assertEquals(vehicle.getUvin(), uvin);
    }

    @org.junit.jupiter.api.Test
    void getDate() {
        assertEquals(vehicle.getDate(),date);
    }

    @org.junit.jupiter.api.Test
    void setDate() {
        date = new Date();
        vehicle.setDate(date);
        assertEquals(vehicle.getDate(),date);
    }

    @org.junit.jupiter.api.Test
    void getnNumber() {
        assertEquals(vehicle.getnNumber(),nNumber);
    }

    @org.junit.jupiter.api.Test
    void setnNumber() {
        vehicle.setnNumber("12TEST");
        nNumber = "12TEST";
        assertEquals(vehicle.getnNumber(),nNumber);
    }

    @org.junit.jupiter.api.Test
    void getFaaNumber() {
        assertEquals(vehicle.getFaaNumber(),faaNumber);
    }

    @org.junit.jupiter.api.Test
    void setFaaNumber() {
        faaNumber = "TESTING";
        vehicle.setFaaNumber(faaNumber);
        assertEquals(vehicle.getFaaNumber(),faaNumber);
    }

    @org.junit.jupiter.api.Test
    void getVehicleName() {
        assertEquals(vehicle.getVehicleName(),vehicleName);
    }

    @org.junit.jupiter.api.Test
    void setVehicleName() {
        vehicleName = "TESTING1234";
        vehicle.setVehicleName(vehicleName);
        assertEquals(vehicle.getVehicleName(),vehicleName);
    }

    @org.junit.jupiter.api.Test
    void getManufacturer() {
        assertEquals(vehicle.getManufacturer(),manufacturer);
    }

    @org.junit.jupiter.api.Test
    void setManufacturer() {
        manufacturer = "TESTINGMANU";
        vehicle.setManufacturer(manufacturer);
        assertEquals(vehicle.getManufacturer(),manufacturer);
    }

    @org.junit.jupiter.api.Test
    void getModel() {
        assertEquals(vehicle.getModel(),model);
    }

    @org.junit.jupiter.api.Test
    void setModel() {
        model = "TESTINGMODEL";
        vehicle.setModel(model);
        assertEquals(vehicle.getModel(),model);

    }

    @org.junit.jupiter.api.Test
    void getVehicleClass() {
        assertEquals(vehicle.getVehicleClass(), vehicleClass);
    }

    @org.junit.jupiter.api.Test
    void setVehicleClass() {
        vehicleClass = Vehicle.EnumVehicleClass.FIXEDWING;
        vehicle.setVehicleClass(vehicleClass);
        assertEquals(vehicle.getVehicleClass(),vehicleClass);
    }

    @org.junit.jupiter.api.Test
    void getRegisteredBy() {
        assertEquals(vehicle.getRegisteredBy(),registeredBy);
    }

    @org.junit.jupiter.api.Test
    void setRegisteredBy() {
        registeredBy = "THATSME!";
        vehicle.setRegisteredBy(registeredBy);
        assertEquals(vehicle.getRegisteredBy(),registeredBy);
    }

    @org.junit.jupiter.api.Test
    void getOwner() {
        assertEquals(vehicle.getOwner(),owner);
    }

    @org.junit.jupiter.api.Test
    void setOwner() {
        owner = "THATSMEAGAIN!";
        vehicle.setOwner(owner);
        assertEquals(vehicle.getOwner(),owner);
    }
}
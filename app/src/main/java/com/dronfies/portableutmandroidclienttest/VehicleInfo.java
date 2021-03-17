package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class VehicleInfo extends AppCompatActivity {
    String id;
    String endPoint;

    TextView mNumber;
    TextView mModel;
    TextView mManufacturer;
    TextView mType;
    TextView mRegisteredBy;
    TextView mOwner;
    TextView mDate;
    TextView mFaa;
    TextView mUvin;
    TextView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);

        mNumber = findViewById(R.id.activity_description);
        mModel = findViewById(R.id.activity_startDate);
        mManufacturer = findViewById(R.id.activity_maxAltitude);
        mType = findViewById(R.id.activity_pilotName);
        mRegisteredBy = findViewById(R.id.activity_pilotPhone);
        mOwner = findViewById(R.id.activity_droneId);
        mDate = findViewById(R.id.activity_droneDescription);
        mFaa = findViewById(R.id.activity_owner);
        mUvin = findViewById(R.id.activity_flightComments);
        mName = findViewById(R.id.activity_opId);

        id = getIntent().getStringExtra("vehicle");
        endPoint = getIntent().getStringExtra("utmEndpoint");
        new Thread(new Runnable() {
                        @Override
            public void run() {
                load(endPoint);
            }
        }).start();
    }

    private void load(String endpoint) {
        String Number;
        String Model;
        String Manufacturer;
        String Type;
        String RegisteredBy;
        String Owner;
        String Date;
        String Faa;
        String Uvin;
        String Name;

        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(endpoint);
        try{

            Vehicle vehicle = dronfiesUssServices.getVehicleById(id);
            Number = vehicle.getnNumber();
            Model = vehicle.getModel();
            Manufacturer = vehicle.getManufacturer();
            Type = String.valueOf(vehicle.getVehicleClass());
            RegisteredBy = vehicle.getRegisteredBy();
            Owner = vehicle.getOwner();
            Date = String.valueOf(vehicle.getDate());
            Faa = vehicle.getFaaNumber();
            Uvin = vehicle.getUvin();
            Name = vehicle.getVehicleName();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNumber.setText(Number);
                    mModel.setText(Model);
                    mManufacturer.setText(Manufacturer);
                    mType.setText(Type);
                    mRegisteredBy.setText(RegisteredBy);
                    mOwner.setText(Owner);
                    mDate.setText(Date);
                    mFaa.setText(Faa);
                    mUvin.setText(Uvin);
                    mName.setText(Name);
                }
            });



        } catch (Exception e){
            Log.d("ERRORonEndpoint", e.getMessage(), e);
        }


    }
}
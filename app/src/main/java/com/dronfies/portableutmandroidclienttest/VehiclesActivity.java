package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class VehiclesActivity extends AppCompatActivity {
    LinearLayout vehiclesLayout;
    String utmEndpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
        vehiclesLayout = (LinearLayout) findViewById(R.id.vehiclesLinearLayout);
        utmEndpoint = getIntent().getStringExtra("utmEndpoint");
        new Thread(new Runnable() {
            @Override
            public void run() {
                testVehicles();
            }
        }).start();
    }


    private void addVehicle(Vehicle vehicle) {
        View vehicleView = getLayoutInflater().inflate(R.layout.layout_vehicle,null);
        vehicleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(getApplicationContext(), VehicleInfo.class);
                next.putExtra("vehicle", vehicle.getUvin());
                next.putExtra("utmEndpoint",utmEndpoint);
                startActivity(next);
            }
        });
        ImageView image = vehicleView.findViewById(R.id.operationImage);
        TextView name = vehicleView.findViewById(R.id.operationName);
        name.setText(vehicle.getVehicleName());
        //Change icon because XML defined icon not showing
        image.setImageResource(android.R.drawable.btn_star);
        vehiclesLayout.addView(vehicleView);
    }

    private void testVehicles(){
        int vehicleCount;
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(utmEndpoint);
        if(dronfiesUssServices.isAuthenticated()){
            try {
                List<Vehicle> vehicleList = dronfiesUssServices.getVehicles();
                vehicleCount = vehicleList.size();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Vehicle vehicle:vehicleList) {
                            addVehicle(vehicle);
                        }

                    }
                });
            } catch (Exception e) {
                Log.e("ERRORonVehichle", e.getMessage(), e);
            }
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "You should login on the previous screen.", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }


}
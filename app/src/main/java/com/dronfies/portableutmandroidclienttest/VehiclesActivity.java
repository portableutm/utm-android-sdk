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

import com.dronfies.portableutmandroidclienttest.entities.ICompletitionCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VehiclesActivity extends AppCompatActivity {

    private LinearLayout vehiclesLayout;
    private FloatingActionButton mButtonAddVehicle;
    private String utmEndpoint;
    private boolean mFromOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
        vehiclesLayout = (LinearLayout) findViewById(R.id.vehiclesLinearLayout);
        mButtonAddVehicle = findViewById(R.id.buttonAddVehicle);
        mButtonAddVehicle.setOnClickListener(view -> onClickAddVehicle());
        utmEndpoint = getIntent().getStringExtra("utmEndpoint");
        mFromOperator = getIntent().getBooleanExtra("fromOperator", false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                testVehicles();
            }
        }).start();
    }

    private void onClickAddVehicle(){
        UIGenericUtils.GoToActivity(this, AddVehicleActivity.class, Arrays.asList(Constants.ENDPOINT_KEY), Arrays.asList(utmEndpoint));
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
                final List<Vehicle>[] vehicleList = new List[1];
                if(mFromOperator){
                    dronfiesUssServices.getOperatorVehicles(10, 0, new ICompletitionCallback<List<Vehicle>>() {
                        @Override
                        public void onResponse(List<Vehicle> vehicles, String errorMessage) {
                            vehicleList[0]=vehicles;
                        }
                    });
                }else{
                    dronfiesUssServices.getVehicles(10, 0, new ICompletitionCallback<List<Vehicle>>() {
                        @Override
                        public void onResponse(List<Vehicle> vehicles, String errorMessage) {
                            vehicleList[0]=vehicles;
                        }
                    });
                }
                vehicleCount = vehicleList[0].size();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Vehicle vehicle:vehicleList[0]) {
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
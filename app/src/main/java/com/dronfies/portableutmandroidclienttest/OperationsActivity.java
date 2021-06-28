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
import com.dronfies.portableutmandroidclienttest.entities.Operation;

import java.util.List;

public class OperationsActivity extends AppCompatActivity {
    LinearLayout operationsLayout;
    String utmEndpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operations);

        operationsLayout = (LinearLayout) findViewById(R.id.operationsLinearLayout);
        utmEndpoint = getIntent().getStringExtra("utmEndpoint");
        new Thread(new Runnable() {
            @Override
            public void run() {
                testOperations();
            }
        }).start();
    }

    private void addOperation(Operation operation) {
        View operationView = getLayoutInflater().inflate(R.layout.layout_operation,null);
        operationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(getApplicationContext(), OperationInfoActivity.class);
                next.putExtra("operation", operation.getId());
                next.putExtra("utmEndpoint",utmEndpoint);
                startActivity(next);
            }
        });
        ImageView image = operationView.findViewById(R.id.operationImage);
        TextView name = operationView.findViewById(R.id.operationName);
        name.setText(operation.getDescription());
        //Change icon because XML defined icon not showing
        image.setImageResource(android.R.drawable.presence_online);
        operationsLayout.addView(operationView);
    }

    private void testOperations(){
        int vehicleCount;
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(utmEndpoint);
        List<Operation> operationList;
        if(dronfiesUssServices.isAuthenticated()){
            try {
                dronfiesUssServices.getOperations(new ICompletitionCallback<List<Operation>>() {
                    @Override
                    public void onResponse(List<Operation> operations, String errorMessage) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorMessage == null){
                                    for (Operation operation:operations) {
                                        addOperation(operation);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error when getting operations", Toast.LENGTH_SHORT ).show();
                                    Log.e("ERRORonOperations", errorMessage);
                                }
                            }
                        });
                    }
                });

            } catch (Exception e) {
                Log.e("ERRORonOperation", e.getMessage(), e);
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
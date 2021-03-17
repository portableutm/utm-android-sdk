package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText mEndPoint;
    private EditText mUsername;
    private EditText mPassword;
    private TextView mResult;
    private String utmEndpoint;
    Button btn_Login;
    Button btn_Vehicles;
    Button btn_Operations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEndPoint = (EditText) findViewById(R.id.et_endpoint);
        mUsername = (EditText) findViewById(R.id.et_username);
        mPassword = (EditText) findViewById(R.id.et_password);
        mResult = (TextView) findViewById(R.id.tv_result);

        btn_Login = (Button) findViewById(R.id.btn_Login);
        btn_Vehicles = (Button) findViewById(R.id.btn_Vehicles);
        btn_Operations = (Button) findViewById(R.id.btn_Operations);
        btn_Vehicles.setVisibility(View.INVISIBLE);
        btn_Operations.setVisibility(View.INVISIBLE);


        btn_Login.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        login(v);
                    }
                }).start();
            }
        });
        btn_Vehicles.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                vehicles(v);
            }
        });
        btn_Operations.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                operations(v);
            }
        });
    }

    private void operations(View v) {
        Intent next = new Intent(this, OperationsActivity.class);
        next.putExtra("utmEndpoint",utmEndpoint);
        startActivity(next);
    }

    private void login(View view){
        String endPoint = String.valueOf(mEndPoint.getText());
        utmEndpoint = endPoint;
        String username = String.valueOf(mUsername.getText());
        String password = String.valueOf(mPassword.getText());
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(endPoint);
        try {
            final String ret = dronfiesUssServices.login_sync(username, password);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mResult.setText(ret);
                    if (ret != null){
                        btn_Vehicles.setVisibility(View.VISIBLE);
                        btn_Operations.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch(Exception e){
            Log.e("ERRORonLOGIN", e.getMessage(), e);
        }

    }

    private void vehicles(View view){
        Intent next = new Intent(this, VehiclesActivity.class);
        next.putExtra("utmEndpoint",utmEndpoint);
        startActivity(next);
    }

}
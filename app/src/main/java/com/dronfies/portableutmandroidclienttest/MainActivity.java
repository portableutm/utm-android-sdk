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

import com.google.gson.Gson;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity_Logs";

    // views
    private EditText mEditTextEndpoint;
    private EditText mEditTextUsername;
    private EditText mEditTextPassword;
    private TextView mTextViewAccessToken;
    private Button mButtonLogin;
    private Button mButtonEndpoints;
    private Button mButtonVehicles;
    private Button mButtonOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextEndpoint = findViewById(R.id.editTextEndpoint);
        mEditTextUsername = findViewById(R.id.editTextUsername);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mTextViewAccessToken = findViewById(R.id.textViewAccessToken);
        mButtonLogin = findViewById(R.id.buttonLogin);
        mButtonLogin.setOnClickListener(view -> onClickLogin());
        mButtonEndpoints = findViewById(R.id.buttonEndpoints);
        mButtonEndpoints.setOnClickListener(view -> onClickEndpoints());
        mButtonVehicles = findViewById(R.id.buttonVehicles);
        mButtonVehicles.setOnClickListener(view -> onClickVehicles());
        mButtonOperations = findViewById(R.id.buttonOperations);
        mButtonOperations.setOnClickListener(view -> onClickOperations());
    }

    //------------------------------------------------------------------------------------------------------
    //------------------------------------------- EVENT HANDLERS -------------------------------------------
    //------------------------------------------------------------------------------------------------------

    private void onClickLogin(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String utmEndpoint = String.valueOf(mEditTextEndpoint.getText());
                String username = String.valueOf(mEditTextUsername.getText());
                String password = String.valueOf(mEditTextPassword.getText());
                DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(utmEndpoint);
                try {
                    final String ret = dronfiesUssServices.login_sync(username, password);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewAccessToken.setText(ret);
                            mEditTextEndpoint.setEnabled(false);
                        }
                    });
                } catch(Exception e){
                    Log.e("ERRORonLOGIN", e.getMessage(), e);
                }
            }
        }).start();
    }

    private void onClickEndpoints(){
        new Thread(() -> {
            String utmEndpoint = String.valueOf(mEditTextEndpoint.getText());
            DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(utmEndpoint);
            if(dronfiesUssServices == null){
                showToast("There is no portable utm backend deployed on the endpoint entered");
                return;
            }
            try{
                List<Endpoint> listEndpoints = dronfiesUssServices.getEndpoints();
                if(listEndpoints == null || listEndpoints.isEmpty()){
                    showToast("There are no endpoints configured on the backend");
                    return;
                }
                String[] endpoints = new String[listEndpoints.size()];
                for(int i = 0; i < listEndpoints.size(); i++){
                    endpoints[i] = new Gson().toJson(listEndpoints.get(i));
                }
                Intent intent = new Intent(new Intent(this, EndpointsActivity.class));
                intent.putExtra(Constants.DRONFIES_USS_ENDPOINT_KEY, utmEndpoint);
                intent.putExtra(Constants.ENDPOINTS_KEY, endpoints);
                startActivity(intent);
            }catch (Exception ex){
                Log.d(TAG, ex.getMessage(), ex);
                showToast(String.format("Error: %s", ex.getMessage()));
            }
        }).start();
    }

    private void onClickVehicles(){
        String utmEndpoint = String.valueOf(mEditTextEndpoint.getText());
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(utmEndpoint);
        if(dronfiesUssServices == null || !dronfiesUssServices.isAuthenticated()){
            Toast.makeText(this, "You have to login to use this functionality", Toast.LENGTH_LONG).show();
            return;
        }
        Intent next = new Intent(this, VehiclesActivity.class);
        next.putExtra("utmEndpoint",utmEndpoint);
        startActivity(next);
    }


    private void onClickOperations() {
        String utmEndpoint = String.valueOf(mEditTextEndpoint.getText());
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(utmEndpoint);
        if(dronfiesUssServices == null || !dronfiesUssServices.isAuthenticated()){
            Toast.makeText(this, "You have to login to use this functionality", Toast.LENGTH_LONG).show();
            return;
        }
        Intent next = new Intent(this, OperationsActivity.class);
        next.putExtra("utmEndpoint",utmEndpoint);
        startActivity(next);
    }

    private void showToast(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
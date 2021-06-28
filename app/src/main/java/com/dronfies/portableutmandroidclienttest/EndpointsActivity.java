package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class EndpointsActivity extends AppCompatActivity {

    // state
    private String mDronfiesussEndpoint;

    // views
    private LinearLayout mLinearLayoutEndpoints;
    private EditText mEditTextUsername;
    private Button mButtonSearch;
    private TextView mTextViewEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endpoints);

        // state
        mDronfiesussEndpoint = getIntent().getStringExtra(Constants.DRONFIES_USS_ENDPOINT_KEY);

        // views binding
        mLinearLayoutEndpoints = findViewById(R.id.linearLayoutEndpoints);
        mEditTextUsername = findViewById(R.id.editTextUsername);
        mButtonSearch = findViewById(R.id.buttonSearch);
        mButtonSearch.setOnClickListener(v -> onClickSearch());
        mTextViewEndpoint = findViewById(R.id.textViewEndpoint);

        String[] endpoints = getIntent().getStringArrayExtra(Constants.ENDPOINTS_KEY);
        loadEndpoints(endpoints);
    }

    private void onClickSearch(){
        String username = mEditTextUsername.getText().toString().trim();
        if(username.isEmpty()){
            UIGenericUtils.ShowToast(this, "You have to enter a username to use this feature");
            return;
        }
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(mDronfiesussEndpoint);
        if(dronfiesUssServices == null){
            UIGenericUtils.ShowToast(this, "There is no portable utm backend deployed on the endpoint entered");
            return;
        }
        new Thread(() -> {
            try{
                String endpoint = dronfiesUssServices.getEndpoint(username);
                runOnUiThread(() -> mTextViewEndpoint.setText(endpoint));
            } catch (Exception ex){
                runOnUiThread(() -> UIGenericUtils.ShowToast(EndpointsActivity.this, ex.getMessage()));
            }
        }).start();
    }

    private void loadEndpoints(String[] endpoints){
        for(String strEndpoint : endpoints){
            Endpoint endpoint = new Gson().fromJson(strEndpoint, Endpoint.class);
            LinearLayout linearLayoutEndpoint = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_endpoint, null);
            linearLayoutEndpoint.setPadding(0, 0, 0, UIGenericUtils.ConvertDPToPX(this, 10));
            ((TextView)linearLayoutEndpoint.findViewById(R.id.textViewCountry)).setText(endpoint.getCountryCode());
            ((TextView)linearLayoutEndpoint.findViewById(R.id.textViewName)).setText(endpoint.getName());
            ((TextView)linearLayoutEndpoint.findViewById(R.id.textViewBackendEndpoint)).setText(endpoint.getBackendEndpoint());
            ((TextView)linearLayoutEndpoint.findViewById(R.id.textViewFrontendEndpoint)).setText(endpoint.getFrontendEndpoint());
            mLinearLayoutEndpoints.addView(linearLayoutEndpoint);
        }
    }
}
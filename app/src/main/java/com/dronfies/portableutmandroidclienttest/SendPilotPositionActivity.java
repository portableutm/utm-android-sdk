package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SendPilotPositionActivity extends AppCompatActivity {

    // state
    private String mDronfiesUSSEndpoint;

    // views
    private RelativeLayout relativeLayoutRoot;
    private EditText editTextOperationId;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private EditText editTextAltitude;
    private EditText editTextDroneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_pilot_position);

        // state init
        mDronfiesUSSEndpoint = getIntent().getStringExtra(Constants.ENDPOINT_KEY);

        // views binding
        relativeLayoutRoot = findViewById(R.id.relative_layout_root);
        editTextOperationId = findViewById(R.id.editTextOperationId);
        try{
            editTextOperationId.setText(getIntent().getStringExtra(Constants.OPERATION_ID_KEY));
        }catch (Exception ex){}
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);
        editTextAltitude = findViewById(R.id.editTextAltitude);
        editTextDroneId = findViewById(R.id.editTextDroneId);
        try{
            editTextDroneId.setText(getIntent().getStringExtra(Constants.DRONE_ID_KEY));
        }catch (Exception ex){}
        findViewById(R.id.button_send_pilot_position).setOnClickListener((view) -> onClickSendPilotPosition());
    }

    private void onClickSendPilotPosition(){
        // validate data
        final double[] lon = {0};
        final double[] lat = {0};
        final double[] alt = {0};
        final String operationId = editTextOperationId.getText().toString();
        final String droneId = editTextDroneId.getText().toString();
        try{
            lon[0] = Double.parseDouble(editTextLongitude.getText().toString());
        }catch (Exception ex){
            UIGenericUtils.ShowToast(this, "Invalid longitude");
            return;
        }
        try{
            lat[0] = Double.parseDouble(editTextLatitude.getText().toString());
        }catch (Exception ex){
            UIGenericUtils.ShowToast(this, "Invalid latitude");
            return;
        }
        try{
            alt[0] = Double.parseDouble(editTextAltitude.getText().toString());
        }catch (Exception ex){
            UIGenericUtils.ShowToast(this, "Invalid altitude");
            return;
        }

        // send pilot position
        LinearLayout linearLayoutProgressBar = UIGenericUtils.ShowProgressBar(relativeLayoutRoot);
        new Thread(() -> {
            try {
                DronfiesUssServices.getInstance(mDronfiesUSSEndpoint).sendPilotPosition_sync(
                        lon[0], lat[0], alt[0], operationId, droneId
                );
                runOnUiThread(() -> {
                    relativeLayoutRoot.removeView(linearLayoutProgressBar);
                    UIGenericUtils.ShowAlert(SendPilotPositionActivity.this, "Pilot Position Sent!");
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    relativeLayoutRoot.removeView(linearLayoutProgressBar);
                    UIGenericUtils.ShowAlert(SendPilotPositionActivity.this, "Error: " + ex.getMessage());
                });
            }
        }).start();
    }
}
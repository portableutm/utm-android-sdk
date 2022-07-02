package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.dronfies.portableutmandroidclienttest.entities.IGenericCallback;

public class TrackerPositionActivity extends AppCompatActivity {

    private String mEndpoint;
    private String mOperationId;
    private String mTrackerPositionRef;

    private TextView mTextViewLatitude;
    private TextView mTextViewLongitude;
    private TextView mTextViewAltitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker_position);

        mEndpoint = getIntent().getStringExtra(Constants.ENDPOINT_KEY);
        mOperationId = getIntent().getStringExtra(Constants.OPERATION_ID_KEY);

        mTextViewLatitude = findViewById(R.id.text_view_latitude);
        mTextViewLongitude = findViewById(R.id.text_view_longitude);
        mTextViewAltitude = findViewById(R.id.text_view_altitude);

        try {
            mTrackerPositionRef = DronfiesUssServices.getInstance(mEndpoint).connectToTrackerPositionUpdates(mOperationId, new IGenericCallback<TrackerPosition>() {
                @Override
                public void onCallbackExecution(TrackerPosition trackerPosition, String errorMessage) {
                    if(errorMessage != null){
                        runOnUiThread(() -> UIGenericUtils.ShowToast(TrackerPositionActivity.this, errorMessage));
                        return;
                    }
                    runOnUiThread(() -> {
                        mTextViewLatitude.setText(trackerPosition.getLatitude() + "");
                        mTextViewLongitude.setText(trackerPosition.getLongitude() + "");
                        mTextViewAltitude.setText(trackerPosition.getAltitude() + "");
                        UIGenericUtils.ShowToast(TrackerPositionActivity.this, "Position Received!");
                    });
                }
            });
        } catch (NoAuthenticatedException e) {
            UIGenericUtils.ShowToast(this, e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectTrackerPositionUpdate();
    }

    private void disconnectTrackerPositionUpdate(){
        try{
            DronfiesUssServices.getInstance(mEndpoint).disconnectFromUpdates(mTrackerPositionRef);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
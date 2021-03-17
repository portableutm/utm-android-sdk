package com.dronfies.portableutmandroidclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.dronfies.portableutmandroidclienttest.entities.Operation;

public class OperationInfo extends AppCompatActivity {

    String id;
    String endPoint;

    TextView mDescription;
    TextView mStart;
    TextView mEnd;
    TextView mMaxAltitude;
    TextView mPilot;
    TextView mPhone;
    TextView mDroneId;
    TextView mDroneDescription;
    TextView mOwner;
    TextView mComments;
    TextView mId;
    TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_info);

        mDescription = findViewById(R.id.activity_description);
        mStart = findViewById(R.id.activity_startDate);
        mEnd = findViewById(R.id.activity_endDate);
        mMaxAltitude = findViewById(R.id.activity_maxAltitude);
        mPilot = findViewById(R.id.activity_pilotName);
        mPhone = findViewById(R.id.activity_pilotPhone);
        mDroneId = findViewById(R.id.activity_droneId);
        mDroneDescription = findViewById(R.id.activity_droneDescription);
        mOwner = findViewById(R.id.activity_owner);
        mComments = findViewById(R.id.activity_flightComments);
        mId = findViewById(R.id.activity_opId);
        mStatus = findViewById(R.id.activity_status);
        
        id = getIntent().getStringExtra("operation");
        endPoint = getIntent().getStringExtra("utmEndpoint");

        new Thread(new Runnable() {
            @Override
            public void run() {
                load(endPoint);
            }
        }).start();

    }

    private void load(String endPoint) {
        String Description;
        String Start;
        String End;
        String MaxAltitude;
        String Pilot;
        String Phone;
        String DroneId;
        String DroneDescription;
        String Owner;
        String Comments;
        String Id;
        String Status;
        
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(endPoint);

        try {
            Operation operation = dronfiesUssServices.getOperationById_sync(id);
             Description = operation.getDescription();
             Start = String.valueOf(operation.getStartDatetime());
             End = String.valueOf(operation.getEndDatetime());
             MaxAltitude = String.valueOf(operation.getMaxAltitude());
             Pilot = operation.getPilotName();
             Phone = operation.getContactPhone();
             DroneId = operation.getDroneId();
             DroneDescription = operation.getDroneDescription();
             Owner = operation.getOwner();
             Comments = operation.getOwner();
             Id = operation.getId();
             Status = String.valueOf(operation.getState());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   mDescription.setText(Description);
                   mStart.setText(Start);
                   mEnd.setText(End);
                   mMaxAltitude.setText(MaxAltitude);
                   mPilot.setText(Pilot);
                   mPhone.setText(Phone);
                   mDroneId.setText(DroneId);
                   mDroneDescription.setText(DroneDescription);
                   mOwner.setText(Owner);
                   mComments.setText(Comments);
                   mId.setText(Id);
                   mStatus.setText(Status);
                }
            });

        } catch (Exception e) {
            Log.d("ERRORonOperations", e.getMessage(), e);
        }
    }
}
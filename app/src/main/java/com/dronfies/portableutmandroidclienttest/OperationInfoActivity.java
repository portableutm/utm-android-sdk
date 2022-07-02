package com.dronfies.portableutmandroidclienttest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dronfies.portableutmandroidclienttest.entities.IGenericCallback;
import com.dronfies.portableutmandroidclienttest.entities.Operation;
import com.dronfies.portableutmandroidclienttest.entities.OperationStateUpdate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class OperationInfoActivity extends AppCompatActivity {

    // consts
    private static int PICK_DAT_FILE_REQUEST = 1;
    private static int REQUEST_PERMISSION = 2;

    // state
    private String id;
    private String endPoint;

    // views
    private RelativeLayout mRelativeLayoutRoot;
    private TextView mDescription;
    private TextView mStart;
    private TextView mEnd;
    private TextView mMaxAltitude;
    private TextView mPilot;
    private TextView mPhone;
    private TextView mDroneId;
    private TextView mDroneDescription;
    private TextView mOwner;
    private TextView mComments;
    private TextView mId;
    private TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_info);

        mRelativeLayoutRoot = findViewById(R.id.relative_layout_root);
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
        findViewById(R.id.button_change_state).setOnClickListener(v -> onClickChangeState());
        findViewById(R.id.button_upload_dat_file).setOnClickListener(v -> onClickUploadDatFile());
        findViewById(R.id.button_send_pilot_position).setOnClickListener(v -> onClickSendPilotPosition());
        findViewById(R.id.button_check_tracker_position).setOnClickListener(v -> onClickCheckTrackerPosition());
        findViewById(R.id.button_check_state_updates).setOnClickListener(v -> onClickCheckStateUpdates());

        id = getIntent().getStringExtra("operation");
        endPoint = getIntent().getStringExtra("utmEndpoint");

        new Thread(new Runnable() {
            @Override
            public void run() {
                load(endPoint);
            }
        }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_DAT_FILE_REQUEST) {
                if (data == null) {
                    //no data present
                    return;
                }

                Uri selectedFileUri = data.getData();
                LinearLayout linearLayoutProgressBar = UIGenericUtils.ShowProgressBar(mRelativeLayoutRoot);
                new Thread(() -> {
                    try {
                        DronfiesUssServices.getInstance(endPoint).uploadDatFile(mId.getText().toString(), getFileFromUri(selectedFileUri).getAbsolutePath());
                        runOnUiThread(() -> {
                            mRelativeLayoutRoot.removeView(linearLayoutProgressBar);
                            UIGenericUtils.ShowAlert(OperationInfoActivity.this, "dji dat file uploaded!");
                        });
                    } catch (Exception ex) {
                        Log.d("_Logs", ex.getMessage(), ex);
                        runOnUiThread(() -> {
                            mRelativeLayoutRoot.removeView(linearLayoutProgressBar);
                            UIGenericUtils.ShowAlert(OperationInfoActivity.this, "Error: " + ex.getMessage());
                        });
                    }
                }).start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION){
            for(int i = 0; i < permissions.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    return;
                }
            }
            uploadDatFile();
        }
    }

    private void onClickChangeState() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        for (Operation.EnumOperationState state : Operation.EnumOperationState.values()) {
            Button button = new Button(this);
            button.setText(state.toString());
            button.setOnClickListener(v -> updateOperationState(state));
            view.addView(button);
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_state))
                .setView(view)
                .create()
                .show();
    }

    private void onClickUploadDatFile() {
        // Add permission for camera and let user grant the permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OperationInfoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }
        uploadDatFile();
    }

    private void onClickSendPilotPosition() {
        UIGenericUtils.GoToActivity(
                this,
                SendPilotPositionActivity.class,
                Arrays.asList(Constants.ENDPOINT_KEY, Constants.OPERATION_ID_KEY, Constants.DRONE_ID_KEY),
                Arrays.asList(endPoint, id, mDroneId.getText().toString()));
    }

    private void onClickCheckTrackerPosition() {
        UIGenericUtils.GoToActivity(
                this,
                TrackerPositionActivity.class,
                Arrays.asList(Constants.ENDPOINT_KEY, Constants.OPERATION_ID_KEY),
                Arrays.asList(endPoint, id));
    }

    private void onClickCheckStateUpdates() {
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(endPoint);
        final String[] updatesRef = {null};
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("<STATE>")
                .setMessage("<MESSAGE>")
                .setCancelable(false)
                .setNegativeButton(R.string.stop, (dialogInterface, i) -> {
                    if(updatesRef[0] == null){
                        runOnUiThread(() -> UIGenericUtils.ShowAlert(OperationInfoActivity.this, getString(R.string.wait)));
                        return;
                    }
                    runOnUiThread(() -> dialogInterface.dismiss());
                    dronfiesUssServices.disconnectFromUpdates(updatesRef[0]);
                })
                .show();

        try {
            updatesRef[0] = dronfiesUssServices.connectToOperationStateUpdates(id, (operationStateUpdate, errorMessage) -> {
                if(errorMessage != null){
                    runOnUiThread(() -> {
                        alertDialog.setTitle("ERROR");
                        alertDialog.setMessage(errorMessage);
                    });
                    return;
                }
                runOnUiThread(() -> {
                    alertDialog.setTitle(operationStateUpdate.getState().name());
                    alertDialog.setMessage(operationStateUpdate.getMessage());
                });
            });
        } catch (NoAuthenticatedException e) {
            UIGenericUtils.ShowAlert(OperationInfoActivity.this, e.getMessage());
        }
    }

    private void uploadDatFile(){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_DAT_FILE_REQUEST);
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

    private void updateOperationState(Operation.EnumOperationState state) {
        new Thread(() -> {
            try {
                DronfiesUssServices.getInstance(endPoint).updateOperationState(id, state);
                runOnUiThread(() -> mStatus.setText(state.toString()));
            } catch (Exception ex) {
                runOnUiThread(() -> UIGenericUtils.ShowToast(this, String.format("error: %s", ex.getMessage())));
            }
        }).start();
    }

    private File getFileFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        switch (uri.getScheme()) {
            case "content":
                return getFileFromContentUri(uri);
            case "file":
                return new File(uri.getPath());
            default:
                return null;
        }
    }

    private File getFileFromContentUri(Uri contentUri) {
        if (contentUri == null) {
            return null;
        }
        File file = null;
        String filePath;
        String fileName;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
            cursor.close();
            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
            if (!file.exists() || file.length() <= 0 || TextUtils.isEmpty(filePath)) {
                filePath = getPathFromInputStreamUri(contentUri, fileName);
            }
            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
        }
        return file;
    }

    public String getPathFromInputStreamUri(Uri uri, String fileName) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = getContentResolver().openInputStream(uri);
                File file = createTemporalFileFrom(inputStream, fileName);
                filePath = file.getPath();

            } catch (Exception e) {
                // log exception
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    // log exception
                }
            }
        }

        return filePath;
    }

    private File createTemporalFileFrom(InputStream inputStream, String fileName)
            throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];
            // I define the copy file path
            targetFile = new File(getCacheDir(), fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return targetFile;
    }
}
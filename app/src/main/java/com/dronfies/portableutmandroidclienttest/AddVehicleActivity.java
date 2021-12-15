package com.dronfies.portableutmandroidclienttest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dronfies.portableutmandroidclienttest.entities.ExtraField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddVehicleActivity extends AppCompatActivity {

    // consts
    private static int PICK_FILE_REQUEST = 1;
    private static int REQUEST_PERMISSION = 2;

    // state
    private String mUtmEndpoint;
    private ExtraField mLastFileExtraFieldClicked;
    private Map<ExtraField, View> mMapViewExtraField;

    // views
    private EditText mEditTextDate;
    private EditText mEditTextNNumber;
    private EditText mEditTextFAANumber;
    private EditText mEditTextVehicleName;
    private EditText mEditTextManufacturer;
    private EditText mEditTextModel;
    private EditText mEditTextVehicleClass;
    private LinearLayout mLinearLayoutExtraFields;
    private Button mButtonAddVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Vehicle");

        // init state
        mUtmEndpoint = getIntent().getStringExtra(Constants.ENDPOINT_KEY);
        mMapViewExtraField = new HashMap<>();

        // views binding
        mEditTextDate = findViewById(R.id.edit_text_date);
        setCurrentDateTime();
        mEditTextNNumber = findViewById(R.id.edit_text_n_number);
        mEditTextFAANumber = findViewById(R.id.edit_text_faa_number);
        mEditTextVehicleName = findViewById(R.id.edit_text_vehicle_name);
        mEditTextManufacturer = findViewById(R.id.edit_text_manufacturer);
        mEditTextModel = findViewById(R.id.edit_text_model);
        mEditTextVehicleClass = findViewById(R.id.edit_text_vehicle_class);
        mEditTextVehicleClass.setText(Vehicle.EnumVehicleClass.MULTIROTOR.name());
        mLinearLayoutExtraFields = findViewById(R.id.linear_layout_extra_fields);
        mButtonAddVehicle = findViewById(R.id.button_add_vehicle);
        mButtonAddVehicle.setOnClickListener(view -> onClickAddVehicle());

        // load extra fields
        new Thread(() -> loadExtraFields()).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    //no data present
                    return;
                }
                Uri selectedFileUri = data.getData();
                ((TextView)mMapViewExtraField.get(mLastFileExtraFieldClicked)).setText(getFileFromUri(selectedFileUri).getAbsolutePath());
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
            uploadFile();
        }
    }

    //-----------------------------------------------------------------------------------------
    //------------------------------------ ONCLICK METHODS ------------------------------------
    //-----------------------------------------------------------------------------------------

    private void onClickChooseFile(ExtraField extraField){
        mLastFileExtraFieldClicked = extraField;
        // Add permission for camera and let user grant the permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }
        uploadFile();
    }

    private void onClickAddVehicle(){
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(mUtmEndpoint);
        String uvin = null;
        Date date = getDate();
        if(date == null){
            UIGenericUtils.ShowToast(this, "Date is not valid");
            return;
        }
        String nNumber = mEditTextNNumber.getText().toString();
        String faaNumber = mEditTextFAANumber.getText().toString();
        String vehicleName = mEditTextVehicleName.getText().toString();
        String manufacturer = mEditTextManufacturer.getText().toString();
        String model = mEditTextModel.getText().toString();
        Vehicle.EnumVehicleClass vehicleClass = getVehicleClass();
        if(vehicleClass == null){
            UIGenericUtils.ShowToast(this, "Vehicle class is not valid (MULTIROTOR or FIXEDWING)");
            return;
        }
        String registeredBy = dronfiesUssServices.getUsername();
        String owner = dronfiesUssServices.getUsername();
        Vehicle.EnumVehicleAuthorization authorization = null;
        Vehicle vehicle = new Vehicle(uvin, date, nNumber, faaNumber, vehicleName, manufacturer, model, vehicleClass, registeredBy, owner, authorization);
        Map<ExtraField, Object> mapExtraFieldValues = getExtraFieldValues();
        new Thread(() -> {
            try {
                dronfiesUssServices.addVehicle(vehicle, mapExtraFieldValues);
            } catch (Exception e) {
                Log.d("_Logs", e.getMessage(), e);
                runOnUiThread(() -> UIGenericUtils.ShowAlert(this, e.getMessage()));
                return;
            }
            runOnUiThread(() -> UIGenericUtils.ShowAlert(this, "Vehicle added!"));
        }).start();
    }

    //-----------------------------------------------------------------------------------------
    //------------------------------------ PRIVATE METHODS ------------------------------------
    //-----------------------------------------------------------------------------------------

    private Map<ExtraField, Object> getExtraFieldValues(){
        Map<ExtraField, Object> ret = new HashMap<>();
        for(Map.Entry<ExtraField, View> entry : mMapViewExtraField.entrySet()){
            ExtraField extraField = entry.getKey();
            if(extraField.getType() == ExtraField.EnumExtraFieldType.STRING){
                EditText editText = (EditText) entry.getValue();
                String value = editText.getText().toString().trim();
                if(!value.isEmpty()){
                    ret.put(extraField, value);
                }
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.NUMBER){
                EditText editText = (EditText) entry.getValue();
                Double value = null;
                try{
                    value = Double.parseDouble(editText.getText().toString().trim());
                }catch (Exception ex){}
                if(value != null){
                    ret.put(extraField, value);
                }
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.DATE){
                EditText editText = (EditText) entry.getValue();
                Date value = null;
                try{
                    value = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(editText.getText().toString().trim());
                }catch (Exception ex){}
                if(value != null){
                    ret.put(extraField, value);
                }
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.FILE){
                TextView textView = (TextView) entry.getValue();
                String value = textView.getText().toString().trim();
                if(!value.isEmpty()){
                    ret.put(extraField, value);
                }
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.BOOL){
                Spinner spinner = (Spinner) entry.getValue();
                String strValue = spinner.getSelectedItem().toString().trim();
                if(strValue.equalsIgnoreCase("YES")){
                    ret.put(extraField, true);
                }else if(strValue.equalsIgnoreCase("NO")){
                    ret.put(extraField, false);
                }
            }
        }
        return ret;
    }

    private void loadExtraFields(){
        DronfiesUssServices dronfiesUssServices = DronfiesUssServices.getInstance(mUtmEndpoint);
        List<ExtraField> extraFields = null;
        try {
            extraFields = dronfiesUssServices.getVehicleExtraFields();
        } catch (Exception e) {
            runOnUiThread(() -> UIGenericUtils.ShowAlert(AddVehicleActivity.this, "There was an error trying to get the extra fields so the vehicle cannot be added"));
            return;
        }
        for(ExtraField extraField : extraFields){
            runOnUiThread(() -> mLinearLayoutExtraFields.addView(getExtraFieldView(extraField)));
        }
    }

    private View getExtraFieldView(ExtraField extraField){
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textViewName = new TextView(this);
        String text = extraField.isRequired() ? extraField.getName() + " (required)" : extraField.getName();
        textViewName.setText(text);
        textViewName.setTypeface(textViewName.getTypeface(), Typeface.BOLD_ITALIC);
        linearLayout.addView(textViewName);

        int dp10 = UIGenericUtils.ConvertDPToPX(this, 10);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, dp10);
        if(extraField.getType() == ExtraField.EnumExtraFieldType.STRING || extraField.getType() == ExtraField.EnumExtraFieldType.NUMBER || extraField.getType() == ExtraField.EnumExtraFieldType.DATE){
            EditText editText = new EditText(this);
            if(extraField.getType() == ExtraField.EnumExtraFieldType.DATE){
                editText.setHint("dd-mm-yyyy hh:mm:ss");
            }
            linearLayout.addView(editText, layoutParams);
            mMapViewExtraField.put(extraField, editText);
        } else if(extraField.getType() == ExtraField.EnumExtraFieldType.FILE){
            LinearLayout linearLayoutFile = new LinearLayout(this);
            linearLayoutFile.setOrientation(LinearLayout.HORIZONTAL);
            Button buttonSelectFile = new Button(this);
            buttonSelectFile.setText("CHOOSE FILE");
            buttonSelectFile.setOnClickListener(view -> onClickChooseFile(extraField));
            linearLayoutFile.addView(buttonSelectFile);
            TextView textViewSelectedFile = new TextView(this);
            linearLayoutFile.addView(textViewSelectedFile);
            linearLayout.addView(linearLayoutFile, layoutParams);
            mMapViewExtraField.put(extraField, textViewSelectedFile);
        } else if(extraField.getType() == ExtraField.EnumExtraFieldType.BOOL){
            List<String> spinnerArray = new ArrayList<>();
            spinnerArray.add("-");
            spinnerArray.add("YES");
            spinnerArray.add("NO");
            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
            spinner.setAdapter(spinnerArrayAdapter);
            linearLayout.addView(spinner, layoutParams);
            mMapViewExtraField.put(extraField, spinner);
        }

        return linearLayout;
    }

    private Vehicle.EnumVehicleClass getVehicleClass(){
        try{
            return Vehicle.EnumVehicleClass.valueOf(mEditTextVehicleClass.getText().toString().trim());
        }catch (Exception ex){
            return null;
        }
    }

    private Date getDate(){
        try{
            return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(mEditTextDate.getText().toString());
        }catch (Exception ex){
            return null;
        }
    }

    private void setCurrentDateTime(){
        mEditTextDate.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
    }

    private void uploadFile(){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQUEST);
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
            if (file == null || !file.exists() || file.length() <= 0 || TextUtils.isEmpty(filePath)) {
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
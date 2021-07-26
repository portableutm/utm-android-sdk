package com.dronfies.portableutmandroidclienttest;

import android.util.Log;

import com.dronfies.portableutmandroidclienttest.entities.GPSCoordinates;
import com.dronfies.portableutmandroidclienttest.entities.ICompletitionCallback;
import com.dronfies.portableutmandroidclienttest.exception.BadRequestException;
import com.dronfies.portableutmandroidclienttest.exception.NotFoundException;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DronfiesUssServices {

    // Singleton Pattern
    private static DronfiesUssServices INSTANCE = null;

    private final IRetrofitAPI api;

    private String authToken = null;
    private String mUsername = null;

    private static String utmEndpoint = null;

    private DronfiesUssServices() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(utmEndpoint)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        api = retrofit.create(IRetrofitAPI.class);
    }

    public static DronfiesUssServices getInstance(String utmEndpoint){
        if(utmEndpoint == null){
            // utmEndpoint cant be null
            throw new RuntimeException("UTM endpoint can't be null");
        }
        boolean utmEndpointChanged = false;
        if(DronfiesUssServices.utmEndpoint == null || !DronfiesUssServices.utmEndpoint.equals(utmEndpoint)){
            utmEndpointChanged = true;
            DronfiesUssServices.utmEndpoint = utmEndpoint;
        }
        if(INSTANCE == null || utmEndpointChanged){
            // it means we have to regenerate the INSTANCE
            try{
                INSTANCE = new DronfiesUssServices();
            }catch(Exception ex){}
        }
        return INSTANCE;
    }

    //----------------------------------------------------------------------------------------------------
    //------------------------------------------ PUBLIC METHODS ------------------------------------------
    //----------------------------------------------------------------------------------------------------

    public boolean isAuthenticated(){
        return authToken != null && mUsername != null;
    }

    // To use this method, isAuthenticated must return true
    public String getUsername(){
        return mUsername;
    }

    public void logout(){
        authToken = null;
        mUsername = null;
    }

    public void login(String username, String password, final ICompletitionCallback<String> callback){
        api.login(new User(username, password)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + "");
                    return;
                }
                authToken = response.body();
                mUsername = username;
                callback.onResponse(response.body(), null);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    public String login_sync(String username, String password) throws Exception {
        Response<String> response = api.login(new User(username, password)).execute();
        authToken = response.body();
        mUsername = username;
        return authToken;
    }

    // When we add an operation, the droneDescription is ignored. This field is used when we get the operations from the backend
    /*public void addOperation(com.dronfies.portableutmandroidclient.entities.Operation operation, final ICompletitionCallback<String> callback){
        api.addOperation(authToken, transformOperation(operation)).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(!response.isSuccessful()){
                    String errorBody = "";
                    try{
                        errorBody = response.errorBody().string();
                    }catch (Exception ex){}
                    callback.onResponse(null, response.code() + " (response.body: "+response.raw().body()+", response.errorBody: "+ errorBody + ")");
                    return;
                }
                // we update the authToken everytime a service responds succesfully
                //DronfiesUssServices.this.authToken = response.headers().get("token");

                if(response.body().toString().startsWith("{Error=")){
                    callback.onResponse(null, response.body().toString() + " (httpCode="+response.code()+")");
                }else{
                    callback.onResponse(response.body().toString() + " ("+response.getClass()+")", null);
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }*/

    // When we add an operation, the droneDescription is ignored. This field is used when we get the operations from the backend
    // return the id of the operation inserted
    public String addOperation_sync(com.dronfies.portableutmandroidclienttest.entities.Operation operation) throws Exception{
        Operation transformedOperation = transformOperation(operation);
        Call<ResponseBody> call = api.addOperation(authToken, transformedOperation);
        Response<ResponseBody> response = call.execute();
        if(!response.isSuccessful()){
            String errorBody = "";
            try{
                errorBody = response.errorBody().string();
            }catch (Exception ex){}
            throw new Exception(response.code() + " (response.body: "+response.raw().body()+", response.errorBody: "+ errorBody + ")");
        }
        if(response.body().toString().startsWith("{Error=")){
            throw new Exception(response.body().toString() + " (httpCode="+response.code()+")");
        }else{
            return new JSONObject(response.body().string()).get("gufi") + "";
        }
    }

    public void sendPosition(double lon, double lat, double alt, double heading, String operationId, final ICompletitionCallback<String> callback){
        Date now = new Date();
        String timeSent = new SimpleDateFormat("yyyy-MM-dd").format(now) + "T" + new SimpleDateFormat("HH:mm:ss.SSS").format(now) + "Z";
        Position position = new Position(
                (int) Math.round(alt),
                new Location(
                    "Point",
                    new double[]{lon, lat}
                ),
                heading,
                timeSent,
                operationId
        );
        api.sendPosition(authToken, position).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + " ("+response.getClass()+")");
                    return;
                }
                // we update the authToken everytime a service responds succesfully
                //DronfiesUssServices.this.authToken = response.headers().get("token");

                callback.onResponse(response.body().toString() + " ("+response.getClass()+")", null);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    public void sendPosition_sync(double lon, double lat, double alt, double heading, String operationId) throws Exception{
        Date now = new Date();
        String timeSent = new SimpleDateFormat("yyyy-MM-dd").format(now) + "T" + new SimpleDateFormat("HH:mm:ss.SSS").format(now) + "Z";
        Position position = new Position(
                (int)Math.round(alt),
                new Location(
                        "Point",
                        new double[]{lon, lat}
                ),
                heading,
                timeSent,
                operationId
        );
        api.sendPosition(authToken, position).execute();
    }

    public void sendParaglidingPosition(double lon, double lat, double alt, final ICompletitionCallback<String> callback){
        Date now = new Date();
        String timeSent = new SimpleDateFormat("yyyy-MM-dd").format(now) + "T" + new SimpleDateFormat("HH:mm:ss.SSS").format(now) + "Z";
        ParaglidingPosition position = new ParaglidingPosition(
                (int) Math.round(alt),
                new Location(
                        "Point",
                        new double[]{lon, lat}
                ),
                timeSent
        );
        api.sendParaglidingPosition(authToken, position).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + " ("+response.getClass()+")");
                    return;
                }
                // we update the authToken everytime a service responds succesfully
                //DronfiesUssServices.this.authToken = response.headers().get("token");

                callback.onResponse(response.body().toString() + " ("+response.getClass()+")", null);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    public void getOperations(final ICompletitionCallback<List<com.dronfies.portableutmandroidclienttest.entities.Operation>> callback) throws NoAuthenticatedException {
        getOperations(null, null, callback);
    }

    public void getOperations(Integer limit, Integer offset, final ICompletitionCallback<List<com.dronfies.portableutmandroidclienttest.entities.Operation>> callback) throws NoAuthenticatedException {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        Call<Object> call = api.getOperations(authToken);
        if(limit != null && offset != null){
            call = api.getOperations(authToken, limit, offset);
        }
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + " ("+response.getClass()+")");
                    return;
                }
                // we update the authToken everytime a service responds succesfully
                //DronfiesUssServices.this.authToken = response.headers().get("token");
                List<com.dronfies.portableutmandroidclienttest.entities.Operation> listOperations = new ArrayList<>();
                JsonObject jsonObject = new Gson().toJsonTree((Map<?, List<?>>)response.body()).getAsJsonObject();
                for(JsonElement jsonElement : jsonObject.get("ops").getAsJsonArray()){
                    try{
                        listOperations.add(getOperationFromJsonObject(jsonElement.getAsJsonObject()));
                    }catch(Exception ex){}
                }
                callback.onResponse(listOperations, null);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    public com.dronfies.portableutmandroidclienttest.entities.Operation getOperationById_sync(String operationId) throws Exception {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        Call<Object> call = api.getOperationById(authToken, operationId);
        Response<Object> response = call.execute();
        if(!response.isSuccessful()){
            throw new Exception(response.code() + " ("+response.getClass()+")");
        }
        JsonObject jsonObject = new Gson().toJsonTree((Map<?, List<?>>)response.body()).getAsJsonObject();
        return getOperationFromJsonObject(jsonObject);
    }

    public void deleteOperation(String operationId, final ICompletitionCallback<String> callback){
        api.deleteOperation(authToken, operationId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + " ("+response.getClass()+")");
                    return;
                }
                callback.onResponse(response.body().toString(), null);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    public List<Vehicle> getVehicles() throws Exception {
        String responseBody = api.getVehicles(authToken).execute().body().string();
        JSONArray jsonArrayVehicles = new JSONArray(responseBody);
        List<Vehicle> ret = new ArrayList<>();
        for(int i = 0; i < jsonArrayVehicles.length(); i++){
            JSONObject jsonObject = jsonArrayVehicles.getJSONObject(i);
            ret.add(parseVehicle(jsonObject));
        }
        return ret;
    }

    public Vehicle getVehicleById(String id) throws Exception {
        String responseBody = api.getVehicleById(authToken,id).execute().body().string();
        JSONObject jsonVehicles = new JSONObject(responseBody);
        return parseVehicle(jsonVehicles);
    }

    public List<RestrictedFlightVolume> getRestrictedFlightVolumes() throws Exception {
        String responseBody = api.getRestrictedFlightVolumes(authToken).execute().body().string();
        JSONArray jsonArrayRFVs = new JSONArray(responseBody);
        List<RestrictedFlightVolume> ret = new ArrayList<>();
        for(int i = 0; i  < jsonArrayRFVs.length(); i++){
            JSONObject jsonObject = jsonArrayRFVs.getJSONObject(i);
            ret.add(parseRFV(jsonObject));
        }
        return ret;
    }

    public List<Endpoint> getEndpoints() throws Exception {
        String responseBody = api.getEndpoints().execute().body().string();
        JSONArray jsonArrayEndpoints = new JSONArray(responseBody);
        List<Endpoint> result = new ArrayList<>();
        for(int i = 0; i < jsonArrayEndpoints.length(); i++){
            JSONObject jsonObject = jsonArrayEndpoints.getJSONObject(i);
            String name = jsonObject.get("name") + "";
            String backendEndpoint = jsonObject.get("endpoint") + "";
            String frontendEndpoint = jsonObject.get("frontendEndpoint") + "";
            String countryCode = jsonObject.get("country") + "";
            result.add(new Endpoint(name, backendEndpoint, frontendEndpoint, countryCode));
        }
        return result;
    }

    public String getEndpoint(String username) throws Exception {
        Response<ResponseBody> response = api.getEndpoint(authToken, username).execute();
        if(response.code() != 200){
            handleErrorResponse(response);
        }
        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);
        if(!jsonObject.has("endpoint")){
            throw new Exception("500: Response body hasn't got the 'endpoint' key");
        }
        return jsonObject.get("endpoint") + "";
    }

    public void updateOperationState(String operationId, com.dronfies.portableutmandroidclienttest.entities.Operation.EnumOperationState state) throws Exception {
        UpdateStateRequestBody requestBody = new UpdateStateRequestBody(state.name());
        Response<ResponseBody> response = api.updateOperationState(authToken, operationId, requestBody).execute();
        if(response.code() != 200){
            handleErrorResponse(response);
        }
    }

    public void uploadDatFile(String operationId, String filepath) throws Exception{
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), new File(filepath));
        MultipartBody.Part parts = MultipartBody.Part.createFormData("djiDatFile", filepath, requestBody);

        RequestBody requestBodyOperationId = RequestBody.create(MediaType.parse("text/plain"), operationId);

        Response<ResponseBody> response = api.uploadDatFile(parts, requestBodyOperationId).execute();
        if(response.code() != 200){
            handleErrorResponse(response);
        }
    }

    //----------------------------------------------------------------------------------------------------
    //----------------------------------------- PRIVATE METHODS  -----------------------------------------
    //----------------------------------------------------------------------------------------------------

    private Operation transformOperation(com.dronfies.portableutmandroidclienttest.entities.Operation operation){
        List<List<Double>> polygonCoordinates = new ArrayList<>();
        for(GPSCoordinates latLng : operation.getPolygon()){
            // for the backend, we have to send (longitude, latitude)
            polygonCoordinates.add(Arrays.asList(latLng.getLongitude(), latLng.getLatitude()));
        }

        Date submitDate = new Date();
        List<String> uasRegistrations = new ArrayList<>();
        uasRegistrations.add(operation.getDroneId());
        PriorityElements priorityElements = new PriorityElements(
                1,
                "EMERGENCY_AIR_AND_GROUND_IMPACT"
        );

        List<ContingencyPlan> contingencyPlans = new ArrayList<ContingencyPlan>();
        /*List<ContingencyPlan> contingencyPlans = Arrays.asList(
                new ContingencyPlan(
                        Arrays.asList("ENVIRONMENTAL", "LOST_NAV"),
                        "",
                        new ContingencyPolygon(
                                "Polygon",
                                new ArrayList<List<List<Double>>>()
                        ),
                        "LANDING",
                        "",
                        -1,
                        -1,
                        Arrays.asList(1, 0),
                        formatDateForOperationObject(operation.getStartDatetime()),
                        formatDateForOperationObject(operation.getEndDatetime())
                )
        );*/
        List<OperationVolume> operationVolumes = Arrays.asList(
                new OperationVolume(
                    formatDateForOperationObject(operation.getStartDatetime()),
                        formatDateForOperationObject(operation.getEndDatetime()),
                        -1,
                        operation.getMaxAltitude(),
                        new OperationGeography(
                                "Polygon",
                                Arrays.asList(
                                        polygonCoordinates
                                )
                        ),
                        true
                )
        );
        List<com.dronfies.portableutmandroidclienttest.NegotiationAgreement> negotiationAgreements = Arrays.asList(
                new com.dronfies.portableutmandroidclienttest.NegotiationAgreement(
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                )
        );
        return new Operation(
                operation.getDescription(),
                "",
                "Simple polygon",
                "",
                formatDateForOperationObject(submitDate),
                formatDateForOperationObject(submitDate),
                0,
                2,
                false,
                operation.getPilotName(),
                operation.getContactPhone(),
                null,
                operation.getOwner(),
                uasRegistrations,
                priorityElements,
                contingencyPlans,
                operationVolumes,
                negotiationAgreements
        );
    }

    private com.dronfies.portableutmandroidclienttest.entities.Operation getOperationFromJsonObject(JsonObject jsonObject) throws Exception {
        String id = jsonObject.get("gufi").getAsString();
        String description = jsonObject.get("name").getAsString();
        String flightComments = jsonObject.get("flight_comments").getAsString();
        String pilotName = "";
        try{
            pilotName = jsonObject.get("contact").getAsString();
        }catch(Exception ex){}
        String contactPhone = "";
        try{
            contactPhone = jsonObject.get("contact_phone").getAsString();
        }catch(Exception ex){}
        String droneId = null;
        String droneDescription = null;
        try{
            droneId = jsonObject.get("uas_registrations").getAsJsonArray().get(0).getAsJsonObject().get("uvin").getAsString();
            droneDescription = jsonObject.get("uas_registrations").getAsJsonArray().get(0).getAsJsonObject().get("vehicleName").getAsString();
        }catch(Exception ex){}
        JsonArray jsonArrayOperationVolumes = jsonObject.get("operation_volumes").getAsJsonArray();
        if(jsonArrayOperationVolumes.size() != 1){
            throw new Exception("operacion invalida");
        }
        JsonObject jsonObjectOperationVolume = jsonArrayOperationVolumes.get(0).getAsJsonObject();
        String strEffectiveTimeBegin = jsonObjectOperationVolume.get("effective_time_begin").getAsString();
        String strEffectiveTimeEnd = jsonObjectOperationVolume.get("effective_time_end").getAsString();
        int maxAltitude = (int) Math.round(jsonObjectOperationVolume.get("max_altitude").getAsDouble());
        com.dronfies.portableutmandroidclienttest.entities.Operation.EnumOperationState state = com.dronfies.portableutmandroidclienttest.entities.Operation.EnumOperationState.valueOf(jsonObject.get("state").getAsString());
        String owner = jsonObject.getAsJsonObject("owner").get("username").getAsString();
        // parse polygon
        List<GPSCoordinates> polygon = new ArrayList<>();
        try{
            JsonArray coordinates = jsonObjectOperationVolume.getAsJsonObject("operation_geography").getAsJsonArray("coordinates").get(0).getAsJsonArray();
            for(int i = 0; i < coordinates.size(); i++){
                // the order in the jsonObject is (longitude, latitude)
                GPSCoordinates latLng = new GPSCoordinates(
                        coordinates.get(i).getAsJsonArray().get(1).getAsDouble(),
                        coordinates.get(i).getAsJsonArray().get(0).getAsDouble()
                );
                polygon.add(latLng);
            }
        }catch(Exception ex){}
        // return operation
        return new com.dronfies.portableutmandroidclienttest.entities.Operation(
                id,
                description,
                polygon,
                parseDate(strEffectiveTimeBegin),
                parseDate(strEffectiveTimeEnd),
                maxAltitude,
                pilotName,
                contactPhone,
                droneId,
                droneDescription,
                state,
                owner,
                flightComments
        );
    }

    private Date parseDate(String strDatetime) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(strDatetime.replaceAll("T", " ").replaceAll("Z", ""));
    }

    private String formatDateForOperationObject(Date date){
        // example: 2019-12-11T19:59:10Z
        return new SimpleDateFormat("yyyy-MM-dd").format(date) + "T" + new SimpleDateFormat("HH:mm:ss").format(date) + "Z";
    }

    private Vehicle parseVehicle(JSONObject jsonObjectVehicle) throws Exception {
        String uvin = getStringValueFromJSONObject(jsonObjectVehicle, "uvin");
        Date date = null;
        if(jsonObjectVehicle.has("date") && !jsonObjectVehicle.isNull("date")){
            date = parseDate(jsonObjectVehicle.getString("date"));
        }
        String nNumber = getStringValueFromJSONObject(jsonObjectVehicle, "nNumber");
        String faaNumber = getStringValueFromJSONObject(jsonObjectVehicle, "faaNumber");
        String vehicleName = getStringValueFromJSONObject(jsonObjectVehicle, "vehicleName");
        String manufacturer = getStringValueFromJSONObject(jsonObjectVehicle, "manufacturer");
        String model = getStringValueFromJSONObject(jsonObjectVehicle, "model");
        String strVehicleClass = getStringValueFromJSONObject(jsonObjectVehicle, "class");
        Vehicle.EnumVehicleClass vehicleClass = null;
        if(strVehicleClass != null){
            if(strVehicleClass.equalsIgnoreCase("MULTIROTOR")){
                vehicleClass = Vehicle.EnumVehicleClass.MULTIROTOR;
            }else if(strVehicleClass.equalsIgnoreCase("FIXEDWING")){
                vehicleClass = Vehicle.EnumVehicleClass.FIXEDWING;
            }
        }
        String registeredBy = null;
        if(jsonObjectVehicle.has("registeredBy") && !jsonObjectVehicle.isNull("registeredBy")){
            registeredBy = getStringValueFromJSONObject(jsonObjectVehicle.getJSONObject("registeredBy"), "username");
        }
        String owner = null;
        if(jsonObjectVehicle.has("owner") && !jsonObjectVehicle.isNull("owner")){
            owner = getStringValueFromJSONObject(jsonObjectVehicle.getJSONObject("owner"), "username");
        }
        return new Vehicle(uvin, date, nNumber, faaNumber, vehicleName, manufacturer, model, vehicleClass, registeredBy, owner);
    }

    private RestrictedFlightVolume parseRFV(JSONObject jsonObjectRFV) throws Exception {
        String id = getStringValueFromJSONObject(jsonObjectRFV, "id");
        int minAltitude = Integer.parseInt(getStringValueFromJSONObject(jsonObjectRFV, "min_altitude"));
        int maxAltitude = Integer.parseInt(getStringValueFromJSONObject(jsonObjectRFV, "max_altitude"));
        String comments = getStringValueFromJSONObject(jsonObjectRFV, "comments");
        List<LatLng> polygon = new ArrayList<>();
        JSONArray jsonArrayCoordinates = (JSONArray) jsonObjectRFV.getJSONObject("geography").getJSONArray("coordinates").get(0);
        for(int i = 0; i < jsonArrayCoordinates.length(); i++){
            double lat = ((JSONArray)jsonArrayCoordinates.get(i)).getDouble(1);
            double lng = ((JSONArray)jsonArrayCoordinates.get(i)).getDouble(0);
            polygon.add(new LatLng(lat, lng));
        }
        return new RestrictedFlightVolume(id, polygon, minAltitude, maxAltitude, comments);
    }

    private String getStringValueFromJSONObject(JSONObject jsonObject, String key) throws Exception {
        if(!jsonObject.has(key) || jsonObject.isNull(key)){
            return null;
        }
        return jsonObject.getString(key);
    }

    private void handleErrorResponse(Response response) throws Exception {
        switch (response.code()){
            case 400:
                throw new BadRequestException("400: Bad Request");
            case 404:
                throw new NotFoundException("404: Not Found");
            default:
              throw new Exception("500: Internal server error");
        }
    }
}

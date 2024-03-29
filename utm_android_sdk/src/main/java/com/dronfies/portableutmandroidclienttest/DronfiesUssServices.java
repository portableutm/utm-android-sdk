package com.dronfies.portableutmandroidclienttest;

import android.util.Log;

import com.dronfies.portableutmandroidclienttest.entities.ExtraField;
import com.dronfies.portableutmandroidclienttest.entities.GPSCoordinates;
import com.dronfies.portableutmandroidclienttest.entities.ICompletitionCallback;
import com.dronfies.portableutmandroidclienttest.entities.IGenericCallback;
import com.dronfies.portableutmandroidclienttest.entities.OperationStateUpdate;
import com.dronfies.portableutmandroidclienttest.exception.BadRequestException;
import com.dronfies.portableutmandroidclienttest.exception.NotFoundException;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketIOException;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Part;

public class DronfiesUssServices {

    // Singleton Pattern
    private static DronfiesUssServices INSTANCE = null;

    private final IRetrofitAPI api;

    private String authToken = null;
    private String mUsername = null;

    private static String utmEndpoint = null;

    private Map<String, Socket> mMapSockets = new HashMap<>();

    public DronfiesUssServices(IRetrofitAPI api) {
        this.api = api;
    }

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
    @Deprecated
    public static DronfiesUssServices getUnsafeInstanceDONOTUSE(String utmEndpoint){
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

                OkHttpClient okHttpClient = new UnsafeOkHttpClient().getUnsafeOkHttpClient().newBuilder()
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

                INSTANCE = new DronfiesUssServices(retrofit.create(IRetrofitAPI.class));
            }catch(Exception ex){}
        }
        return INSTANCE;
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

    public User getUserInfo() throws IOException {
        Response<User> response = api.getUser(authToken,mUsername).execute();
        return response.body();
    }

    public String addExpressOperation_sync(ExpressOperationData data) throws Exception {
        Response<ResponseBody> response = api.addExpressOperation(authToken,data).execute();
        return new JSONObject(response.body().string()).get("gufi").toString();
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

    public void sendPilotPosition(double lon, double lat, double alt, String operationId, String droneId, final ICompletitionCallback<String> callback){
        String timeSent = Instant.now().toString();
        PilotPosition pilotPosition = new PilotPosition(
                (int) Math.round(alt),
                new Location(
                        "Point",
                        new double[]{lon, lat}
                ),
                timeSent,
                operationId,
                droneId
        );
        api.sendPilotPosition(authToken, pilotPosition).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + " ("+response.getClass()+")");
                    return;
                }

                callback.onResponse(response.body().toString() + " ("+response.getClass()+")", null);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    public void sendPilotPosition_sync(double lon, double lat, double alt, String operationId, String droneId) throws Exception{
        String timeSent = Instant.now().toString();
        PilotPosition pilotPosition = new PilotPosition(
                (int)Math.round(alt),
                new Location(
                        "Point",
                        new double[]{lon, lat}
                ),
                timeSent,
                operationId,
                droneId
        );
        Response response = api.sendPilotPosition(authToken, pilotPosition).execute();
        if(!response.isSuccessful()){
            throw new Exception(String.format("[HTTP %d] %s", response.code(), response.errorBody().string()));
        }
    }

    public void sendPosition(double lon, double lat, double alt, double heading, String operationId, final ICompletitionCallback<String> callback){
        String timeSent = Instant.now().toString();
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
        String timeSent = Instant.now().toString();
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
        String timeSent = Instant.now().toString() ;
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
        getOperations(null, null,null, callback);
    }

    public void getOperations(Integer limit, Integer offset, List<String> states, final ICompletitionCallback<List<com.dronfies.portableutmandroidclienttest.entities.Operation>> callback) throws NoAuthenticatedException {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        Call<Object> call = api.getOperations(authToken);
        if(limit != null && offset != null){
            call = api.getOperations(authToken, limit, offset);
            if (states != null && !states.isEmpty()) {
                JSONArray arrayStates = new JSONArray();
                for (int i = 0; i < states.size(); i++) {
                    arrayStates.put(states.get(i));
                }
                call = api.getOperations(authToken,limit, offset, arrayStates.toString());
            }
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

    public void getVehicles(Integer take, Integer skip, final ICompletitionCallback<List<Vehicle>> callback) throws Exception {
        getVehicles("","",take, skip,false, callback);
    }
    public void getVehicles(String filterBy, String filter,Integer take, Integer skip, final ICompletitionCallback<List<Vehicle>> callback) throws Exception {
        getVehicles(filterBy, filter,take, skip,false, callback);
    }

    public void getOperatorVehicles(Integer take, Integer skip, final ICompletitionCallback<List<Vehicle>> callback) throws Exception {
        getVehicles("","",take, skip,true, callback);
    }
    public void getOperatorVehicles(String filterBy,String filter,Integer take, Integer skip, final ICompletitionCallback<List<Vehicle>> callback) throws Exception {
        getVehicles(filterBy,filter,take, skip,true, callback);
    }

    public Vehicle getVehicleById(String id) throws Exception {
        String responseBody = api.getVehicleById(authToken,id).execute().body().string();
        JSONObject jsonVehicles = new JSONObject(responseBody);
        return parseVehicle(jsonVehicles);
    }

    public void addVehicle(Vehicle vehicle, Map<ExtraField, Object> mapExtraFieldValues) throws Exception {
        JsonObject jsonObjectExtraFields = new JsonObject();
        List<MultipartBody.Part> files = new ArrayList<>();
        for(Map.Entry<ExtraField, Object> entry : mapExtraFieldValues.entrySet()){
            ExtraField extraField = entry.getKey();
            if(extraField.getType() == ExtraField.EnumExtraFieldType.STRING){
                jsonObjectExtraFields.addProperty(extraField.getName(), (String)entry.getValue());
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.DATE){
                jsonObjectExtraFields.addProperty(extraField.getName(), formatDateForOperationOrVehicleObject((Date)entry.getValue()));
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.BOOL){
                jsonObjectExtraFields.addProperty(extraField.getName(), (Boolean)entry.getValue());
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.NUMBER){
                jsonObjectExtraFields.addProperty(extraField.getName(), (Double)entry.getValue());
            }else if(extraField.getType() == ExtraField.EnumExtraFieldType.FILE){
                String filepath = (String)entry.getValue();
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), new File(filepath));
                MultipartBody.Part part = MultipartBody.Part.createFormData(extraField.getName(), filepath, requestBody);
                files.add(part);
            }
        }
        String strExtraFields = jsonObjectExtraFields.toString();

        validateVehicle(vehicle);

        String ownerId = getUsername();

        Response<ResponseBody> response = api.postVehicle(
            authToken,
            files,
            getRequestBody(vehicle.getDate().toString()),
            getRequestBody(vehicle.getnNumber()),
            getRequestBody(vehicle.getFaaNumber()),
            getRequestBody(vehicle.getVehicleName()),
            getRequestBody(vehicle.getManufacturer()),
            getRequestBody(vehicle.getModel()),
            getRequestBody(vehicle.getVehicleClass().name()),
            getRequestBody(ownerId),
            getRequestBody(strExtraFields)
        ).execute();
        if(!response.isSuccessful()){
            throw new Exception(String.format("%d: %s", response.code(), response.message()));
        }
    }

    public List<RestrictedFlightVolume> getRestrictedFlightVolumes() throws Exception {
        String responseBody = api.getRestrictedFlightVolumes(authToken).execute().body().string();
        JSONObject object = new JSONObject(responseBody);
        JSONArray jsonArrayRFVs = object.getJSONArray("rfvs");
        List<RestrictedFlightVolume> ret = new ArrayList<>();
        for(int i = 0; i  < jsonArrayRFVs.length(); i++){
            JSONObject jsonObject = jsonArrayRFVs.getJSONObject(i);
            ret.add(parseRFV(jsonObject));
        }
        return ret;
    }

    public List<UASVolumeReservation> getUASVolumes() throws Exception{
        String responseBody = api.getUASVolume(authToken).execute().body().string();
        JSONObject object = new JSONObject(responseBody);
        JSONArray jsonArrayUVRs = object.getJSONArray("uvrs");
        List<UASVolumeReservation> ret = new ArrayList<>();
        for(int i = 0; i  < jsonArrayUVRs.length(); i++){
            JSONObject jsonObject = jsonArrayUVRs.getJSONObject(i);
            ret.add(parseUVR(jsonObject));
        }
        return ret;
    }

    public UASVolumeReservation getUASVolumeReservationById_sync(String id) throws Exception {
        String responseBody = api.getUASVolumeById(authToken,id).execute().body().string();
        JSONObject jsonUVR = new JSONObject(responseBody);
        return parseUVR(jsonUVR);
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

    public Tracker getTrackerInformation(String trackerId) throws Exception {
        ResponseBody responseBody = null;
        responseBody = api.getTrackerById(authToken,trackerId).execute().body();
        if (responseBody == null){
            return null;
        }
        JSONObject json = new JSONObject(responseBody.string());
        try {
            if (json.getJSONObject("vehicle") != null){
                return new Tracker(json.getString("hardware_id"),parseVehicle(json.getJSONObject("vehicle")));
            }
        } catch (JSONException e) {
            return new Tracker(json.getString("hardware_id"), parseDirectory(json.getJSONArray("directory")));
        }
        return null;
    }

    public void registerTracker(String trackerId, String uvin) throws IOException {
        Tracker tracker = new Tracker(trackerId,uvin);
        api.registerTracker(authToken, tracker).execute();
    }

    public String connectToOperationUpdates(String operationId, IGenericCallback<TrackerPosition> callback) throws NoAuthenticatedException {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        try {
            IO.Options options = new IO.Options();
            options.query = String.format("token=%s", authToken);
            Socket socket = IO.socket(String.format("%s/private", utmEndpoint), options);
            String eventName = String.format("new-tracker-position[gufi=%s]", operationId);
            socket.on(eventName, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    try{
                        JSONObject jsonObject = (JSONObject)objects[0];
                        double latitude = jsonObject.getDouble("latitude");
                        double longitude = jsonObject.getDouble("longitude");
                        double altitude = jsonObject.getDouble("altitude");
                        double heading = jsonObject.getDouble("heading");
                        Date time_sent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(jsonObject.getString("time_sent").replaceAll("T", " ").replaceAll("Z", ""));
                        TrackerPosition trackerPosition = new TrackerPosition(latitude, longitude, altitude, heading, time_sent);
                        callback.onCallbackExecution(trackerPosition, null);
                    }catch (Exception ex){
                        callback.onCallbackExecution(null, ex.getMessage());
                    }
                }
            });
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }
            });
            socket.connect();
            String ret = UUID.randomUUID().toString();
            mMapSockets.put(ret, socket);
            return ret;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String connectToTrackerPositionUpdates(String operationId, IGenericCallback<TrackerPosition> callback) throws NoAuthenticatedException {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        try {
            IO.Options options = new IO.Options();
            options.query = String.format("token=%s", authToken);
//            options.transports = new String[] { WebSocket.NAME };
            Socket socket = IO.socket(String.format("%s/private", utmEndpoint), options);
            String eventName = String.format("new-tracker-position[gufi=%s]", operationId);
            socket.on(eventName, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    try{
                        JSONObject jsonObject = (JSONObject)objects[0];
                        double latitude = jsonObject.getDouble("latitude");
                        double longitude = jsonObject.getDouble("longitude");
                        double altitude = jsonObject.getDouble("altitude");
                        double heading = jsonObject.getDouble("heading");
                        Date time_sent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(jsonObject.getString("time_sent").replaceAll("T", " ").replaceAll("Z", ""));
                        TrackerPosition trackerPosition = new TrackerPosition(latitude, longitude, altitude, heading, time_sent);
                        callback.onCallbackExecution(trackerPosition, null);
                    }catch (Exception ex){
                        callback.onCallbackExecution(null, ex.getMessage());
                    }
                }
            });
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }
            });
            socket.connect();
            String ret = UUID.randomUUID().toString();
            mMapSockets.put(ret, socket);
            return ret;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String connectToOperationStateUpdates(String operationId, IGenericCallback<OperationStateUpdate> callback) throws NoAuthenticatedException {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        try {
            IO.Options options = new IO.Options();
            options.query = String.format("token=%s", authToken);
            Socket socket = IO.socket(String.format("%s/private", utmEndpoint), options);
            String eventName = String.format("operation-state[gufi=%s]", operationId);
            socket.on(eventName, objects -> {
                try{
                    JSONObject jsonObject = (JSONObject)objects[0];
                    com.dronfies.portableutmandroidclienttest.entities.Operation.EnumOperationState state = com.dronfies.portableutmandroidclienttest.entities.Operation.EnumOperationState.valueOf(jsonObject.getString("state"));
                    String message = jsonObject.getString("message");
                    callback.onCallbackExecution(new OperationStateUpdate(state, message), null);
                }catch (Exception ex){
                    callback.onCallbackExecution(null, ex.getMessage());
                }
            }).on(Socket.EVENT_CONNECT_ERROR, args -> {
                callback.onCallbackExecution(null, getErrorDescription("EVENT_CONNECT_ERROR", args));
            }).on(Socket.EVENT_ERROR, args -> {
                callback.onCallbackExecution(null, getErrorDescription("EVENT_ERROR", args));
            });
            socket.connect();
            String ret = UUID.randomUUID().toString();
            mMapSockets.put(ret, socket);
            return ret;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnectFromUpdates(String ref){
        try{
            mMapSockets.get(ref).disconnect();
            mMapSockets.remove(ref);
        }catch (Exception ex){}
    }

    public void disconnectFromTrackerPositionUpdates(String ref){
        try{
            mMapSockets.get(ref).disconnect();
            mMapSockets.remove(ref);
        }catch (Exception ex){}
    }

    public List<ExtraField> getUserExtraFields() throws Exception {
        return getExtraFields("userExtraFields");
    }

    public List<ExtraField> getVehicleExtraFields() throws Exception {
        return getExtraFields("vehicleExtraFields");
    }

    //----------------------------------------------------------------------------------------------------
    //----------------------------------------- PRIVATE METHODS  -----------------------------------------
    //----------------------------------------------------------------------------------------------------

    private String getErrorDescription(String errorName, Object[] args){
        String strArgs = "";
        if(args != null){
            strArgs += "[";
            for(int i = 0; i < args.length; i++){
                strArgs += args[i] + ", ";
            }
            if(strArgs.endsWith(", ")) strArgs = strArgs.substring(0, strArgs.length() - 2);
            strArgs += "]";
        }
        return errorName + " (args: "+strArgs+")";
    }

    private List<Directory> parseDirectory(JSONArray jsonArr) throws JSONException {
        List<Directory> dir = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            String uvin = jsonArr.getJSONObject(i).getString("uvin");
            String endpoint = jsonArr.getJSONObject(i).getString("endpoint");
            dir.add(new Directory(uvin, endpoint));
        }
        return dir;
    }

    private List<ExtraField> getExtraFields(String rootElement) throws Exception {
        String schemasStr = api.getSchemas().execute().body().string();
        JsonElement jsonElementSchema = new JsonParser().parse(schemasStr);
        JsonElement jsonElementUserExtraFields = jsonElementSchema.getAsJsonObject().get(rootElement);
        List<ExtraField> ret = new ArrayList<>();
        for(Map.Entry<String, JsonElement> entry : jsonElementUserExtraFields.getAsJsonObject().entrySet()){
            boolean required = entry.getValue().getAsJsonObject().get("required").toString().trim().equalsIgnoreCase("true");
            String strType = entry.getValue().getAsJsonObject().get("type").toString().trim();
            strType = strType.substring(1, strType.length()-1);
            ExtraField.EnumExtraFieldType type = ExtraField.EnumExtraFieldType.valueOf(strType.toUpperCase());
            ret.add(new ExtraField(entry.getKey(), type, required));
        }
        return ret;
    }


    private void getVehicles(String filterBy, String filter,Integer take, Integer skip, boolean fromOperator, final ICompletitionCallback<List<Vehicle>> callback) throws Exception {
        if(authToken == null || mUsername == null){
            throw new NoAuthenticatedException("You must call login method, before calling this method");
        }
        getUserExtraFields();
        Call<ResponseBody> call;
        if(fromOperator){
            if(filterBy.isEmpty()) {
                call = api.getOperatorVehicles(authToken, take, skip);
            } else {
                call = api.getOperatorVehicles(authToken, take, skip, filterBy, filter);
            }
        }else{
            if (filterBy.isEmpty()){
                call = api.getVehicles(authToken, take, skip);
            } else {
                call = api.getVehicles(authToken, take, skip, filterBy, filter);
            }
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    callback.onResponse(null, response.code() + " ("+response.getClass()+")");
                    return;
                }
                List<Vehicle> listVehicles = new ArrayList<>();
                String responseBody = null;
                try {
                    responseBody = response.body().string();
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONArray jsonArrayVehicles = responseObject.getJSONArray("vehicles");
                    for(int i = 0; i < jsonArrayVehicles.length(); i++){
                        JSONObject jsonObject = jsonArrayVehicles.getJSONObject(i);
                        listVehicles.add(parseVehicle(jsonObject));
                    }

                    callback.onResponse(listVehicles, null);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onResponse(null, t.getMessage());
            }
        });
    }

    private Operation transformOperation(com.dronfies.portableutmandroidclienttest.entities.Operation operation){
        List<List<Double>> polygonCoordinates = new ArrayList<>();
        for(GPSCoordinates latLng : operation.getPolygon()){
            // for the backend, we have to send (longitude, latitude)
            polygonCoordinates.add(Arrays.asList(latLng.getLongitude(), latLng.getLatitude()));
        }

        String submitDate = Instant.now().toString();
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
                    formatDateForOperationOrVehicleObject(operation.getStartDatetime()),
                        formatDateForOperationOrVehicleObject(operation.getEndDatetime()),
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
                operation.getFlightComments(),
                "Simple polygon",
                submitDate,
                submitDate,
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
        String flightComments = "";
        try{
            flightComments = jsonObject.get("flight_comments").getAsString();
        } catch (Exception ex) { }
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(strDatetime.replaceAll("T", " ").replaceAll("Z", ""));
    }

    private String formatDateForOperationOrVehicleObject(Date date){
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
        Vehicle.EnumVehicleAuthorization authorization = null;
        try{
            authorization = Vehicle.EnumVehicleAuthorization.valueOf(jsonObjectVehicle.getString("authorized").toUpperCase().trim());
        }catch (Exception ex){}
        return new Vehicle(uvin, date, nNumber, faaNumber, vehicleName, manufacturer, model, vehicleClass, registeredBy, owner, authorization);
    }

    private UASVolumeReservation parseUVR(JSONObject jsonObjectUVR) throws Exception {
        String message_id = getStringValueFromJSONObject(jsonObjectUVR, "message_id");
        int minAltitude = Integer.parseInt(getStringValueFromJSONObject(jsonObjectUVR, "min_altitude"));
        int maxAltitude = Integer.parseInt(getStringValueFromJSONObject(jsonObjectUVR, "max_altitude"));
        String reason = getStringValueFromJSONObject(jsonObjectUVR, "reason");
        Date begin = parseDate(getStringValueFromJSONObject(jsonObjectUVR, "effective_time_begin"));
        Date end = parseDate(getStringValueFromJSONObject(jsonObjectUVR, "effective_time_end"));
        String cause = getStringValueFromJSONObject(jsonObjectUVR, "cause");
        String type = getStringValueFromJSONObject(jsonObjectUVR, "type");
        List<LatLng> polygon = new ArrayList<>();
        JSONArray jsonArrayCoordinates = (JSONArray) jsonObjectUVR.getJSONObject("geography").getJSONArray("coordinates").get(0);
        for(int i = 0; i < jsonArrayCoordinates.length(); i++){
            double lat = ((JSONArray)jsonArrayCoordinates.get(i)).getDouble(1);
            double lng = ((JSONArray)jsonArrayCoordinates.get(i)).getDouble(0);
            polygon.add(new LatLng(lat, lng));
        }
        return new UASVolumeReservation(message_id,type,cause, polygon,begin,end,minAltitude,maxAltitude,reason);


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

//    private Tracker parseTracker(JSONObject jsonTracker) throws JSONException {
//        String hardwareId = jsonTracker.getString("hardware_id");
//        Directory directory = new Directory();
//        if (jsonTracker.getString("hardware_id") != null){
//            Tracker tracker = new Tracker(hardwareId);
//
//        }
//
//    }

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

    private JsonObject vehicleToJsonObject(Vehicle vehicle){
        JsonObject ret = new JsonObject();
        ret.addProperty("date", formatDateForOperationOrVehicleObject(vehicle.getDate()));
        ret.addProperty("nNumber", vehicle.getnNumber());
        ret.addProperty("faaNumber", vehicle.getFaaNumber());
        ret.addProperty("vehicleName", vehicle.getVehicleName());
        ret.addProperty("manufacturer", vehicle.getManufacturer());
        ret.addProperty("model", vehicle.getModel());
        ret.addProperty("class", vehicle.getVehicleClass().name());
        JsonObject jsonObjectOwner = new JsonObject();
        jsonObjectOwner.addProperty("username", getUsername());
        ret.add("date", jsonObjectOwner);
        return ret;
    }

    private RequestBody getRequestBody(String value){
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private void validateVehicle(Vehicle vehicle) throws Exception{
        if(vehicle.getVehicleName() == null || vehicle.getVehicleName().length() < 2 || vehicle.getVehicleName().length() > 255){
            throw new Exception("vehicleName can't be null, and must have 2 to 255 characters");
        }
    }
}

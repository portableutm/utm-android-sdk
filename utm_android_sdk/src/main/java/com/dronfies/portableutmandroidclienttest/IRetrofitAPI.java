package com.dronfies.portableutmandroidclienttest;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface IRetrofitAPI {

    @POST("auth/login")
    Call<String> login(@Body User user);

    @POST("operation")
    Call<ResponseBody> addOperation(@Header("auth") String authToken, @Body Operation operation);

    @POST("pilotPosition")
    Call<Object> sendPilotPosition(@Header("auth") String authToken, @Body PilotPosition pilotPosition);

    @POST("position")
    Call<Object> sendPosition(@Header("auth") String authToken, @Body Position position);

    @POST("paraglidingposition")
    Call<Object> sendParaglidingPosition(@Header("auth") String authToken, @Body ParaglidingPosition position);

    @GET("operation/owner")
    Call<Object> getOperations(@Header("auth") String authToken);

    @GET("operation/owner")
    Call<Object> getOperations(@Header("auth") String authToken, @Query("take") int limit, @Query("skip") int offset);

    @GET("operation/{id}")
    Call<Object> getOperationById(@Header("auth") String authToken, @Path("id") String id);

    @DELETE("operation/{id}")
    Call<Object> deleteOperation(@Header("auth") String authToken, @Path("id") String id);

    @GET("vehicle")
    Call<ResponseBody> getVehicles(@Header("auth") String authToken, @Query("take") int take, @Query("skip") int skip);

    @GET("vehicle")
    Call<ResponseBody> getVehicles(@Header("auth") String authToken, @Query("take") int take, @Query("skip") int skip, @Query("filterBy") String filterBy, @Query("filter") String filter);

    @GET("vehicle/operator")
    Call<ResponseBody> getOperatorVehicles(@Header("auth") String authToken,  @Query("take") int take, @Query("skip") int skip);

    @GET("vehicle/operator")
    Call<ResponseBody> getOperatorVehicles(@Header("auth") String authToken,  @Query("take") int take, @Query("skip") int skip, @Query("filterBy") String filterBy, @Query("filter") String filter);

    @GET("vehicle/{id}")
    Call<ResponseBody> getVehicleById(@Header("auth") String authToken, @Path("id") String id);

    @Multipart
    @POST("vehicle")
    Call<ResponseBody> postVehicle(
            @Header("auth") String authToken,
            @Part List<MultipartBody.Part> files,
            @Part("date") RequestBody date,
            @Part("nNumber") RequestBody nNumber,
            @Part("faaNumber") RequestBody faaNumber,
            @Part("vehicleName") RequestBody vehicleName,
            @Part("manufacturer") RequestBody manufacturer,
            @Part("model") RequestBody model,
            @Part("class") RequestBody clazz,
            @Part("owner_id") RequestBody ownerId,
            @Part("extra_fields_str") RequestBody serialNumber
    );

    @GET("restrictedflightvolume")
    Call<ResponseBody> getRestrictedFlightVolumes(@Header("auth") String authToken);

    @GET("uasvolume")
    Call<ResponseBody> getUASVolume(@Header("auth") String authToken);

    @GET("endpoints")
    Call<ResponseBody> getEndpoints();

    @GET("endpoint/{username}")
    Call<ResponseBody> getEndpoint(@Header("auth") String authToken, @Path("username") String username);

    @POST("operation/{id}/updatestate")
    Call<ResponseBody> updateOperationState(@Header("auth") String authToken, @Path("id") String id, @Body UpdateStateRequestBody request);

    @Multipart
    @POST("decrypt")
    Call<ResponseBody> uploadDatFile(@Part MultipartBody.Part part, @Part("operationId") RequestBody requestBody);

    @POST("operation/express")
    Call<ResponseBody> addExpressOperation(@Header("auth") String authToken, @Body ExpressOperationData data);

    @GET("trackers/{id}")
    Call<ResponseBody> getTrackerById(@Header("auth") String authToken, @Path("id") String id);

    @POST("trackers")
    Call<ResponseBody> registerTracker(@Header("auth") String authToken, @Body Tracker data);

    @GET("user/{id}")
    Call<User> getUser(@Header("auth") String authToken, @Path("id") String id);

    @GET("schemas")
    Call<ResponseBody> getSchemas();

}

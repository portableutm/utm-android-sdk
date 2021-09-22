package com.dronfies.portableutmandroidclienttest;

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
    Call<Object> getOperations(@Header("auth") String authToken, @Query("limit") int limit, @Query("offset") int offset);

    @GET("operation/{id}")
    Call<Object> getOperationById(@Header("auth") String authToken, @Path("id") String id);

    @DELETE("operation/{id}")
    Call<Object> deleteOperation(@Header("auth") String authToken, @Path("id") String id);

    @GET("vehicle")
    Call<ResponseBody> getVehicles(@Header("auth") String authToken);

    @GET("vehicle/{id}")
    Call<ResponseBody> getVehicleById(@Header("auth") String authToken, @Path("id") String id);

    @GET("restrictedflightvolume")
    Call<ResponseBody> getRestrictedFlightVolumes(@Header("auth") String authToken);

    @GET("endpoints")
    Call<ResponseBody> getEndpoints();

    @GET("endpoint/{username}")
    Call<ResponseBody> getEndpoint(@Header("auth") String authToken, @Path("username") String username);

    @POST("operation/{id}/updatestate")
    Call<ResponseBody> updateOperationState(@Header("auth") String authToken, @Path("id") String id, @Body UpdateStateRequestBody request);

    @Multipart
    @POST("decrypt")
    Call<ResponseBody> uploadDatFile(@Part MultipartBody.Part part, @Part("operationId") RequestBody requestBody);
}

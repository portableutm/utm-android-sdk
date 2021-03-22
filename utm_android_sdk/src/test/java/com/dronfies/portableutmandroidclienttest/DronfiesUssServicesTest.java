package com.dronfies.portableutmandroidclienttest;

import com.dronfies.portableutmandroidclienttest.entities.ICompletitionCallback;
import com.dronfies.portableutmandroidclienttest.entities.Operation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.jupiter.api.Assertions.*;

class DronfiesUssServicesTest {

    private MockWebServer server = new MockWebServer();
    private DronfiesUssServices dronfiesUssServices;
    private static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    @BeforeEach
    void setUp() {
        try {
            server.start();
            HttpUrl url = server.url("/");
            String endpoint = String.valueOf(url);
            dronfiesUssServices = DronfiesUssServices.getInstance(endpoint);
            MockResponse responseAuth = new MockResponse();
            responseAuth
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody("123456789");
            server.enqueue(responseAuth);
            dronfiesUssServices.login_sync("emi", "1234");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void onFinish(){
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void isAuthenticated() {
        if(!dronfiesUssServices.isAuthenticated()){
            fail();
        }
    }

    @Test
    void getUsername() {
        String username = dronfiesUssServices.getUsername();
        assertEquals(username, "emi");
    }

    @Test
    void logout() {
        dronfiesUssServices.logout();
        if(dronfiesUssServices.isAuthenticated()){
            fail();
        }
    }

    @Test
    void addOperation_sync() {

    }

    @Test
    void sendPosition() {
    }

    @Test
    void sendPosition_sync() {
    }

    @Test
    void sendParaglidingPosition() {
    }

    @Test
    void getOperations() {
        String file = "src/test/resources/testAddOperation.json";
        try {
            String json = readFileAsString(file);

             MockResponse response = new MockResponse();
             response
                     .setResponseCode(HttpURLConnection.HTTP_OK)
                     .setBody(json);
            server.enqueue(response);

            Operation op = new Operation("THISID","description", )

            dronfiesUssServices.addOperation_sync(null);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void getOperationById_sync() {
        String file = "src/test/resources/testOperationById.json";
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);

            Operation op = dronfiesUssServices.getOperationById_sync("whateverOperationId");
            assertEquals(op.getDescription(), "testName");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void deleteOperation() {
    }

    @Test
    void getVehicles() {
        String file = "src/test/resources/testVehicles.json";
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);

            List<Vehicle> vehicles = dronfiesUssServices.getVehicles();
            assertEquals(vehicles.get(1).getUvin(), "e4278f4f-497c-4bde-b7a2-560db61f5420");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void getVehicleById() {
        String file = "src/test/resources/testVehicleById.json";
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);

            Vehicle vehicles = dronfiesUssServices.getVehicleById("someId");
            assertEquals(vehicles.getUvin(), "e4278f4f-497c-4bde-b7a2-560db61f5420");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void getRestrictedFlightVolumes() {
        String file = "src/test/resources/testRestictedFlightVolume.json";
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);

            List<RestrictedFlightVolume> rfv = dronfiesUssServices.getRestrictedFlightVolumes();
            assertEquals(rfv.get(0).getId(), "7285acf9-e773-4d84-a6c3-e170397e8a6e");
            assertEquals(rfv.get(0).getComments(), "SUCN - CANELONES");
            assertEquals(rfv.get(0).getMaxAltitude(), 120);
            assertEquals(rfv.get(0).getMinAltitude(), 0);
            assertEquals(rfv.get(0).getPolygon().get(0).latitude, -56.2613888888889);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
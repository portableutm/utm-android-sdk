package com.dronfies.portableutmandroidclienttest;

import com.dronfies.portableutmandroidclienttest.entities.GPSCoordinates;
import com.dronfies.portableutmandroidclienttest.entities.ICompletitionCallback;
import com.dronfies.portableutmandroidclienttest.entities.Operation;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;

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
    void login() {
        MockResponse responseAuth = new MockResponse();
        responseAuth
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("123456789");
        server.enqueue(responseAuth);
        String[] token = {null};
        dronfiesUssServices.login("emi", "1234", new ICompletitionCallback<String>() {
            @Override
            public void onResponse(String s, String errorMessage) {
                assertEquals(s,"123456789" );
                token[0] = s;
            }
        });
        while(token[0]==null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        String file = "src/test/resources/testAddOperation.json";
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);
            List<GPSCoordinates> list = new ArrayList<GPSCoordinates>();
            GPSCoordinates gps = new GPSCoordinates(10,20);
            list.add(gps);
            Operation op = new Operation("THISID","description", list, new Date(),new Date(), 0,"Renzo", "099123456", "300","Drone description", Operation.EnumOperationState.ACCEPTED,"Me", "none" );

            assertEquals(dronfiesUssServices.addOperation_sync(op),"7480bdfc-45f4-4a29-90cd-189ddf706409");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        //WITH 404 ERROR
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                    .setBody(json);
            server.enqueue(response);
            List<GPSCoordinates> list = new ArrayList<GPSCoordinates>();
            GPSCoordinates gps = new GPSCoordinates(10,20);
            list.add(gps);
            Operation op = new Operation("THISID","description", list, new Date(),new Date(), 0,"Renzo", "099123456", "300","Drone description", Operation.EnumOperationState.ACCEPTED,"Me", "none" );

            assertEquals(dronfiesUssServices.addOperation_sync(op),"7480bdfc-45f4-4a29-90cd-189ddf706409");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void sendPosition() {
        MockResponse response = new MockResponse();
        response
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
        server.enqueue(response);
        final String[] string = {null};
        dronfiesUssServices.sendPosition(-54, -36, 100, 180, "whateverOPId", new ICompletitionCallback<String>() {
            @Override
            public void onResponse(String s, String errorMessage) {
                string[0] = errorMessage;
            }
        });
        while(string[0]==null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void sendPosition_sync() {
        String file = "src/test/resources/testOperationById.json";
        try {
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);

            dronfiesUssServices.sendPosition_sync(-54,-36,100,180,"whateverOPId");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void sendParaglidingPosition() {
        MockResponse response = new MockResponse();
        response
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
        server.enqueue(response);
        final String[] string = {null};
        dronfiesUssServices.sendParaglidingPosition(-54, -36, 100, new ICompletitionCallback<String>() {
            @Override
            public void onResponse(String s, String errorMessage) {
                string[0] = errorMessage;
            }
        });
        while(string[0]==null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void getOperations() {
        String file = "src/test/resources/testOperations.json";
        try {
            String json = readFileAsString(file);
            MockResponse response = new MockResponse();
            response
                    .setBody(json);
            server.enqueue(response);

            final Object[] listOper = {null};
            final String[] error = {null};
            //ESTO NO ME ESTA FUNCIONANDO //FIXME
            dronfiesUssServices.getOperations(new ICompletitionCallback<List<Operation>>() {
                @Override
                public void onResponse(List<Operation> operations, String errorMessage) {
                    Operation op = operations.get(0);
                    assertEquals(op.getDroneDescription()," Alta de drone flujo");
                    assertEquals(op.getState(), Operation.EnumOperationState.CLOSED);
                    assertEquals(op.getDurationInHours(),0);
                    listOper[0] = operations;
                    error[0]= errorMessage;
                }
            });
            while(listOper[0] == null && error[0] == null){
                try{
                    Thread.sleep(1000);
                } catch (Exception e){}
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        String file = "src/test/resources/testOperationById.json";
        String[] error = {null};

        MockResponse response = new MockResponse();
        response
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
        server.enqueue(response);

        dronfiesUssServices.deleteOperation("whateverOperationId", new ICompletitionCallback<String>() {
            @Override
            public void onResponse(String s, String errorMessage) {
                error[0] = errorMessage;
            }
        });
        while(error[0] == null){
            try{
                Thread.sleep(1000);
            } catch (Exception e){}
        }
    }

    @Test
    void getVehicles() {
        String file = "src/test/resources/testVehicles.json";
        try {
            CountDownLatch lock = new CountDownLatch(1);
            String json = readFileAsString(file);

            MockResponse response = new MockResponse();
            response
                    .setResponseCode(HttpURLConnection.HTTP_OK)
                    .setBody(json);
            server.enqueue(response);
            dronfiesUssServices.getVehicles(10, 0, new ICompletitionCallback<List<Vehicle>>() {
                @Override
                public void onResponse(List<Vehicle> vehicles, String errorMessage) {
                    assertEquals(vehicles.get(1).getUvin(), "e4278f4f-497c-4bde-b7a2-560db61f5420");
                    lock.countDown();
                }
            });
            lock.wait();
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
            assertEquals(rfv.get(0).getPolygon().get(0).latitude,  -34.4972913705033);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testNotUsed() {
        ContingencyPlan cont = new ContingencyPlan(null,null,null,null,null,100,0,null, null, null);
        ContingencyPolygon contPol = new ContingencyPolygon(null,null);
    }

    @Test
    void i() {
        ContingencyPlan cont = new ContingencyPlan(null,null,null,null,null,100,0,null, null, null);
        ContingencyPolygon contPol = new ContingencyPolygon(null,null);
    }

}
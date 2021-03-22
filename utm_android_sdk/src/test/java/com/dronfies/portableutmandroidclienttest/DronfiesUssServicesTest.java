package com.dronfies.portableutmandroidclienttest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.jupiter.api.Assertions.*;

class DronfiesUssServicesTest {

    private MockWebServer server = new MockWebServer();
    private static String readFileAsString(String file)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    @BeforeAll
    void setUp() {
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getInstance() {
    }

    @Test
    void isAuthenticated() {
    }

    @Test
    void getUsername() {
    }

    @Test
    void logout() {
    }

    @Test
    void login() {
    }

    @Test
    void login_sync() {
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
        String file = "src/test/resources/testOperations.json";
        try {
            String json = readFileAsString(file);
             MockResponse response = new MockResponse();
             response
                     .setBody(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetOperations() {
    }

    @Test
    void getOperationById_sync() {
    }

    @Test
    void deleteOperation() {
    }

    @Test
    void getVehicles() {
    }

    @Test
    void getVehicleById() {
    }

    @Test
    void getRestrictedFlightVolumes() {
    }
}
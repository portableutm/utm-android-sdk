package com.dronfies.portableutmandroidclienttest;

public class Endpoint {

    private String name;
    private String backendEndpoint;
    private String frontendEndpoint;
    private String countryCode;

    public Endpoint(String name, String backendEndpoint, String frontendEndpoint, String countryCode) {
        this.name = name;
        this.backendEndpoint = backendEndpoint;
        this.frontendEndpoint = frontendEndpoint;
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackendEndpoint() {
        return backendEndpoint;
    }

    public void setBackendEndpoint(String backendEndpoint) {
        this.backendEndpoint = backendEndpoint;
    }

    public String getFrontendEndpoint() {
        return frontendEndpoint;
    }

    public void setFrontendEndpoint(String frontendEndpoint) {
        this.frontendEndpoint = frontendEndpoint;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}

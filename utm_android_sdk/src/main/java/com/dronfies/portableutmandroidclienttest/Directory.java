package com.dronfies.portableutmandroidclienttest;

public class Directory{
    public String uvin;
    public String endpoint;

    public Directory(String uvin, String endpoint) {
        this.uvin = uvin;
        this.endpoint = endpoint;
    }

    public String getUvin() {
        return uvin;
    }

    public void setUvin(String uvin) {
        this.uvin = uvin;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
package com.dronfies.portableutmandroidclienttest;

public class UpdateStateRequestBody {

    private String state;

    public UpdateStateRequestBody(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

package com.dronfies.portableutmandroidclienttest.entities;

public class OperationStateUpdate {

    private Operation.EnumOperationState state;
    private String message;

    public OperationStateUpdate(Operation.EnumOperationState state, String message) {
        this.state = state;
        this.message = message;
    }

    public Operation.EnumOperationState getState() {
        return state;
    }

    public void setState(Operation.EnumOperationState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

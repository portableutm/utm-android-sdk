package com.dronfies.portableutmandroidclienttest.entities;

public interface IGenericCallback<T> {

    void onCallbackExecution(T t, String errorMessage);
}

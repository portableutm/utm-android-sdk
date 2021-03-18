package com.dronfies.portableutmandroidclienttest.entities;

public interface ICompletitionCallback<T> {

    void onResponse(T t, String errorMessage);
}

package com.dronfies.portableutmandroidclienttest;

public class NoAuthenticatedException extends Exception {

    public NoAuthenticatedException(String message){
        super(message);
    }

    public NoAuthenticatedException(String message, Exception ex){
        super(message, ex);
    }
}

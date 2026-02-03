package com.spring.lica.client.datagokr;

public class DataGoKrApiException extends RuntimeException {

    public DataGoKrApiException(String message) {
        super(message);
    }

    public DataGoKrApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

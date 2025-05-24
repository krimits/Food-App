package com.fooddelivery.client.android.repository;

public interface ResponseCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage); // Or pass Exception e
}

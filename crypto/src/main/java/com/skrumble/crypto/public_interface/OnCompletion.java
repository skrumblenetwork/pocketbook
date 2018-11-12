package com.skrumble.crypto.public_interface;

public interface OnCompletion<T extends Object> {
    void onCompleted(boolean success, T object);
}

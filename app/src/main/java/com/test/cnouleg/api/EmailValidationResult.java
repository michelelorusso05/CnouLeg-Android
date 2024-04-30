package com.test.cnouleg.api;
@SuppressWarnings("unused")
public class EmailValidationResult {
    boolean exists;

    public EmailValidationResult() {}

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}

package com.test.cnouleg.api;

public class InsertionResult {
    String insertedId;
    String uploadDate;

    public InsertionResult() {}

    public String getInsertedId() {
        return insertedId;
    }

    public void setInsertedId(String insertedId) {
        this.insertedId = insertedId;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}

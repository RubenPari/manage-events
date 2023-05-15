package com.rubenpari.manageevents.config;

import com.rubenpari.manageevents.utils.Status;

public class ResponseObject {
    private String message;
    private Status status;

    public ResponseObject(String message, Status status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

package com.example.test.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestPluginModel {

    @XmlElement(name = "value")
    private String message;

    public RestPluginModel() {
    }

    public RestPluginModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
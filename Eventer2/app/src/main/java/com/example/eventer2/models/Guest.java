package com.example.eventer2.models;

public class Guest {
    private String name;
    private String number;
    private String arrival;
    private String userId;
    private String demoId;
    private String eventId;
    private String email;

    public Guest() {
    }

    public Guest(String name, String number, String arrival) {
        this.name = name;
        this.number = number;
        this.arrival = arrival;
    }

    public Guest(String name, String number, String arrival, String userId, String demoId, String eventId) {
        this.name = name;
        this.number = number;
        this.arrival = arrival;
        this.userId = userId;
        this.demoId = demoId;
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDemoId() {
        return demoId;
    }

    public void setDemoId(String demoId) {
        this.demoId = demoId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
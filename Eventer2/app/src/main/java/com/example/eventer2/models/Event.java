package com.example.eventer2.models;

import com.google.firebase.firestore.Exclude;

import androidx.annotation.NonNull;

public class Event {

    private String authorId;
    private String name;
    private String theme;
    private String location;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String eventLocation;

    @Exclude
    private String eventId;

    public <T extends Event> T returnId(@NonNull String id){
        this.eventId = id;
        return (T) this;
    }

    public Event() {
    }

    public Event(String authorId, String name, String theme, String location, String startDate, String endDate, String startTime, String endTime, String eventLocation) {
        this.authorId = authorId;
        this.name = name;
        this.theme = theme;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventLocation = eventLocation;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


}

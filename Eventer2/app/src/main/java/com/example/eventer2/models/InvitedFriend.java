package com.example.eventer2.models;

import androidx.annotation.NonNull;

public class InvitedFriend {
    private String name;
    private String number;
    private String eventId;
    private String userId;
    private String demoId;
    private boolean check;


    public InvitedFriend() {
    }

    public InvitedFriend(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public InvitedFriend(String name, String number, String eventId, String id) {
        this.name = name;
        this.number = number;
        this.demoId = id;
        this.eventId = eventId;
    }

    public InvitedFriend(String name, String number, String eventId, String demoId, String userId) {
        this.name = name;
        this.number = number;
        this.eventId = eventId;
        this.demoId = demoId;
        this.userId = userId;
    }


    public <T extends InvitedFriend> T returnId(@NonNull String id){
        this.eventId = id;
        return (T) this;
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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDemoId() {
        return demoId;
    }
    public void setDemoId(String invitedId) {
        this.demoId = invitedId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}

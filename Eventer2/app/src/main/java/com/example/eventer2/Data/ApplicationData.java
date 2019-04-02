package com.example.eventer2.Data;

import android.app.Application;
import android.net.Uri;

import com.example.eventer2.models.InvitedFriend;

import java.util.ArrayList;

public class ApplicationData extends Application {
    private Uri eventImageUri;
    private String userImageUri;
    private String eventLocation;
    private String userName;
    private String userPhone;
    private boolean change;
    public ArrayList<InvitedFriend> friendsList = new ArrayList<>();

    public void setImageUri(Uri eventImageUri) {
        eventImageUri = eventImageUri;
    }

    public Uri getImageUri() {
        return eventImageUri;
    }

    public Uri getEventImageUri() {
        return eventImageUri;
    }

    public void setEventImageUri(Uri eventImageUri) {
        this.eventImageUri = eventImageUri;
    }

    public String getUserImageUri() {
        return userImageUri;
    }

    public void setUserImageUri(String userImageUri) {
        this.userImageUri = userImageUri;
    }

    public void setEventLocation(String location) {
        eventLocation =  location;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setChange(boolean change) {
        change = change;
    }

    public boolean getChange() {
        return change;
    }

}

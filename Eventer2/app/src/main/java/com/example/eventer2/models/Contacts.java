package com.example.eventer2.models;

public class Contacts {

    private String name;
    private String image;
    private String number;
    private String userId;

    public Contacts() {
    }

    public Contacts(String name, String image, String number, String userId) {
        this.name = name;
        this.image = image;
        this.number = number;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

package com.example.eventer2.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GuestItem implements Parcelable {
    public String name;
    public String number;
    public String arrival;

    public GuestItem(String name, String number, String arrival) {
        this.name = name;
        this.number = number;
        this.arrival = arrival;
    }

    public GuestItem(String name) {
        this.name = name;
    }

    public GuestItem() {
    }

    protected GuestItem(Parcel in) {
    }

    public static final Creator<GuestItem> CREATOR = new Creator<GuestItem>() {
        @Override
        public GuestItem createFromParcel(Parcel in) {
            return new GuestItem(in);
        }

        @Override
        public GuestItem[] newArray(int size) {
            return new GuestItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
}

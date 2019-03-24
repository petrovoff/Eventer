package com.example.eventer2.models;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class GuestList extends ExpandableGroup<GuestItem> {

    public String size;

    public GuestList(String title, String size, List<GuestItem> items) {
        super(title, items);
        this.size = size;
    }
}

package com.example.eventer2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventer2.R;
import com.example.eventer2.holders.GuestItemViewHolder;
import com.example.eventer2.holders.GuestListViewHolder;
import com.example.eventer2.models.GuestItem;
import com.example.eventer2.models.GuestList;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class GuestAdapter extends ExpandableRecyclerViewAdapter<GuestListViewHolder, GuestItemViewHolder> {


    public GuestAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public GuestListViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expendable_recyclerview_guest_list, parent, false);
        return new GuestListViewHolder(view);
    }

    @Override
    public GuestItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expendable_recyclerview_guest_item, parent, false);
        return new GuestItemViewHolder(view);    }

    @Override
    public void onBindChildViewHolder(GuestItemViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        GuestItem guestItem = (GuestItem) group.getItems().get(childIndex);
        holder.bind(guestItem);
    }

    @Override
    public void onBindGroupViewHolder(GuestListViewHolder holder, int flatPosition, ExpandableGroup group) {
        GuestList guestList = (GuestList) group;
        holder.bind(guestList);
    }
}

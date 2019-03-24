package com.example.eventer2.holders;

import android.view.View;
import android.widget.TextView;

import com.example.eventer2.R;
import com.example.eventer2.models.GuestList;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class GuestListViewHolder extends GroupViewHolder {
    private TextView mName;
    private TextView mSize;
    public GuestListViewHolder(View itemView) {
        super(itemView);

        mName = itemView.findViewById(R.id.guest_list_id);
        mSize = itemView.findViewById(R.id.guest_list_size);
    }

    public void bind(GuestList guestList){
        mName.setText(guestList.getTitle());
        mSize.setText(guestList.size);
    }
}

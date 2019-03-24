package com.example.eventer2.holders;

import android.view.View;
import android.widget.TextView;

import com.example.eventer2.R;
import com.example.eventer2.models.GuestItem;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class GuestItemViewHolder extends ChildViewHolder {

    private TextView mTextView;

    public GuestItemViewHolder(View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.guest_item_id);
    }
    public void bind(GuestItem guestItem){
        mTextView.setText(guestItem.name);
    }
}

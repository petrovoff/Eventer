package com.example.eventer2.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.example.eventer2.listeners.CustomItemListener;
import com.example.eventer2.models.BooVariable;
import com.example.eventer2.models.Guest;
import com.example.eventer2.models.GuestList;
import com.example.eventer2.models.InvitedFriend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class GuestRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Guest> mGuestList;
    public Context mContext;

    private CustomItemListener mListener;
    private static final int EMPTY_VIEW = 10;

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        private EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public GuestRecyclerAdapter(List<Guest> guestList, CustomItemListener listener) {
        this.mGuestList = guestList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        mContext = parent.getContext();

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_guest_empty, parent, false);
            return new EmptyViewHolder(v);
        }
        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item, parent, false);
            return new ViewHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        if (holder instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;

            String guestName = mGuestList.get(position).getName();
            String guestArrival = mGuestList.get(position).getArrival();
            vh.guest_name.setText(guestName);
            vh.guestArrival.setText(guestArrival);
            vh.guestArrival.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mGuestList.size() > 0 ? mGuestList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mGuestList.size() == 0) {

            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;

        TextView guest_name, guestArrival, guestAuthorArrival;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            guest_name = mView.findViewById(R.id.guest_name);
            guestAuthorArrival = mView.findViewById(R.id.guest_admin_arrival);
            guestArrival = mView.findViewById(R.id.guest_arrival);

        }


        @Override
        public void onClick(View v) {

        }
    }

    public void updateList(List<Guest> newList) {
        mGuestList = new ArrayList<>();
        mGuestList.addAll(newList);
        notifyDataSetChanged();
    }
}


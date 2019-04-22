package com.example.eventer2.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.R;
import com.example.eventer2.activities.EventInfoActivity;
import com.example.eventer2.activities.InviteActivity;
import com.example.eventer2.models.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {

    public List<Event> mEventList;
    public Context mContext;

    public FirebaseAuth mAuth;
    public FirebaseFirestore mFirestore;

    public EventRecyclerAdapter(List<Event> eventList){
        this.mEventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        mContext = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        return new EventRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final Date today = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        final String eventId = mEventList.get(position).getEventId();
        final String currentUserId = mAuth.getCurrentUser().getUid();

        String eventName = mEventList.get(position).getName();
        holder.setEventName(eventName);

        String startDate = mEventList.get(position).getStartDate();
        String endDate = mEventList.get(position).getEndDate();
        holder.setEventDate(startDate,endDate);

        String startTime = mEventList.get(position).getStartTime();
        String endTime = mEventList.get(position).getEndTime();
        holder.setEventTime(startTime,endTime);

        //otvaramo infoEvent
        holder.onInfoBtn(eventId);
        holder.onCard(eventId);

//        holder.onDeletePastEventsBtn(eventId);

        holder.delete_btn.setOnClickListener(v -> {
//            String currentUserId = mAuth.getCurrentUser().getUid();
            mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String authorId = task.getResult().getString("authorId");

                        if(authorId != null) {
                            if (!authorId.equals(currentUserId)) {
                                mFirestore.collection("Users/" + currentUserId + "/InvitedEvents").document(eventId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mEventList.remove(position);
                                        notifyDataSetChanged();
                                    }
                                });
                                mFirestore.collection("Users/" + currentUserId + "/CreatedEvents").document(eventId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mEventList.remove(position);
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        });

        //potvrda dolaska
        mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                final String guestName = task.getResult().getString("name");
                final String guestNumber = task.getResult().getString("phone");
                String demoId = task.getResult().getString("demoId");

                holder.onYesBtn(eventId, demoId, guestName, guestNumber, currentUserId);
                holder.onNoBtn(eventId, demoId, guestName, guestNumber, currentUserId);
                holder.onMaybeBtn(eventId, demoId, guestName, guestNumber, currentUserId);

            }
        });

        //prikazujemo korisnicima na koje dogadjaje idu
        mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    String demoId = task.getResult().getString("demoId");

                    if(demoId != null) {
                        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                if (userTask.getResult().exists()) {
                                    String arrival = userTask.getResult().getString("arrival");

                                    if(arrival != null) {
                                        switch (arrival) {
                                            case "Yes":
                                                holder.yes_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                                                holder.no_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                                                holder.maybe_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                                                break;
                                            case "No":
                                                holder.yes_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                                                holder.no_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                                                holder.maybe_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                                                break;
                                            case "Maybe":
                                                holder.yes_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                                                holder.no_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                                                holder.maybe_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        //ako si gost prikazuju ti se dugmici
        mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()) {
                    String authorId = task.getResult().getString("authorId");
                    String endDate1 = task.getResult().getString("endDate");
                    String endTime1 = task.getResult().getString("endTime");

                    String dateTime = endDate1 + " " + endTime1;

                    try {
                        Date eventEndDate = format.parse(dateTime);
                        if (today.after(eventEndDate)) {
                            holder.setDeleteBtnVisible();
                        } else if (authorId.equals(currentUserId) && !today.after(eventEndDate)) {
                            holder.setVisibleBtn();

                        } else if (!authorId.equals(currentUserId) && !today.after(eventEndDate)) {
                            holder.setInvisibleBtn();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    mFirestore.collection("Users").document(authorId).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String authorName = task1.getResult().getString("name");
                            holder.setAuthorName(authorName);
                        }
                    });
                }
            }
        });

        holder.invite_btn.setOnClickListener(v -> {
            Intent inviteIntent = new Intent(mContext, InviteActivity.class);
            inviteIntent.putExtra("eventId", eventId);
            mContext.startActivity(inviteIntent);
        });

    }

    @Override
    public int getItemCount() {
        if(mEventList != null){
            return mEventList.size();
        }else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView authorName;
        private TextView event_name;
        private TextView start_date, end_date, start_time, end_time;
        private ImageView info_btn;
        private CardView event_card;
        private Button yes_btn;
        private Button no_btn;
        private Button maybe_btn;
        private Button invite_btn;
        private Button delete_btn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            event_card = mView.findViewById(R.id.event_card);
            info_btn = mView.findViewById(R.id.event_info_btn);
            yes_btn = mView.findViewById(R.id.event_list_yes);
            no_btn = mView.findViewById(R.id.event_list_no);
            maybe_btn = mView.findViewById(R.id.event_list_maybe);
            invite_btn = mView.findViewById(R.id.event_list_invite_btn);
            delete_btn = mView.findViewById(R.id.event_list_delete_btn);



        }

        public void setEventName(String name){
            event_name = mView.findViewById(R.id.event_list_name);
            event_name.setText(name);
        }

        public void setAuthorName(String name){
            authorName = mView.findViewById(R.id.event_list_author);
            authorName.setText(name);

        }

        public void setEventDate(String startDate, String endDate){
            start_date = mView.findViewById(R.id.event_list_start_date);
            end_date = mView.findViewById(R.id.event_list_end_date);

            start_date.setText(startDate);
            end_date.setText(endDate);
        }

        public void setEventTime(String startTime, String endTime){
            start_time = mView.findViewById(R.id.event_list_start_time);
            end_time = mView.findViewById(R.id.event_list_end_time);

            start_time.setText(startTime);
            end_time.setText(endTime);

        }

        public void onInfoBtn(final String eventId) {
            info_btn.setOnClickListener(v -> {
                Intent infoIntent = new Intent(mContext, EventInfoActivity.class);
                infoIntent.putExtra("eventId", eventId);
                infoIntent.putExtra("change", "0");
                mContext.startActivity(infoIntent);
            });
        }

        public void onCard(final String eventId) {
            event_card.setOnClickListener(v -> {
                Intent infoIntent = new Intent(mContext, EventInfoActivity.class);
                infoIntent.putExtra("eventId", eventId);
                infoIntent.putExtra("change", "0");
                mContext.startActivity(infoIntent);
            });
        }

        public void onYesBtn(final String eventId, final String demoId, final String guestName, final String guestNumber, String userId){
            yes_btn.setOnClickListener(v ->
                    mFirestore.collection("Events/" + eventId + "/Guests").document(demoId)
                    .get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                Map<String, Object> guestMap = new HashMap<>();
                                guestMap.put("name", guestName);
                                guestMap.put("number", guestNumber);
                                guestMap.put("demoId", demoId);
                                guestMap.put("userId", userId);
                                guestMap.put("eventId", eventId);
                                guestMap.put("arrival", "Yes");
                                mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap);
                            }
                        }
                        yes_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                        no_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                        maybe_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    }));
        }

        public void onNoBtn(final String eventId, final String demoId, final String guestName, final String guestNumber, String userId){
            no_btn.setOnClickListener(v -> mFirestore.collection("Events/" + eventId + "/Guests").document(demoId)
                    .get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                Map<String, Object> guestMap = new HashMap<>();
                                guestMap.put("name", guestName);
                                guestMap.put("number", guestNumber);
                                guestMap.put("demoId", demoId);
                                guestMap.put("userId", userId);
                                guestMap.put("eventId", eventId);
                                guestMap.put("arrival", "No");
                                mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap);
                            }
                        }
                        yes_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                        no_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                        maybe_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    }));
        }
        public void onMaybeBtn(final String eventId, final String demoId, final String guestName, final String guestNumber, String userId){
            maybe_btn.setOnClickListener(v ->
                    mFirestore.collection("Events/" + eventId + "/Guests").document(demoId)
                    .get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                Map<String, Object> guestMap = new HashMap<>();

                                guestMap.put("name", guestName);
                                guestMap.put("number", guestNumber);
                                guestMap.put("demoId", demoId);
                                guestMap.put("userId", userId);
                                guestMap.put("eventId", eventId);
                                guestMap.put("arrival", "Maybe");
                                mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap);
                            }
                        }
                        yes_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                        no_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                        maybe_btn.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                    }));
        }

        public void setInvisibleBtn(){
            yes_btn.setVisibility(mView.VISIBLE);
            no_btn.setVisibility(mView.VISIBLE);
            maybe_btn.setVisibility(mView.VISIBLE);
            invite_btn.setVisibility(mView.INVISIBLE);
        }
        public void setVisibleBtn(){
            yes_btn.setVisibility(mView.INVISIBLE);
            no_btn.setVisibility(mView.INVISIBLE);
            maybe_btn.setVisibility(mView.INVISIBLE);
            invite_btn.setVisibility(mView.VISIBLE);
        }

        public void setDeleteBtnVisible(){
            yes_btn.setVisibility(mView.INVISIBLE);
            no_btn.setVisibility(mView.INVISIBLE);
            maybe_btn.setVisibility(mView.INVISIBLE);
            invite_btn.setVisibility(mView.INVISIBLE);
            delete_btn.setVisibility(mView.VISIBLE);
        }
    }

}

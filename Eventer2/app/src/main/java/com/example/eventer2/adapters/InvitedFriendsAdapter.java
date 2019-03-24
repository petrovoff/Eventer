package com.example.eventer2.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.R;
import com.example.eventer2.activities.InviteActivity;
import com.example.eventer2.models.Guest;
import com.example.eventer2.models.InvitedFriend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InvitedFriendsAdapter extends RecyclerView.Adapter<InvitedFriendsAdapter.ViewHolder>{

    public List<InvitedFriend> mInvitedList;
    public Context mContext;

    public FirebaseFirestore mFirestore;
    public FirebaseAuth mAuth;

    public InvitedFriendsAdapter(List<InvitedFriend> invitedList, Context context){
        this.mContext = context;
        this.mInvitedList = invitedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invited_friend_list_item, parent,false);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();



        return new InvitedFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        TextView guest_name, guest_number;

        String currentUserId = mAuth.getCurrentUser().getUid();

        final String eventId = mInvitedList.get(position).getEventId();
        final String invited_demo = mInvitedList.get(position).getDemoId();
        final String invited_id = mInvitedList.get(position).getUserId();
        final String invited_name = mInvitedList.get(position).getName();
        final String invited_number = mInvitedList.get(position).getNumber();

        guest_name = holder.invitedName;
        guest_number = holder.invitedNumber;

        guest_name.setText(mInvitedList.get(position).getName());
        guest_number.setText(mInvitedList.get(position).getNumber());

        holder.mEventImage.setVisibility(View.INVISIBLE);
        holder.invitedUsername.setVisibility(View.INVISIBLE);

        //promena broja iz imenika
        String phone = mInvitedList.get(position).getNumber();
        if(phone.startsWith("0")){
            phone = phone.substring(1);
            phone = "+381" + phone;
        }
        String finalPhone = phone;
        //prikazivanje ikonice
        mFirestore.collection("Contacts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots != null){
                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        InvitedFriend friends = doc.getDocument().toObject(InvitedFriend.class);

                        String number = friends.getNumber();
                        String contactName = friends.getName();

                        if(number != null && contactName != null){
                            if(number.equals(finalPhone)){
                                holder.mEventImage.setVisibility(View.VISIBLE);
                                holder.invitedUsername.setText(contactName);

                                if(InviteActivity.onOff){
                                    holder.invitedUsername.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }
            }
        });
//        //pozivamo ljude na event
        holder.guestInvateBtn.setOnClickListener(v -> {

            Toast.makeText(mContext, "Username:" + invited_name + " userId:" + invited_demo, Toast.LENGTH_SHORT).show();

            mFirestore.collection("Events/" + eventId + "/Guests").document(invited_demo).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if (!task.getResult().exists()) {

                        HashMap<String, Object> invitedMap = new HashMap<>();
                        invitedMap.put("name", invited_name);
                        invitedMap.put("number", invited_number);
                        invitedMap.put("demoId", invited_demo);
                        invitedMap.put("userId", invited_id);
                        invitedMap.put("eventId", eventId);
                        invitedMap.put("arrival", "No answer");

                        mFirestore.collection("Events/" + eventId + "/Guests").document(invited_demo).set(invitedMap);
                        holder.guestInvateBtn.setImageDrawable(mContext.getDrawable(R.drawable.invite_friends_done));
                        Toast.makeText(mContext, invited_name + " was invited!", Toast.LENGTH_LONG).show();
                    }
                }
            });

            mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String location = task.getResult().getString("eventLocation");
                        String downloadUri = task.getResult().getString("image_url");
                        String theme = task.getResult().getString("theme");
                        String startDate = task.getResult().getString("startDate");
                        String endDate = task.getResult().getString("endDate");
                        String startTime = task.getResult().getString("startTime");
                        String endTime = task.getResult().getString("endTime");
                        String author = task.getResult().getString("authorId");

                        HashMap<String, String> eventMap = new HashMap<>();
                        eventMap.put("image_url", downloadUri);
                        eventMap.put("name", name);
                        eventMap.put("theme", theme);
                        eventMap.put("eventLocation", location);
                        eventMap.put("startDate", startDate);
                        eventMap.put("endDate", endDate);
                        eventMap.put("startTime", startTime);
                        eventMap.put("endTime", endTime);
                        eventMap.put("authorId", author);

                        if(invited_id != null){
                            mFirestore.collection("Users/" + invited_id + "/InvitedEvents").document(eventId).set(eventMap);
                        }else {
                            mFirestore.collection("WithoutAcc").document(invited_demo).set(eventMap);
                        }
                    }
                }
            });
        });

//        //prikazuje nam se da li smo nekog pozvali
        mFirestore.collection("Events/" + eventId + "/Guests").document(invited_demo).addSnapshotListener((documentSnapshot, e) -> {
            if(documentSnapshot != null) {
                if (documentSnapshot.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.guestInvateBtn.setImageDrawable(mContext.getDrawable(R.drawable.invite_friends_done));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.guestInvateBtn.setImageDrawable(mContext.getDrawable(R.drawable.guest_not_invited));
                    }
                }
            }
        });

        mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(userTask -> {
           if(userTask.isSuccessful()) {
               if (userTask.getResult().exists()) {
                   String userId = userTask.getResult().getString("userId");
                   String demoId = userTask.getResult().getString("demoId");

                   mFirestore.collection("Events/" + eventId + "/Guests").addSnapshotListener((guestQuery, e) -> {
                       if(guestQuery != null){
                           for(DocumentChange doc: guestQuery.getDocumentChanges()) {
                               if (doc.getType() == DocumentChange.Type.ADDED) {
                                   Guest guest = doc.getDocument().toObject(Guest.class);

                                   String guestDemoId = guest.getDemoId();
                                   String guestArrival = guest.getArrival();

                                   if(demoId.equals(guestDemoId)){
                                       HashMap<String, Object> invitedMap = new HashMap<>();
                                       invitedMap.put("name", invited_name);
                                       invitedMap.put("number", invited_number);
                                       invitedMap.put("demoId", invited_demo);
                                       invitedMap.put("userId", userId);
                                       invitedMap.put("eventId", eventId);
                                       invitedMap.put("arrival", guestArrival);

                                       mFirestore.collection("Events" + eventId + "/Guests").document(demoId).set(invitedMap);
                                   }
                               }
                           }
                       }
                   });

               }
           }
        });

    }

    @Override
    public int getItemCount() {
        if(mInvitedList != null){
            return mInvitedList.size();
        }else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;

        private TextView invitedName;
        private TextView invitedNumber;
        private TextView invitedUsername;
        private ImageView guestInvateBtn;
        private ImageView mEventImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            guestInvateBtn = mView.findViewById(R.id.guest_invate_btn);
            mEventImage = mView.findViewById(R.id.guest_list_event_logo);

            invitedName = mView.findViewById(R.id.guest_list_name);
            invitedNumber = mView.findViewById(R.id.guest_list_number);
            invitedUsername = mView.findViewById(R.id.guest_list_username);
        }
    }

    public void updateList(List<InvitedFriend> newList){
        mInvitedList = new ArrayList<>();
        mInvitedList.addAll(newList);
        notifyDataSetChanged();

    }
}

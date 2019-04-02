package com.example.eventer2.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.Data.ApplicationData;
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

public class InvitedBaseAdapter extends RecyclerView.Adapter<InvitedBaseAdapter.ViewHolder> {


    private ApplicationData mData;
    private List<InvitedFriend> mInvitedList;
    private Context mContext;

    public FirebaseFirestore mFirestore;
    public FirebaseAuth mAuth;

    Boolean clicked = false;

    public InvitedBaseAdapter(List<InvitedFriend> invitedList, Context context){
        this.mContext = context;
        this.mInvitedList = invitedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invited_friend_list_item, parent,false);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mData = (ApplicationData) mContext.getApplicationContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
        holder.guestInviteBtn.setOnClickListener(v -> {
            if(!clicked){
                clicked = true;
                mData.friendsList.add(mInvitedList.get(position));
                holder.guestInviteBtn.setImageDrawable(mContext.getDrawable(R.drawable.invite_friends_done));

            }else {
                clicked = false;
                mData.friendsList.remove(mInvitedList.get(position));
                holder.guestInviteBtn.setImageDrawable(mContext.getDrawable(R.drawable.guest_not_invited));
            }

        });

//        //prikazuje nam se da li smo nekog pozvali
        mFirestore.collection("Events/" + eventId + "/Guests").document(invited_demo).addSnapshotListener((documentSnapshot, e) -> {
            if(documentSnapshot != null) {
                if (documentSnapshot.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.guestInviteBtn.setImageDrawable(mContext.getDrawable(R.drawable.invited_base));
                        holder.guestInviteBtn.setEnabled(false);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.guestInviteBtn.setImageDrawable(mContext.getDrawable(R.drawable.guest_not_invited));
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
        private ImageView guestInviteBtn;
        private ImageView mEventImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            guestInviteBtn = mView.findViewById(R.id.guest_invate_btn);
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

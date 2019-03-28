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

public class GuestRecyclerAdapter extends RecyclerView.Adapter<GuestRecyclerAdapter.ViewHolder> {

    public List<Guest> mGuestList;
    public Context mContext;

    private CustomItemListener mListener;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    final Date today = new Date();
    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    public GuestRecyclerAdapter(List<Guest> guestList, CustomItemListener listener) {
        this.mGuestList = guestList;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item, parent, false);
        mContext = parent.getContext();
        ViewHolder vh = new ViewHolder(view);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        vh.setIsRecyclable(false);

        String currentUserId = mAuth.getCurrentUser().getUid();

        String guestId = mGuestList.get(position).getUserId();
        String guestName = mGuestList.get(position).getName();
        String guestArrival = mGuestList.get(position).getArrival();
        String eventId = mGuestList.get(position).getEventId();
        String demoId = mGuestList.get(position).getDemoId();


        vh.setGuestData(guestName, guestArrival);


        vh.authorYesBtn.setOnClickListener(v -> {
//            mListener.onItemClick(v,vh.getLayoutPosition());
            onYesBtn(eventId,demoId,guestName);
            vh.setGuestData(guestName, "Yes (A)");
            vh.authorCard.setVisibility(View.INVISIBLE);
            vh.guestArrival.setVisibility(View.VISIBLE);
        });

        vh.authorNoBtn.setOnClickListener(v -> {
//            mListener.onItemClick(v,vh.getLayoutPosition());
            onNoBtn(eventId,demoId,guestName);
            vh.setGuestData(guestName, "No (A)");
            vh.authorCard.setVisibility(View.INVISIBLE);
            vh.guestArrival.setVisibility(View.VISIBLE);
        });
        vh.authorMaybeBtn.setOnClickListener(v -> {
//            mListener.onItemClick(v,vh.getLayoutPosition());
            onMaybeBtn(eventId,demoId,guestName);
            vh.setGuestData(guestName, "Maybe (A)");
            vh.authorCard.setVisibility(View.INVISIBLE);
            vh.guestArrival.setVisibility(View.VISIBLE);
        });


            if (guestId == null && guestArrival.equals("No answer")) {
                vh.guestArrival.setVisibility(View.VISIBLE);

            }
            if (guestId == null) {
                mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(eventTask -> {
                    if (eventTask.isSuccessful()) {
                        if (eventTask.getResult().exists()) {
                            String authorId = eventTask.getResult().getString("authorId");
                            String endDate = eventTask.getResult().getString("endDate");

                            Date eventEndDate = null;
                            try {
                                eventEndDate = format.parse(endDate);
                                if (today.before(eventEndDate) && authorId.equals(currentUserId)) {
                                    vh.guestArrival.setOnClickListener(v -> {
                                        vh.authorCard.setVisibility(View.VISIBLE);
                                        vh.guestArrival.setVisibility(View.INVISIBLE);
                                    });
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        }

    }

    @Override
    public int getItemCount() {
        if (mGuestList != null) {
            return mGuestList.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View mView;

        private TextView guest_name, guestArrival;
        private TextView authorYesBtn, authorNoBtn, authorMaybeBtn;
        private CardView authorCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            authorYesBtn = mView.findViewById(R.id.admin_yes_btn);
            authorNoBtn = mView.findViewById(R.id.admin_no_btn);
            authorMaybeBtn = mView.findViewById(R.id.admin_maybe_btn);
            authorCard = mView.findViewById(R.id.admin_cardview);

            authorYesBtn.setOnClickListener(this);
            authorNoBtn.setOnClickListener(this);
            authorMaybeBtn.setOnClickListener(this);
        }

        public void setGuestData(String name, String arrival) {
            guest_name = mView.findViewById(R.id.guest_name);
            guestArrival = mView.findViewById(R.id.guest_arrival);

            guest_name.setText(name);
            guestArrival.setText(arrival);

        }

        @Override
        public void onClick(View v) {

        }
    }

    private void onYesBtn(String eventId, String demoId, String guestName) {

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "Yes (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {

            Toast.makeText(mContext, guestName + " say Yes", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });

    }

    private void onNoBtn(String eventId, String demoId, String guestName) {

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "No (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " say No", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });
    }

    private void onMaybeBtn(String eventId, String demoId, String guestName) {

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "Maybe (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " say Maybe", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });
    }

    public void updateList(List<Guest> newList) {
        mGuestList = new ArrayList<>();
        mGuestList.addAll(newList);
        notifyDataSetChanged();
    }
}


package com.example.eventer2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.R;
import com.example.eventer2.listeners.CustomItemListener;
import com.example.eventer2.models.Guest;
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

public class GuestPhoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public List<Guest> mGuestList;
    public Context mContext;
    private CustomItemListener mListener;
    private static final int EMPTY_VIEW = 10;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    final Date today = new Date();
    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    public GuestPhoneAdapter(List<Guest> guestList, CustomItemListener listener) {
        this.mGuestList = guestList;
        this.mListener = listener;
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder {
        private EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        mContext = parent.getContext();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_guest_empty, parent, false);
            return new GuestPhoneAdapter.EmptyViewHolder(v);
        }
        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item, parent, false);
            return new GuestPhoneAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        if (holder instanceof GuestPhoneAdapter.ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;

            String currentUserId = mAuth.getCurrentUser().getUid();

            String guestId = mGuestList.get(position).getUserId();
            String guestName = mGuestList.get(position).getName();
            String guestArrival = mGuestList.get(position).getArrival();
            String eventId = mGuestList.get(position).getEventId();
            String demoId = mGuestList.get(position).getDemoId();


            vh.setGuestData(guestName, guestArrival);
            vh.guestArrival.setVisibility(View.VISIBLE);

            vh.authorYesBtn.setOnClickListener(v -> {
                onYesBtn(eventId,demoId,guestName);
                vh.setGuestData(guestName, "Yes (A)");
                vh.authorCard.setVisibility(View.INVISIBLE);
                vh.authorArrivalCard.setVisibility(View.VISIBLE);
            });

            vh.authorNoBtn.setOnClickListener(v -> {
                onNoBtn(eventId,demoId,guestName);
                vh.setGuestData(guestName, "No (A)");
                vh.authorCard.setVisibility(View.INVISIBLE);
                vh.authorArrivalCard.setVisibility(View.VISIBLE);
            });
            vh.authorMaybeBtn.setOnClickListener(v -> {
                onMaybeBtn(eventId,demoId,guestName);
                vh.setGuestData(guestName, "Maybe (A)");
                vh.authorCard.setVisibility(View.INVISIBLE);
                vh.authorArrivalCard.setVisibility(View.VISIBLE);
            });

            if (guestId == null) {
                vh.authorArrivalCard.setVisibility(View.VISIBLE);

            }else {
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
                                    vh.authorArrivalCard.setOnClickListener(v -> {
                                        vh.authorCard.setVisibility(View.VISIBLE);
                                        vh.authorArrivalCard.setVisibility(View.INVISIBLE);
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

        private TextView guest_name, guestArrival, guestAuthorArrival;
        private TextView authorYesBtn, authorNoBtn, authorMaybeBtn;
        private CardView authorCard, authorArrivalCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            authorYesBtn = mView.findViewById(R.id.admin_yes_btn);
            authorNoBtn = mView.findViewById(R.id.admin_no_btn);
            authorMaybeBtn = mView.findViewById(R.id.admin_maybe_btn);
            authorCard = mView.findViewById(R.id.admin_cardview);
            authorArrivalCard = mView.findViewById(R.id.guest_admin_card);

            authorYesBtn.setOnClickListener(this);
            authorNoBtn.setOnClickListener(this);
            authorMaybeBtn.setOnClickListener(this);
            authorArrivalCard.setOnClickListener(this);

        }

        public void setGuestData(String name, String arrival) {
            guest_name = mView.findViewById(R.id.guest_name);
            guestAuthorArrival = mView.findViewById(R.id.guest_admin_arrival);
            guestArrival = mView.findViewById(R.id.guest_arrival);

            guest_name.setText(name);

            guestAuthorArrival.setText(arrival);
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

            Toast.makeText(mContext, guestName + " says Yes", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });

    }

    private void onNoBtn(String eventId, String demoId, String guestName) {

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "No (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " says No", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });
    }

    private void onMaybeBtn(String eventId, String demoId, String guestName) {

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "Maybe (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " says Maybe", Toast.LENGTH_SHORT).show();
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

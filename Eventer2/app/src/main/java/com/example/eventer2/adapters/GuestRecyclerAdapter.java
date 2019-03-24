package com.example.eventer2.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.R;
import com.example.eventer2.models.Guest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    final Date today = new Date();
    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    public GuestRecyclerAdapter(List<Guest> guestList){
        this.mGuestList = guestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_list_item, parent, false);
        mContext = parent.getContext();

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new GuestRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        String currentUserId = mAuth.getCurrentUser().getUid();

        String guestId = mGuestList.get(position).getUserId();

        String guestName = mGuestList.get(position).getName();
        String guestArrival = mGuestList.get(position).getArrival();
        String eventId = mGuestList.get(position).getEventId();
        String demoId = mGuestList.get(position).getDemoId();
        holder.setGuestData(guestName, guestArrival);

        if (guestId == null && guestArrival.equals("No answer")) {
            holder.guestArrival.setVisibility(View.VISIBLE);

        }
        if(guestId == null) {
            mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(eventTask -> {
                if (eventTask.isSuccessful()) {
                    if (eventTask.getResult().exists()) {
                        String authorId = eventTask.getResult().getString("authorId");
                        String endDate = eventTask.getResult().getString("endDate");

                        Date eventEndDate = null;
                        try {
                            eventEndDate = format.parse(endDate);
                            if (today.before(eventEndDate) && authorId.equals(currentUserId)) {
                                holder.guestArrival.setOnClickListener(v -> {
                                    holder.authorCard.setVisibility(View.VISIBLE);
                                    holder.guestArrival.setVisibility(View.INVISIBLE);
                                });
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        holder.authorYesBtn.setOnClickListener(v ->{
            onYesBtn(eventId, demoId, guestName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.authorCard.setVisibility(View.INVISIBLE);
                holder.guestArrival.setVisibility(View.VISIBLE);
                notifyItemChanged(position);
            }

        });

        holder.authorNoBtn.setOnClickListener(v ->{
            onNoBtn(eventId, demoId, guestName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.authorCard.setVisibility(View.INVISIBLE);
                holder.guestArrival.setVisibility(View.VISIBLE);

            }

        });

        holder.authorMaybeBtn.setOnClickListener(v ->{
            onMaybeBtn(eventId, demoId, guestName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.authorCard.setVisibility(View.INVISIBLE);
                holder.guestArrival.setVisibility(View.VISIBLE);

            }
        });


    }

    @Override
    public int getItemCount() {
        if(mGuestList != null){
            return mGuestList.size();
        }else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View mView;

        private TextView guestName, guestArrival;
        private TextView authorYesBtn, authorNoBtn, authorMaybeBtn;
        private CardView authorCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }
        public void setGuestData(String name, String arrival){
            guestName = mView.findViewById(R.id.guest_name);
            guestArrival = mView.findViewById(R.id.guest_arrival);

            authorYesBtn = mView.findViewById(R.id.admin_yes_btn);
            authorNoBtn = mView.findViewById(R.id.admin_no_btn);
            authorMaybeBtn = mView.findViewById(R.id.admin_maybe_btn);
            authorCard = mView.findViewById(R.id.admin_cardview);

            guestName.setText(name);
            guestArrival.setText(arrival);
        }
    }

    private void onYesBtn(String eventId, String demoId, String guestName){

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "Yes (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " say Yes", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });

    }

    private void onNoBtn(String eventId, String demoId, String guestName){

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "No (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " say No", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });

    }

    private void onMaybeBtn(String eventId, String demoId, String guestName){

        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("arrival", "Maybe (A)");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).update(guestMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(mContext, guestName + " say Maybe", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(mContext, "Something was wrong!", Toast.LENGTH_SHORT).show();
        });

    }
}


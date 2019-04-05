package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.GoogleMapAndPlaces.InfoMapActivity;
import com.example.eventer2.GoogleMapAndPlaces.MapActivity;
import com.example.eventer2.R;
import com.example.eventer2.adapters.GuestPhoneAdapter;
import com.example.eventer2.adapters.GuestRecyclerAdapter;
import com.example.eventer2.listeners.CustomItemListener;
import com.example.eventer2.models.BooVariable;
import com.example.eventer2.models.Guest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.model.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventInfoActivity extends AppCompatActivity {

    private static final String TAG = "EventInfoActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    final Date today = new Date();

    private ApplicationData mData;

    private TextView mName;
    private TextView mTheme;
    private TextView mLocation;
    private TextView mStartDate, mEndDate;
    private TextView mStartTime, mEndTime;
    private ImageView mImageView;
    private ImageView mInviteBtn;
    private ImageView mExportBtn;
    private Button mEventerBtn, mAdminBtn;
    private ProgressBar mProgressBar;

    private String mEventId;
    private String currentUserId;
    private String mAuthorId;
    private String location;
    private int mState;

    private ArrayList<Guest> guest_list;
    private ArrayList<Guest> guest_phone_list;
    private RecyclerView guest_recycler_view;
    private RecyclerView guest_author_recycler;
    private GuestRecyclerAdapter guest_adapter;
    private GuestPhoneAdapter guest_phone_adapter;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        init();

        mState = 1;
        onLoadGuests();

        guest_list = new ArrayList<>();
        guest_phone_list = new ArrayList<>();

        guest_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        guest_recycler_view.addItemDecoration(new DividerItemDecoration(guest_recycler_view.getContext(), DividerItemDecoration.VERTICAL));

        guest_author_recycler.setLayoutManager(new LinearLayoutManager(this));
        guest_author_recycler.addItemDecoration(new DividerItemDecoration(guest_recycler_view.getContext(), DividerItemDecoration.VERTICAL));

        mEventId = getIntent().getStringExtra("eventId");
        currentUserId = mAuth.getCurrentUser().getUid();

        mFirestore.collection("Events").document(mEventId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    mProgressBar.setVisibility(View.VISIBLE);

                    final String name = task.getResult().getString("name");
                    String theme = task.getResult().getString("theme");
                    location = task.getResult().getString("eventLocation");
                    final String startDate = task.getResult().getString("startDate");
                    final String startTime = task.getResult().getString("startTime");
                    final String endDate = task.getResult().getString("endDate");
                    final String endTime = task.getResult().getString("endTime");
                    String image = task.getResult().getString("image_url");
                    mAuthorId = task.getResult().getString("authorId");

                    mName.setText(name);
                    mTheme.setText(theme);
                    mLocation.setText(location);
                    mStartDate.setText(startDate);
                    mEndDate.setText(endDate);
                    mStartTime.setText(startTime);
                    mEndTime.setText(endTime);

                    if(image != null) {
                        Glide.with(EventInfoActivity.this).load(image).into(mImageView);
                    }else {
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.backgroundholder);
                        Glide.with(EventInfoActivity.this).setDefaultRequestOptions(placeholderRequest)
                                .load(image).into(mImageView);
                    }

                    mProgressBar.setVisibility(View.INVISIBLE);

                    if(mAuthorId.equals(currentUserId)) {
                        try {
                            Date eventEndDate = format.parse(endDate);
                            if (today.after(eventEndDate)) {
                                mInviteBtn.setVisibility(View.INVISIBLE);
                                mExportBtn.setVisibility(View.INVISIBLE);
                            } else if (mAuthorId.equals(currentUserId) && !today.after(eventEndDate)) {
                                mInviteBtn.setVisibility(View.VISIBLE);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

//                        mExportBtn.setOnClickListener(v -> {
//                            Intent intent = new Intent(Intent.ACTION_EDIT);
//                            intent.setType("vnd.android.cursor.item/event");
////                                intent.putExtra(CalendarContract.Events.DTSTART, "21/5/2019 15:26:33");
//                            intent.putExtra(CalendarContract.Events.DTEND, endDate);
//                            intent.putExtra(CalendarContract.Events.TITLE, name);
//                            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
//                            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, "26.06.2019 15:26:33");
//                            startActivity(intent);
//                        });
                }
            }else {
                Toast.makeText(this, "Something was wrong: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });

        //prikazivanje gostiju
        mEventerBtn.setOnClickListener(v -> {
            mState = 1;
            inBackground();

            guest_recycler_view.setVisibility(View.VISIBLE);
            guest_author_recycler.setVisibility(View.INVISIBLE);

            mEventerBtn.setEnabled(false);
            mAdminBtn.setEnabled(true);

            mEventerBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mEventerBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
            mEventerBtn.setTextSize(16);
            mAdminBtn.setTextSize(12);
            mAdminBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mAdminBtn.setTextColor(getResources().getColor(R.color.colorAccent));

        });

        mAdminBtn.setOnClickListener(v -> {
            mState = 2;
            inBackground();

            guest_recycler_view.setVisibility(View.INVISIBLE);
            guest_author_recycler.setVisibility(View.VISIBLE);

            mEventerBtn.setEnabled(true);
            mAdminBtn.setEnabled(false);

            mAdminBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mAdminBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
            mAdminBtn.setTextSize(16);
            mEventerBtn.setTextSize(12);
            mEventerBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mEventerBtn.setTextColor(getResources().getColor(R.color.colorAccent));
        });


        guest_adapter = new GuestRecyclerAdapter(guest_list, (v, position) -> {
            guest_list.clear();
            mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null){
                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            Guest guests = doc.getDocument().toObject(Guest.class);
                            String guestId = guests.getUserId();
                            if(guestId != null){
                                guest_list.add(guests);
                                guest_adapter.updateList(guest_list);
                            }
                        }
                    }

                }
            });
        });
        guest_recycler_view.setAdapter(guest_adapter);
        guest_adapter.notifyDataSetChanged();


        guest_phone_adapter = new GuestPhoneAdapter(guest_phone_list, (v, position) -> {
            guest_phone_list.clear();
            mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null){
                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            Guest guests = doc.getDocument().toObject(Guest.class);
                            String guestId = guests.getUserId();
                            if(guestId == null){
                                guest_phone_list.add(guests);
                                guest_phone_adapter.updateList(guest_phone_list);
                            }
                        }
                    }

                }
            });
        });
        guest_author_recycler.setAdapter(guest_phone_adapter);
        guest_phone_adapter.notifyDataSetChanged();


        mInviteBtn.setOnClickListener(v -> {
            Intent inviteIntent = new Intent(EventInfoActivity.this, InviteActivity.class);
            inviteIntent.putExtra("eventId", mEventId);
            startActivity(inviteIntent);
        });

        mLocation.setOnClickListener(v -> {
            onLocation(location);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        inBackground();
    }

    private void init(){
        mData = (ApplicationData) getApplication();
        mName = findViewById(R.id.event_info_name);
        mTheme = findViewById(R.id.event_info_theme);
        mLocation = findViewById(R.id.info_location);
        mStartDate = findViewById(R.id.info_start_date);
        mEndDate = findViewById(R.id.info_end_date);
        mStartTime = findViewById(R.id.info_start_time);
        mEndTime = findViewById(R.id.info_end_time);
        mInviteBtn = findViewById(R.id.info_invate_friends);
        mImageView = findViewById(R.id.event_info_bg);
        mExportBtn = findViewById(R.id.export_event_btn);
        mProgressBar = findViewById(R.id.event_info_progress);

        mEventerBtn = findViewById(R.id.event_info_base_btn);
        mAdminBtn = findViewById(R.id.event_info_admin_btn);

        guest_recycler_view = findViewById(R.id.guest_list_view);
        guest_author_recycler = findViewById(R.id.guest_list_author_view);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

    }

    private void onLocation(String location){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Intent infoMap = new Intent(this, InfoMapActivity.class);
                infoMap.putExtra("location", location);
                startActivity(infoMap);
            }else {
                Toast.makeText(this, "Enable your GPS and try again!", Toast.LENGTH_LONG).show();
            }

    }


    private List<Guest> onLoadGuests(){
        guest_recycler_view.setVisibility(View.VISIBLE);
        guest_author_recycler.setVisibility(View.INVISIBLE);
        inBackground();

        return guest_list;
    }


    //google maps service
    public boolean isServiceOK(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(EventInfoActivity.this);

        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(EventInfoActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private ArrayList<Guest> inBackground(){

        ArrayList<Guest> demoList = new ArrayList<>();

        if(mState == 1) {
            mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null){
                    guest_list.clear();
                    for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                        Guest guest=doc.toObject(Guest.class);
                        String guestId = null;
                        if (guest != null) {
                            guestId = guest.getUserId();
                            if(guestId != null){
                                guest_list.add(guest);
                                guest_adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
            demoList = guest_list;
        } else if(mState == 2) {
            mFirestore.collection("Events/" + mEventId + "/Guests").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (queryDocumentSnapshots != null) {
                    guest_phone_list.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Guest guest = doc.toObject(Guest.class);
                        String guestId = null;
                        if (guest != null) {
                            guestId = guest.getUserId();
                            if (guestId == null) {
                                guest_phone_list.add(guest);
                                guest_phone_adapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            });
            demoList = guest_phone_list;
        }
        return demoList;

    }
}

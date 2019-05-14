package com.example.eventer2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
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
import com.example.eventer2.models.Event;
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



    private ApplicationData mData;
    private Event mEvent;

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

    //calendar
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private final Date today = new Date();
    private long calId;
    private String email;

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

        email = mData.getUserEmail();
        Log.i("Data", email);

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
                    mStartDate.setText(dateConverter(startDate));
                    mEndDate.setText(dateConverter(endDate));
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

                        String dateTime = endDate + " " + endTime;
                        try {
                            Date eventEndDate = format.parse(dateTime);
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

                    mExportBtn.setOnClickListener(v -> {
                        mEvent = new Event(mAuthorId, name, theme, startDate, endDate, startTime, endTime, location);
                        calendarPermission();
                    });
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
        Intent infoMap = new Intent(this, InfoMapActivity.class);
        infoMap.putExtra("location", location);
        startActivity(infoMap);
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

    //date convertor
    public String dateConverter(String date){
        String year = date.substring(0,4);
        String month = date.substring(5,7);
        String day = date.substring(8);

        switch (month){
            case "01":
                month = "Jan";
                break;
            case "02":
                month = "Feb";
                break;
            case "03":
                month = "Mart";
                break;
            case "04":
                month = "Apr";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "June";
                break;
            case "07":
                month = "July";
                break;
            case "08":
                month = "August";
                break;
            case "09":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;

        }
        Log.i("EVENT", "Date" + month);
        return day + ". " + month + " " + year + ".";
    }

    //calendar service
    private AlertDialog calendarDialog(){
        PackageManager pmv = getPackageManager();

        List<ApplicationInfo> packages = pmv.getInstalledApplications(PackageManager.GET_META_DATA);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Export event to calendar.")
                .setTitle("Calendar");

        //phone calendar
        builder.setPositiveButton("Calendar", (dialog, id) -> {
            exportEventToCalendar();
            Toast.makeText(this, "Exporting...", Toast.LENGTH_SHORT).show();
            calendarIntent("com.android.calendar");
        });

        //google calendar
        builder.setNegativeButton("Google Calendar", (dialog, id) -> {

            if(googleCalendarExist()){

                if(userEmailExist()){
                    exportEventToGoogleCalendar();
                    Toast.makeText(this, "Exporting...", Toast.LENGTH_SHORT).show();
                    calendarIntent("com.google.android.calendar");
                }else {
                    Toast.makeText(this, "You must have email!", Toast.LENGTH_SHORT).show();
                }
            }else {
                playStoreDialog().show();
            }
        });

        return builder.create();
    }
    private AlertDialog playStoreDialog(){
        //play store dialog
        AlertDialog.Builder appStoreDialog = new AlertDialog.Builder(this);
        appStoreDialog.setMessage("You dont have Google Calendar, do you want to install it? You can export event in phone calendar.")
                .setTitle("Google Calendar");
        //open play store
        appStoreDialog.setPositiveButton("Go to Play Store", (playDialog, playId) -> {
            sendToPlayStore();
        });
        //back
        appStoreDialog.setNegativeButton("Cancel", (playDialog, playId) -> {

        });

        return appStoreDialog.create();
    }
    private boolean googleCalendarExist(){
        PackageManager pmv = getPackageManager();
        List<ApplicationInfo> packages = pmv.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> apps = new ArrayList<>();
        for (ApplicationInfo packageInfo : packages) {
            String appPackageName = packageInfo.packageName;
            if(appPackageName.equals("com.google.android.calendar")) {
                apps.add(appPackageName);
            }
        }

        if(apps.contains("com.google.android.calendar")){
            return true;
        }else {
            return false;
        }
    }
    private boolean userEmailExist(){

        if(email.isEmpty()){
            return false;
        }else {
            return true;
        }
    }
    private void exportEventToGoogleCalendar(){

        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE};
        Cursor calCursor =
                getContentResolver().
                        query(CalendarContract.Calendars.CONTENT_URI,
                                projection,
                                CalendarContract.Calendars.VISIBLE + " = 1",
                                null,
                                CalendarContract.Calendars._ID + " ASC");
        if (calCursor.moveToFirst()) {
            do {
                long id = calCursor.getLong(0);
                String displayName = calCursor.getString(1);
                if(displayName != null) {
                    if (displayName.equals(email)) {
                        calId = id;
                        Log.i("EventInfoActivity", "Name:" + displayName + "Id" + calId);

                        ContentResolver cr = getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(CalendarContract.Events.DTSTART, mEvent.getCalendarDateStart().getTimeInMillis());
                        values.put(CalendarContract.Events.DTEND, mEvent.getCalendarDateEnd().getTimeInMillis());
                        values.put(CalendarContract.Events.TITLE, mEvent.getName());
                        values.put(CalendarContract.Events.DESCRIPTION, "");
                        values.put(CalendarContract.Events.CALENDAR_ID, calId);
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Serbia");
                        values.put(CalendarContract.Events.EVENT_LOCATION, mEvent.getEventLocation());

                        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                        long eventID = Long.parseLong(uri.getLastPathSegment());
                    }
                }
            } while (calCursor.moveToNext());
        }
    }
    private void exportEventToCalendar(){

        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE};
        Cursor calCursor =
                getContentResolver().
                        query(CalendarContract.Calendars.CONTENT_URI,
                                projection,
                                CalendarContract.Calendars.VISIBLE + " = 1",
                                null,
                                CalendarContract.Calendars._ID + " ASC");
        if (calCursor.moveToFirst()) {
            do {
                calId = 1;

                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, mEvent.getCalendarDateStart().getTimeInMillis());
                values.put(CalendarContract.Events.DTEND, mEvent.getCalendarDateEnd().getTimeInMillis());
                values.put(CalendarContract.Events.TITLE, mEvent.getName());
                values.put(CalendarContract.Events.DESCRIPTION, "");
                values.put(CalendarContract.Events.CALENDAR_ID, calId);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Serbia");
                values.put(CalendarContract.Events.EVENT_LOCATION, mEvent.getEventLocation());
                if (ActivityCompat.checkSelfPermission(EventInfoActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                long eventID = Long.parseLong(uri.getLastPathSegment());

            } while (calCursor.moveToNext());
        }
    }
    private void calendarIntent(String packageInfo){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageInfo);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }
    private void sendToPlayStore(){
        final String appPackageName = "com.google.android.calendar"; // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

    }
    private void calendarPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Calendar permission", Toast.LENGTH_LONG).show();
                //pitamo da nam korisnik odobri dozvolu za koriscenje
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR}, 4);
            }else {
                calendarDialog().show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 4) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.WRITE_CALENDAR)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {

                    }
                }
                if (permission.equals(Manifest.permission.READ_CALENDAR)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {

                    }
                }
            }
        }
    }
}

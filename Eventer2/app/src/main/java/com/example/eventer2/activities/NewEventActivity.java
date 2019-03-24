package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.GoogleMapAndPlaces.MapActivity;
import com.example.eventer2.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = "NewEventActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private ApplicationData mApplicationData;

    private ImageView mEventImage;
    private EditText mEventName;
    private EditText mEventTheme;
    private EditText mEventLocation;
    private TextView mStartDisplayDate;
    private TextView mEndDisplayDate;
    private TextView mStartDisplayTime;
    private TextView mEndDisplayTime;
    private Button mCreateEventButton;
    private ProgressBar mEventProgress;
    private ImageView mMap;

    private DatePickerDialog.OnDateSetListener mDateStartListener;
    private DatePickerDialog.OnDateSetListener mDateEndListener;
    private TimePickerDialog.OnTimeSetListener mTimeStartListener;
    private TimePickerDialog.OnTimeSetListener mTimeEndListener;

    private Uri mEventImageUri = null;

    private String mCurrentUserId;
    private String getLocation = "0";
    private String mName, mLocation, mStartDate, mStartTime, mEndDate, mEndTime, mTheme, mEventPicture;
    private int mYear, mMonth, mDay, mHour, mMin;

    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        init();

        //sredjuje bag prilikom postavljanja lokacije
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        getLocation = getIntent().getStringExtra("locationInfo");
        if(getLocation.equals("1")) {
            mName = getIntent().getStringExtra("name");
            mTheme = getIntent().getStringExtra("theme");
            mStartDate = getIntent().getStringExtra("startDate");
            mEndDate = getIntent().getStringExtra("endDate");
            mStartTime = getIntent().getStringExtra("startTime");
            mEndTime = getIntent().getStringExtra("endTime");
            mLocation = getIntent().getStringExtra("location");
            mEventPicture = getIntent().getStringExtra("picture");

            mEventName.setText(mName);
            mEventTheme.setText(mTheme);
            mStartDisplayDate.setText(mStartDate);
            mEndDisplayDate.setText(mEndDate);
            mStartDisplayTime.setText(mStartTime);
            mEndDisplayTime.setText(mEndTime);
            mEventLocation.setText(mLocation);
//            mEventLocation.setEnabled(false);

            if(mEventPicture != null){
                Glide.with(this).load(mEventPicture).into(mEventImage);
            }else {
                mEventImage.setImageResource(R.mipmap.post_placeholder);
            }
        }

        if(isServicesOK()){
            initMap();
        }

        // set time
        mStartDisplayTime.setOnClickListener(v -> setTime(mTimeStartListener));
        mEndDisplayTime.setOnClickListener(v -> setTime(mTimeEndListener));

        //set date
        mStartDisplayDate.setOnClickListener(v -> setDate(mDateStartListener));
        mEndDisplayDate.setOnClickListener(v -> setDate(mDateEndListener));

        mDateStartListener = (view, year, month, dayOfMonth) -> {
            mYear = year;
            mMonth = month;
            mDay = dayOfMonth;

            month = month + 1;
            String date = dayOfMonth + "/" + month + "/" + year;
            mStartDisplayDate.setText(date);
        };

        mDateEndListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String date = dayOfMonth + "/" + month + "/" + year;
            mEndDisplayDate.setText(date);
        };

        mTimeStartListener = (view, hour, minute) ->{
            mHour = hour;
            mMin = minute;

            String time = hour + ":" + minute;
            mStartDisplayTime.setText(time);
        };

        mTimeEndListener = (view, hour, minute) ->{
            String time = hour + ":" + minute;
            mEndDisplayTime.setText(time);
        };
        //set event image
        mEventImage.setOnClickListener(v -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(2,1)
                .start(NewEventActivity.this));

        if(mAuth.getCurrentUser() != null) {

            mCreateEventButton.setOnClickListener(v -> {

                mName = mEventName.getText().toString();
                mTheme = mEventTheme.getText().toString();
                mStartDate = mStartDisplayDate.getText().toString();
                mStartTime = mStartDisplayTime.getText().toString();
                mEndDate = mEndDisplayDate.getText().toString();
                mEndTime = mEndDisplayTime.getText().toString();
                mLocation = mEventLocation.getText().toString();

                mEventProgress.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(mName)
                        && !TextUtils.isEmpty(mTheme) && !TextUtils.isEmpty(mLocation) && !mStartTime.equals("Start Date")
                        && !mEndDate.equals("End Date") && !mStartTime.equals("Start Time")
                        && !mEndTime.equals("End Time") && mEventImageUri != null) {

                    final StorageReference filePath = mStorageReference.child("event_images").child(mName + ".jpg");
                    filePath.putFile(mEventImageUri).addOnSuccessListener(taskSnapshot ->
                            filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                                final String downloadUri = uri.toString();

                                mEventPicture = downloadUri;

                                final String eventId = UUID.randomUUID().toString();

                                Map<String, Object> eventMap = new HashMap<>();
                                eventMap.put("image_url", downloadUri);
                                eventMap.put("name", mName);
                                eventMap.put("theme", mTheme);
                                eventMap.put("eventLocation", mLocation);
                                eventMap.put("startDate", mStartDate);
                                eventMap.put("endDate", mEndDate);
                                eventMap.put("startTime", mStartTime);
                                eventMap.put("endTime", mEndTime);
                                eventMap.put("authorId", mCurrentUserId);

                                mApplicationData.setImageUri(null);

                                mFirebaseFirestore.collection("Events").document(eventId).set(eventMap).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(NewEventActivity.this, "Event was created", Toast.LENGTH_LONG).show();
                                        onInviteFriendIntent(eventId);
                                    } else {
                                        Toast.makeText(NewEventActivity.this, "Some error", Toast.LENGTH_LONG).show();
                                    }
                                    mEventProgress.setVisibility(View.INVISIBLE);
                                });


                                    mFirebaseFirestore.collection("Users/" + mCurrentUserId + "/CreatedEvents").document(eventId).set(eventMap).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(NewEventActivity.this, "Event was created", Toast.LENGTH_LONG).show();
                                        onInviteFriendIntent(eventId);
                                    } else {
                                        Toast.makeText(NewEventActivity.this, "Some error", Toast.LENGTH_LONG).show();
                                    }
                                    mEventProgress.setVisibility(View.INVISIBLE);
                                });
                            })).addOnFailureListener(e -> mEventProgress.setVisibility(View.INVISIBLE));
                }else {
                    Toast.makeText(this, "You must fill all fields!", Toast.LENGTH_SHORT).show();
                    mEventProgress.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mApplicationData.getImageUri() != null) {
            mEventImage.setImageURI(mApplicationData.getImageUri());
        }

        if(mApplicationData.getEventLocation() != null){
            mLocation = mApplicationData.getEventLocation();
            mEventLocation.setText(mLocation);
        }
    }

    private void init(){

        mApplicationData = (ApplicationData) getApplication();
        mEventImage = findViewById(R.id.event_info_bg);
        mEventName = findViewById(R.id.event_info_name);
        mEventTheme = findViewById(R.id.event_info_theme);
        mEventLocation = findViewById(R.id.info_location);
        mStartDisplayDate = findViewById(R.id.info_start_date);
        mEndDisplayDate = findViewById(R.id.info_end_date);
        mStartDisplayTime = findViewById(R.id.start_time_btn);
        mEndDisplayTime = findViewById(R.id.end_time_btn);
        mEventProgress = findViewById(R.id.event_progress);
        mCreateEventButton = findViewById(R.id.event_create_button);
        mMap = findViewById(R.id.event_map);

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Calendar currentday = Calendar.getInstance();
        mYear = currentday.get(Calendar.YEAR);
        mMonth = currentday.get(Calendar.MONTH);
        mDay = currentday.get(Calendar.DAY_OF_MONTH);

        Calendar currentTime = Calendar.getInstance();
        mHour = currentTime.get(Calendar.HOUR_OF_DAY);
        mMin = currentTime.get(Calendar.MINUTE);
    }

    private void initMap(){
        mMap.setOnClickListener(v -> {

            Intent mapIntent = new Intent(this, MapActivity.class);
            startActivity(mapIntent);
        });
    }

    public void setDate(DatePickerDialog.OnDateSetListener dateListener){
         DatePickerDialog dialog = new DatePickerDialog(
                NewEventActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                dateListener,
                mYear,mMonth,mDay);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setTitle("Select Start Date");
        dialog.show();

    }

    private void setTime(TimePickerDialog.OnTimeSetListener timeListener){

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(NewEventActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                timeListener,
                mHour, mMin, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

    }

    private void onInviteFriendIntent(String eventId){
        Intent inviteFriendIntent = new Intent(this, InviteActivity.class);
        inviteFriendIntent.putExtra("eventId", eventId);
        startActivity(inviteFriendIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                mEventImageUri = result.getUri();
                mApplicationData.setImageUri(mEventImageUri);
                mEventImage.setImageURI(mEventImageUri);
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        }
    }

    //google maps
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}

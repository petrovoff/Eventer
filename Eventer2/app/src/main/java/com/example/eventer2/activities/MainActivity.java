package com.example.eventer2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.GoogleMapAndPlaces.MapActivity;
import com.example.eventer2.R;
import com.example.eventer2.fragments.AccountFragment;
import com.example.eventer2.fragments.EventFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {



    private Toolbar mainToolbar;
    private FloatingActionButton mNewEventButton;

    private TextView eventNavBtn, profileNavBtn;
    private CardView eventNavCard, profileNavCard;

    private AccountFragment mAccountFragment;
    private EventFragment mEventFragment;

    private ApplicationData mData;

    private String userImageUrl;
    private String userName;
    private String userPhone;
    private String userEmail;
    private int mAppState;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            sendToLogin();
        }else {
            SharedPreferences prefs = getSharedPreferences("AppState", 0);
            int state = prefs.getInt("state", 0);
            Log.i("APPSTATE", "State: " + state);

            if(state == 0){
                sendToProfile();
            }
        }

        if (currentUser != null){
            //fragments set

            onStartFragment(mEventFragment);

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mNewEventButton.setOnClickListener(v -> {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Intent newEventIntent = new Intent(MainActivity.this, NewEventActivity.class);
                    newEventIntent.putExtra("locationInfo", "0");
                    startActivity(newEventIntent);
                }else {
                    Toast.makeText(this, "Enable your GPS and try again!", Toast.LENGTH_LONG).show();
                }

            });

            eventNavCard.setOnClickListener(v -> {
                replaceFragment(mEventFragment, mAccountFragment);
                eventNavBtn.setTextSize(20);
                profileNavBtn.setTextSize(18);
            });
            profileNavCard.setOnClickListener(v -> {
                replaceFragment(mAccountFragment, mEventFragment);

                profileNavBtn.setTextSize(20);
                eventNavBtn.setTextSize(18);
            });

            String currentUserId = mAuth.getCurrentUser().getUid();

            userImageUrl = mData.getUserImageUri();
            userName = mData.getUserName();
            userPhone = mData.getUserPhone();
            userEmail = mData.getUserEmail();

            if(userName != null || userPhone != null || userEmail != null){

            }else {
                mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String imageUrl = task.getResult().getString("image");
                            String name = task.getResult().getString("name");
                            String phone = task.getResult().getString("phone");
                            String email = task.getResult().getString("email");

                            if(imageUrl != null && name != null && phone != null) {
                                mData.setUserName(name);
                                mData.setUserImageUri(imageUrl);
                                mData.setUserPhone(phone);
                                mData.setUserEmail(email);
                            }

                        }
                    }
                });
            }

            if(userEmail == null){
                mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String email = task.getResult().getString("email");

                            if(email != null) {
                                mData.setUserEmail(email);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout_btn:
                logOut();
                return true;

            case R.id.action_settings_btn:
                sendToProfile();
                return true;

            default: return false;
        }
    }

    private void init(){

        mData = (ApplicationData) getApplication();
        eventNavBtn = findViewById(R.id.event_nav_btn);
        profileNavBtn = findViewById(R.id.profile_nav_btn);

        eventNavCard = findViewById(R.id.event_card);
        profileNavCard = findViewById(R.id.profile_card);

        mNewEventButton = findViewById(R.id.create_event_btn);
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Eventer");

        //fragments
        mAccountFragment = new AccountFragment();
        mEventFragment = new EventFragment();

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }


    private void logOut(){
        String currentUserId = mAuth.getCurrentUser().getUid();

        Map<String, Object> tokenMapDelete = new HashMap<>();
        tokenMapDelete.put("tokenId", "");
        mFirestore.collection("Users").document(currentUserId).update(tokenMapDelete).addOnSuccessListener(aVoid -> {
            mFirestore.collection("Contacts").document(currentUserId).update(tokenMapDelete);

            mData.setUserName(null);
//            mData.setUserEmail(null);
            mAuth.signOut();


            sendToLogin();
        });
    }

    private void sendToLogin(){
        Intent loginIntent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void sendToProfile(){
        Intent settingsIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
        settingsIntent.putExtra("info", "0");
        startActivity(settingsIntent);
        finish();
    }

    private void replaceFragment(Fragment fragment, Fragment removeFragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(removeFragment);
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

    private void onStartFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        exitDialog().show();
    }

    private AlertDialog exitDialog(){
        //exit dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Do you want exit from Eventer?")
                .setTitle("Exit from Eventer");
        //yes
        dialogBuilder.setPositiveButton("Yes", (dialog, id) -> {
            finish();
        });
        //no
        dialogBuilder.setNegativeButton("No", (dialog, id) -> {

        });

        return dialogBuilder.create();
    }

}

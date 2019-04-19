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

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;

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
    private String[] mPerm;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        mPerm = new String[]{
                READ_CONTACTS,
                READ_PHONE_STATE,
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE,
                FINE_LOCATION,
                COARSE_LOCATION

        };

        if(!hasPermissions(this, mPerm)){
            ActivityCompat.requestPermissions(this, mPerm, 1);
        }

        if(mAuth.getCurrentUser() != null) {
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            sendToLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();
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
            Log.i("Data", "Resume Email: " + mData.getUserEmail());
            Log.i("Data", "Resume Phone: " + mData.getUserPhone());
            Log.i("Data", "Resume Name: " + mData.getUserName());

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
                Intent settingsIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
                settingsIntent.putExtra("info", "0");
                startActivity(settingsIntent);
                finish();
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
            mData.setUserEmail(null);
            mAuth.signOut();


            sendToLogin();
        });
    }

    private void sendToLogin(){
        Intent loginIntent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(loginIntent);
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


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(READ_CONTACTS)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        finish();
                    }
                }else if (permission.equals(READ_PHONE_STATE)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        finish();
                    }
                }


            }
        }

    }

}

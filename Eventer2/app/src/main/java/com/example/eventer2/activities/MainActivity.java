package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.example.eventer2.fragments.AccountFragment;
import com.example.eventer2.fragments.EventFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

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

            mNewEventButton.setOnClickListener(v -> {
                Intent newEventIntent = new Intent(MainActivity.this, NewEventActivity.class);
                newEventIntent.putExtra("locationInfo", "0");
                startActivity(newEventIntent);
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

            if(userImageUrl != null && userName != null && userPhone != null) {
            }else {
                mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String imageUrl = task.getResult().getString("image");
                            String name = task.getResult().getString("name");
                            String phone = task.getResult().getString("phone");

                            if(imageUrl != null && name != null && phone != null) {
                                mData.setUserName(name);
                                mData.setUserImageUri(imageUrl);
                                mData.setUserPhone(phone);
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
                Intent settingsIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
                settingsIntent.putExtra("info", "0");
                startActivity(settingsIntent);
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
        mAuth.signOut();
        mData.setUserName(null);
//        mData.setUserPhone(null);
        sendToLogin();
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


}

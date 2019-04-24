package com.example.eventer2.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.example.eventer2.activities.ProfileSetupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {


    private CircleImageView mAccountImage;
    private TextView mName, mPhone, mEmail;
    private ImageView mDataBtn;
    private ImageView mEmailCheck;
    private ProgressBar mAccountProgress;

    private String currentUserId;
    private String imageUri;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String emailChecked = "Disabled";

    private ApplicationData mData;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        Log.i("EMAIL", "Email was:" + emailChecked);

        mData = (ApplicationData) getActivity().getApplication();

        mName = view.findViewById(R.id.account_name);
        mPhone = view.findViewById(R.id.account_phone);
        mAccountImage = view.findViewById(R.id.account_image);
        mAccountProgress = view.findViewById(R.id.account_progress);
        mEmail = view.findViewById(R.id.account_email);
        mDataBtn = view.findViewById(R.id.account_edit);
        mEmailCheck = view.findViewById(R.id.account_email_btn);

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        if(mAuth.getCurrentUser() != null) {
            mFirestore = FirebaseFirestore.getInstance();
            mAccountProgress.setVisibility(View.VISIBLE);

            userName = mData.getUserName();
            userPhone = mData.getUserPhone();
            imageUri = mData.getUserImageUri();
            userEmail = mData.getUserEmail();

//            Log.i("Username", userName);
            //postavljanje podataka
            mEmail.setText(userEmail);
            if(userName == null){
                mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            String name = task.getResult().getString("name");

                            if(name != null){
                                mData.setUserName(name);
                                mName.setText(name);
                            }
                        }
                    }
                });
            }else {
                mName.setText(userName);
            }

            if(imageUri == null){
                mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            String uri = task.getResult().getString("image");

                            if(uri != null){
                                mData.setUserImageUri(uri);
                                Glide.with(AccountFragment.this).load(uri).into(mAccountImage);
                                mAccountProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
            }else {
                Glide.with(AccountFragment.this).load(imageUri).into(mAccountImage);
                mAccountProgress.setVisibility(View.INVISIBLE);

            }
            mPhone.setText(userPhone);
            mEmail.setText(userEmail);

            mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String check = task.getResult().getString("emailNotification");
                        if(check != null){
                            emailChecked = check;

                            if(emailChecked.equals("Enabled")){
                                mEmailCheck.setBackgroundResource(R.drawable.account_enabled_icon);
                            }else if(emailChecked.equals("Disabled")){
                                mEmailCheck.setBackgroundResource(R.drawable.account_disabled_icon);
                            }
                        }
                    }
                }
            });

            mDataBtn.setOnClickListener(v -> {
                Intent setupIntent = new Intent(getActivity(), ProfileSetupActivity.class);
                setupIntent.putExtra("info", "0");
                startActivity(setupIntent);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(userEmail != null){
            if(userEmail.equals("")){
                mEmail.setText("Set your email...");
                mEmail.setOnClickListener(v -> {
                    Intent setupIntent = new Intent(getActivity(), ProfileSetupActivity.class);
                    setupIntent.putExtra("info", "0");
                    startActivity(setupIntent);
                });
            }
        }

        mEmailCheck.setOnClickListener( v -> {
            if(userEmail != null) {
                if(!userEmail.equals("")){
                    if(emailChecked.equals("Enabled")){
                        onEmailDisabled();
                    }else if(emailChecked.equals("Disabled")){
                            onEmailEnable();
                    }
                }else {
                    onEmailDisabled();
                    Toast.makeText(mData, "You must set email!", Toast.LENGTH_SHORT).show();
                }
            }else {
                onEmailDisabled();
                Toast.makeText(mData, "You must set email!", Toast.LENGTH_SHORT).show();
            }
        });

        if(emailChecked.equals("Enabled")){
            mEmailCheck.setBackgroundResource(R.drawable.account_enabled_icon);
        }else {
            mEmailCheck.setBackgroundResource(R.drawable.account_disabled_icon);
        }



    }

    private String onEmailEnable(){
        emailChecked = "Enabled";
        Map<String, Object> emailCheck = new HashMap<>();
        emailCheck.put("emailNotification", emailChecked);

        mFirestore.collection("Users").document(currentUserId).update(emailCheck).addOnSuccessListener(aVoid -> {
            mEmailCheck.setBackgroundResource(R.drawable.account_enabled_icon);
            Log.i("EMAIL", "Email was:" + emailChecked);

            Toast.makeText(mData, "Email notifications is enabled!", Toast.LENGTH_SHORT).show();
            mFirestore.collection("Contacts").document(currentUserId).update(emailCheck);
        });
        return emailChecked;
    }

    private String onEmailDisabled(){
        emailChecked = "Disabled";
        Map<String, Object> emailCheck = new HashMap<>();
        emailCheck.put("emailNotification", emailChecked);

        mFirestore.collection("Users").document(currentUserId).update(emailCheck).addOnSuccessListener(aVoid -> {
            mEmailCheck.setBackgroundResource(R.drawable.account_disabled_icon);
            Log.i("EMAIL", "Email was:" + emailChecked);

            Toast.makeText(mData, "Email notifications is disabled!", Toast.LENGTH_SHORT).show();
            mFirestore.collection("Contacts").document(currentUserId).update(emailCheck);
        });
        return emailChecked;
    }

}

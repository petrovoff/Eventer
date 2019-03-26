package com.example.eventer2.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.example.eventer2.activities.ProfileSetupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {


    private CircleImageView mAccountImage;
    private TextView mName, mPhone;
    private Button mDataBtn;
    private ProgressBar mAccountProgress;

    private String currentUserId;
    private String imageUri;
    private String userName;
    private String userPhone;

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


        mData = (ApplicationData) getActivity().getApplication();

        mName = view.findViewById(R.id.account_name);
        mPhone = view.findViewById(R.id.account_phone);
        mAccountImage = view.findViewById(R.id.account_image);
        mAccountProgress = view.findViewById(R.id.account_progress);
        mDataBtn = view.findViewById(R.id.account_edit);

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        if(mAuth.getCurrentUser() != null) {
            mFirestore = FirebaseFirestore.getInstance();
            mAccountProgress.setVisibility(View.VISIBLE);

            userName = mData.getUserName();
            userPhone = mData.getUserPhone();
            imageUri = mData.getUserImageUri();

            //postavljanje podataka
            mName.setText(userName);
            mPhone.setText(userPhone);
            Glide.with(AccountFragment.this).load(imageUri).into(mAccountImage);
            mAccountProgress.setVisibility(View.INVISIBLE);
//                mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            userName = task.getResult().getString("name");
//                            userPhone = task.getResult().getString("phone");
//                            imageUri = task.getResult().getString("image");
//
//                            mName.setText(userName);
//                            mPhone.setText(userPhone);
//
//                            Glide.with(AccountFragment.this).load(imageUri).into(mAccountImage);
//                        }
//                        mAccountProgress.setVisibility(View.INVISIBLE);
//                    }
//                });

            mDataBtn.setOnClickListener(v -> {
                Intent setupIntent = new Intent(getActivity(), ProfileSetupActivity.class);
                setupIntent.putExtra("info", "0");
                startActivity(setupIntent);
            });
        }

        return view;
    }

}

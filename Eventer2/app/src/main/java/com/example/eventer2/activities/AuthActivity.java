package com.example.eventer2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.rilixtech.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;

    private ApplicationData mData;

    private CountryCodePicker cpp;
    private AppCompatEditText mPhoneNumberInput;
    private TextView mErrorTextView;
    private Button mVerificationBtn;
    private ProgressBar mNumberProgress;

    private String mVerificationId;
    private String phoneNumber;
    private String[] mPerm;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
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

        if(mData.getUserPhone() != null){
            mPhoneNumberInput.setText(mData.getUserPhone());
        }

        mVerificationBtn.setOnClickListener(v -> {

            cpp.registerPhoneNumberTextView(mPhoneNumberInput);


            mNumberProgress.setVisibility(View.VISIBLE);
            mPhoneNumberInput.setEnabled(false);


            phoneNumber = cpp.getNumber();
            //number auth
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    AuthActivity.this,
                    mCallbacks
            );
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                mErrorTextView.setVisibility(View.VISIBLE);
                mPhoneNumberInput.setText("");
                mPhoneNumberInput.setEnabled(true);
                mVerificationBtn.setEnabled(true);
            }, 10000);
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mErrorTextView.setText("There was some error in verification! Check your number.");
                mErrorTextView.setVisibility(View.VISIBLE);
                mPhoneNumberInput.setText("");
                mPhoneNumberInput.setEnabled(true);
                mVerificationBtn.setEnabled(true);

                Toast.makeText(AuthActivity.this, "Error is " + e , Toast.LENGTH_LONG).show();
                mNumberProgress.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                mNumberProgress.setVisibility(View.INVISIBLE);
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //singin with phone number
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = task.getResult().getUser();

                mData.setUserPhone(phoneNumber);

                Intent profIntent = new Intent(AuthActivity.this, ProfileSetupActivity.class);
                profIntent.putExtra("info", "1");
                startActivity(profIntent);
                finish();
                // ...
            } else {
                // Sign in failed, display a message and update the UI
                mErrorTextView.setText("There was some error in login in! Check number.");
                mErrorTextView.setVisibility(View.VISIBLE);
                mPhoneNumberInput.setText("");
                mPhoneNumberInput.setEnabled(true);
                mVerificationBtn.setEnabled(true);

                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                }
            }
        });
    }

    private void init(){
        mData = (ApplicationData) getApplication();

        cpp = findViewById(R.id.ccp);
        mPhoneNumberInput = findViewById(R.id.auth_number_input);
        mErrorTextView = findViewById(R.id.auth_error_textview);
        mVerificationBtn = findViewById(R.id.auth_btn);
        mNumberProgress = findViewById(R.id.auth_progress);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

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

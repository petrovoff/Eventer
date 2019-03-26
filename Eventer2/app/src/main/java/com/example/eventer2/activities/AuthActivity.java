package com.example.eventer2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {

    private ApplicationData mData;

    private EditText mPhoneNumberInput;
    private TextView mErrorTextView;
    private Button mVerificationBtn;
    private ProgressBar mNumberProgress;

    private String mVerificationId;
    private String phoneNumber;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        init();

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }else {
                String number = tm.getLine1Number();
                String country = tm.getSimCountryIso();

                Log.i("SIM", "Number: " + number);
                Log.i("SIM", "Country: " + country);
//                mPhoneNumberInput.setText(number);

            }
        }


        mVerificationBtn.setOnClickListener(v -> {

            mNumberProgress.setVisibility(View.VISIBLE);
            mPhoneNumberInput.setEnabled(false);



            phoneNumber = mPhoneNumberInput.getText().toString();
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
        mPhoneNumberInput = findViewById(R.id.auth_number_input);
        mErrorTextView = findViewById(R.id.auth_error_textview);
        mVerificationBtn = findViewById(R.id.auth_btn);
        mNumberProgress = findViewById(R.id.auth_progress);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

    }
}

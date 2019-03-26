package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText mRegEmailText;
    private EditText mRegPasswordText;
    private EditText mRegConfirmPasswordText;
    private Button mRegBtn;
    private TextView mSignInBtn;
    private ProgressBar mRegProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

        mSignInBtn.setOnClickListener(v -> {
            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });

        mRegBtn.setOnClickListener(v -> {
            String email = mRegEmailText.getText().toString();
            String pass = mRegPasswordText.getText().toString();
            String confirm_pass = mRegConfirmPasswordText.getText().toString();


            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirm_pass)){
                if(pass.equals(confirm_pass)){
                    mRegProgress.setVisibility(View.VISIBLE);

                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Intent authIntent = new Intent(RegisterActivity.this, AuthActivity.class);
                            startActivity(authIntent);
                            finish();

//                            Intent profileIntent = new Intent(RegisterActivity.this, ProfileSetupActivity.class);
//                            profileIntent.putExtra("info", "1");
//                            startActivity(profileIntent);
//                            finish();
                        }else {
                            String errorMessage = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }else {
                    Toast.makeText(RegisterActivity.this, "Confirm Password and Password field doesn't match", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void init(){
        mRegEmailText = findViewById(R.id.register_email);
        mRegPasswordText = findViewById(R.id.register_password);
        mRegConfirmPasswordText = findViewById(R.id.register_confirm_password);
        mRegBtn = findViewById(R.id.register_btn);
        mSignInBtn = findViewById(R.id.register_text_btn);
        mRegProgress = findViewById(R.id.register_progress);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

}

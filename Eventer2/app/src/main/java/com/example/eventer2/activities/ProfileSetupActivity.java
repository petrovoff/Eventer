package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private ApplicationData mData;
    private CircleImageView mUserImage;
    private EditText mUsernameHolder;
    private EditText mEmail;
    private EditText mNumber;
    private TextView mMessage;
    private Button mSaveUserData;
    private ProgressBar mProgressBar;
    private ImageView mProgressDot1, mProgressDot2;

    private Uri mUserImageUri = null;

    private String mUserPhone;
    private String username;
    private String mUser_id;
    private String mInfo = "0";
    private boolean isChanged = false;

    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        init();

        mInfo = getIntent().getStringExtra("info");

        username = mData.getUserName();
        if(username != null){
            mUsernameHolder.setText(username);
//            mUsernameHolder.setEnabled(false);
            if(mInfo.equals("1")) {
                mMessage.setVisibility(View.VISIBLE);
            }
        }

        mUserPhone = mData.getUserPhone();
        if(mUserPhone != null) {

            mNumber.setText(mUserPhone);
            mNumber.setEnabled(false);
        }


        if(mInfo.equals("0")){
            mProgressDot1.setVisibility(View.INVISIBLE);
            mProgressDot2.setVisibility(View.INVISIBLE);
        }

        mUser_id = mAuth.getCurrentUser().getUid();
        mProgressBar.setVisibility(View.VISIBLE);
        mSaveUserData.setEnabled(false);

        if(mAuth.getCurrentUser() != null) {
            mFirestore.collection("Users").document(mUser_id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //proverava da li je korisnik postavio sliku i korisnicko ime
                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name"); //name na firestore
                        String phone = task.getResult().getString("phone"); //name na firestore
                        String image = task.getResult().getString("image"); //url slike koju cuvamo na firestore

                        mUserImageUri = Uri.parse(image); //lokalno cuvamo sliku da bismo mogli da samo ime
                        mUsernameHolder.setText(name);
                        mNumber.setText(phone);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.icon_profile);

                        Glide.with(ProfileSetupActivity.this).setDefaultRequestOptions(placeholderRequest)
                                .load(image).into(mUserImage);
                    } else {
                        if(mInfo.equals("1")) {
                            Toast.makeText(ProfileSetupActivity.this, "Data doesn't Exists", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(ProfileSetupActivity.this, "Set your data!", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(ProfileSetupActivity.this, "Firestore Retrieve Error", Toast.LENGTH_LONG).show();
                }
                mProgressBar.setVisibility(View.INVISIBLE);
                mSaveUserData.setEnabled(true);
            });

            //proveravamo da li u bazi postoje podaci, ako ne postoje cuvamo nove

            mSaveUserData.setOnClickListener(v -> {
                final String user_name = mUsernameHolder.getText().toString();
                String user_email = mEmail.getText().toString();

                if (!TextUtils.isEmpty(user_name) && mUserImageUri != null) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    if (isChanged) { //ako je slika promenjena
                        mUser_id = mAuth.getCurrentUser().getUid();

                        final StorageReference image_path = mStorageReference.child("profile_images").child(mUser_id + ".jpg");
                        image_path.putFile(mUserImageUri).addOnSuccessListener(taskSnapshot ->
                                image_path.getDownloadUrl().addOnSuccessListener(uri -> {
                                    storeFirestore(uri, user_name, mUserPhone, user_email); //skidamo url iz baze i cuvamo u uri
                                })).addOnFailureListener(e -> {
                                    Toast.makeText(ProfileSetupActivity.this, "The image is not Uploaded", Toast.LENGTH_LONG).show();
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                });
                    } else {
                        storeFirestore(null, user_name, mUserPhone, user_email);
                    }
                }
            });

            mUserImage.setOnClickListener(v -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ProfileSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(ProfileSetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        //pitamo da nam korisnik odobri dozvolu za koriscenje
                        ActivityCompat.requestPermissions(ProfileSetupActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        BringImagePicker();
                    }
                } else {
                    BringImagePicker();
                }
            });
        }
    }

    private void init(){
        mData = (ApplicationData)getApplication();
        mUserImage = findViewById(R.id.user_image);
        mUsernameHolder = findViewById(R.id.username);
        mNumber = findViewById(R.id.user_phone_number);
        mEmail = findViewById(R.id.user_email);
        mMessage = findViewById(R.id.user_back_msg);
        mSaveUserData = findViewById(R.id.user_save_data);
        mProgressBar = findViewById(R.id.setup_progress);
        mProgressDot1 = findViewById(R.id.profile_progress_dot1);
        mProgressDot2 = findViewById(R.id.profile_progress_dot2);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mUserImageUri = result.getUri();
                mUserImage.setImageURI(mUserImageUri); //postavljamo izabranu sliku

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }
    }

    //crop image activity - oblikovanje slike
    private void BringImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(ProfileSetupActivity.this);
    }

    private void storeFirestore(Uri uri, String user_name, String phone_number, String email){

        Uri download_uri;
        if(uri != null){
            download_uri = uri;
        } else {
            download_uri = mUserImageUri;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name );
        userMap.put("phone", phone_number );
        userMap.put("image", download_uri.toString());
        userMap.put("demoId", phone_number + "blabla");
        userMap.put("userId", mUser_id);
        userMap.put("email", email);

        Map<String, String> contactMap = new HashMap<>();
        contactMap.put("name", user_name );
        contactMap.put("number", phone_number );
        contactMap.put("image", download_uri.toString());
        contactMap.put("demoId", phone_number + "blabla");
        contactMap.put("userId", mUser_id);

        mFirestore.collection("Users").document(mUser_id).set(userMap).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(ProfileSetupActivity.this, "The user Settings are updated", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();

            }else {
                String error = task.getException().getMessage();
                Toast.makeText(ProfileSetupActivity.this, "Firestore Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
        mFirestore.collection("Contacts").document(mUser_id).set(contactMap);
        mProgressBar.setVisibility(View.INVISIBLE);

    }
}

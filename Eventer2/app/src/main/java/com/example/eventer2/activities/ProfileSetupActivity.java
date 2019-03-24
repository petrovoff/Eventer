package com.example.eventer2.activities;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {


    private CircleImageView mUserImage;
    private EditText mUserName;
    private EditText mNumber;
    private Button mSaveUserData;
    private ProgressBar mProgressBar;
    private ImageView mProgressDot1, mProgressDot2;

    private Uri mUserImageUri = null;

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
        if(mInfo.equals("0")){
            mProgressDot1.setVisibility(View.INVISIBLE);
            mProgressDot2.setVisibility(View.INVISIBLE);
        }

        mUser_id = mAuth.getCurrentUser().getUid();
        mProgressBar.setVisibility(View.VISIBLE);
        mSaveUserData.setEnabled(false);

        if(mAuth.getCurrentUser() != null) {
            mFirestore.collection("Users").document(mUser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        //proverava da li je korisnik postavio sliku i korisnicko ime
                        if (task.getResult().exists()) {

                            String name = task.getResult().getString("name"); //name na firestore
                            String phone = task.getResult().getString("phone"); //name na firestore
                            String image = task.getResult().getString("image"); //url slike koju cuvamo na firestore

                            mUserImageUri = Uri.parse(image); //lokalno cuvamo sliku da bismo mogli da samo ime
                            mUserName.setText(name);
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
                }
            });

            //proveravamo da li u bazi postoje podaci, ako ne postoje cuvamo nove

            mSaveUserData.setOnClickListener(v -> {
                final String user_name = mUserName.getText().toString();
                final String phone_number = mNumber.getText().toString();

                String finalPhone = changeNumber(phone_number);

                if (!TextUtils.isEmpty(user_name) && mUserImageUri != null) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    if (isChanged) { //ako je slika promenjena
                        mUser_id = mAuth.getCurrentUser().getUid();

                        final StorageReference image_path = mStorageReference.child("profile_images").child(mUser_id + ".jpg");
                        image_path.putFile(mUserImageUri).addOnSuccessListener(taskSnapshot ->
                                image_path.getDownloadUrl().addOnSuccessListener(uri -> {
                                    storeFirestore(uri, user_name, finalPhone); //skidamo url iz baze i cuvamo u uri
                                })).addOnFailureListener(e -> {
                                    Toast.makeText(ProfileSetupActivity.this, "The image is not Uploaded", Toast.LENGTH_LONG).show();
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                });
                    } else {
                        storeFirestore(null, user_name, finalPhone);
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
        mUserImage = findViewById(R.id.user_image);
        mUserName = findViewById(R.id.user_full_name);
        mNumber = findViewById(R.id.user_phone_number);
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

    private void storeFirestore(Uri uri, String user_name, String phone_number){

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

    public String changeNumber(String phone){
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
            phone = "+381" + phone;
        }
        return phone;
    }




}

package com.example.eventer2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventer2.Data.ApplicationData;
import com.example.eventer2.R;
import com.example.eventer2.adapters.InvitedBaseAdapter;
import com.example.eventer2.dialogs.DialogSms;
import com.example.eventer2.adapters.InvitedFriendsAdapter;
import com.example.eventer2.email.SendEmailTask;
import com.example.eventer2.models.InvitedFriend;
import com.example.eventer2.models.NumberCatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class InviteActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, DialogSms.DialogListener {

    private ApplicationData mData;
    private RecyclerView inviteFriendsView;
    private InvitedFriendsAdapter mAdapter;
    private InvitedBaseAdapter mBaseAdapter;

    private Toolbar inviteToolbar;
    private Button invitePhoneBtn;
    private Button inviteBaseBtn;
    private Button inviteSkipBtn;
    private Button inviteChecked;
    private Button inviteBaseChecked;

    private String name;
    private String phone;
    private List<InvitedFriend> list;
    private Cursor cursor;
    private RecyclerView.LayoutManager layoutManager;
    public static Boolean onOff;


    private String eventId;
    private String friendId;
    private String currentUserId;
    private String currentUserName;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        init();


        list = new ArrayList<>();
        //prosledjujemo listu kroz metodu getContacts() prilikom prvog startovanja activitija
        layoutManager = new LinearLayoutManager(this);
        if (mAuth.getCurrentUser() != null) {
            inviteFriendsView.setLayoutManager(layoutManager);

            mAdapter = new InvitedFriendsAdapter(getContacts(), this);
            mBaseAdapter = new InvitedBaseAdapter(list, this);
        }

        inviteSkipBtn.setOnClickListener(v -> {
            onSkipBtn();
        });
        //lista iz baze
        inviteBaseBtn.setOnClickListener(v -> {
            onBaseBtn();
            list.clear();

            inviteChecked.setVisibility(View.INVISIBLE);
            inviteBaseChecked.setVisibility(View.VISIBLE);
            mFirestore.collection("Contacts").get().addOnSuccessListener(queryDocumentSnapshots -> {
                cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);

                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");

                    String finalPhone = changeNumber(phone);
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            InvitedFriend friends = doc.getDocument().toObject(InvitedFriend.class);
                            String contactName = friends.getName();
                            String number = friends.getNumber();
                            String userId = friends.getUserId();
                            String id = friends.getDemoId();
                            String email = friends.getEmail();
                            String notification = friends.getEmailNotification();

                            if (number != null) {
                                if (number.equals(finalPhone)) {
                                    for (InvitedFriend invitedFriend : list) {
                                        if (invitedFriend.getNumber().equals(finalPhone)) {
                                            list.remove(invitedFriend);
                                            break;
                                        }
                                    }
                                    list.add(new InvitedFriend(name, finalPhone, eventId, id, userId, email, notification));
                                }
                            }
                        }
                    }
                }
                mBaseAdapter.updateList(list);
                inviteFriendsView.setAdapter(mBaseAdapter);
                mBaseAdapter.notifyDataSetChanged();

            });
        });
        //lista iz imenika
        invitePhoneBtn.setOnClickListener(v -> {
            onPhoneBtn();

            inviteChecked.setVisibility(View.VISIBLE);
            inviteBaseChecked.setVisibility(View.INVISIBLE);

            mFirestore.collection("Contacts").get().addOnSuccessListener(queryDocumentSnapshots -> {
                cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);

                if(cursor!=null) {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");

                        String finalPhone = changeNumber(phone);
                        String id = finalPhone + "blabla";
                        for (InvitedFriend invitedFriend : list) {
                            if (invitedFriend.getNumber().equals(finalPhone)) {
                                list.remove(invitedFriend);
                                break;
                            }
                        }
                        list.add(new InvitedFriend(name, changeNumber(phone), eventId, id));
                    }
                }

                mAdapter.updateList(list);
                inviteFriendsView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            });
        });

        //pozivanje iz imenika
        inviteChecked.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Send SMS permission.", Toast.LENGTH_LONG).show();
                    //pitamo da nam korisnik odobri dozvolu za koriscenje
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.SEND_SMS}, 2);
                }else {
                    smsDialog().show();
                }
            }
        });

        //pozivamo iz aplikacije
        inviteBaseChecked.setOnClickListener(v -> {
            int listSize = mData.friendsList.size();
            for(int i = 0; i < listSize; i++){
                String name = mData.friendsList.get(i).getName();
                String number = mData.friendsList.get(i).getNumber();
                String demo = mData.friendsList.get(i).getDemoId();
                String id = mData.friendsList.get(i).getUserId();
                String email = mData.friendsList.get(i).getEmail();
                String emailNotification = mData.friendsList.get(i).getEmailNotification();


                addInUsers(name, number, demo, id);
                addInEvents(id,demo);
                Toast.makeText(this, "Notification was sent!", Toast.LENGTH_LONG).show();

            }
            mData.friendsList.clear();
        });

        //prosledjujemo listu kao parametar
        layoutManager = new LinearLayoutManager(this);
        if (mAuth.getCurrentUser() != null) {
            inviteFriendsView.setLayoutManager(layoutManager);
            mAdapter = new InvitedFriendsAdapter(list, this);
            inviteFriendsView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void init(){
        mData = (ApplicationData) getApplication();
        inviteToolbar = findViewById(R.id.inviteToolbar);
        setSupportActionBar(inviteToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Search");
        invitePhoneBtn = findViewById(R.id.invite_btn_phone);
        inviteBaseBtn = findViewById(R.id.invite_btn_base);
        inviteFriendsView = findViewById(R.id.invite_list_view);
        inviteSkipBtn = findViewById(R.id.invite_skip_btn);
        inviteChecked = findViewById(R.id.invite_checked);
        inviteBaseChecked = findViewById(R.id.invite_base_checked);


        eventId = getIntent().getStringExtra("eventId");
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        onOff = true;
    }

    //ucitava se prvi put lista sa svim korisnicima
    private List<InvitedFriend> getContacts () {
        onPhoneBtn();
        list.clear();
        inviteChecked.setVisibility(View.VISIBLE);
        inviteBaseChecked.setVisibility(View.INVISIBLE);

        mFirestore.collection("Contacts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);

            if(cursor!=null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");

                    String finalPhone = changeNumber(phone);
                    String id = finalPhone + "blabla";
                    for (InvitedFriend invitedFriend : list) {
                        if (invitedFriend.getNumber().equals(finalPhone)) {
                            list.remove(invitedFriend);
                            break;
                        }
                    }
                    list.add(new InvitedFriend(name, changeNumber(phone), eventId, id));
                }
            }
            mAdapter.updateList(list);
            inviteFriendsView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        });
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.invite_search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        List<InvitedFriend> newList = new ArrayList<>();

        for(InvitedFriend friend : list){
            if(friend.getName().toLowerCase().contains(userInput)){
                newList.add(friend);
            }
        }
        mAdapter.updateList(newList);
        return true;
    }

    public void onBaseBtn(){
        inviteBaseBtn.setEnabled(false);
        invitePhoneBtn.setEnabled(true);

        inviteBaseBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        inviteBaseBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
        inviteBaseBtn.setTextSize(16);
        invitePhoneBtn.setTextSize(12);
        invitePhoneBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        invitePhoneBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    public void onPhoneBtn(){
        inviteBaseBtn.setEnabled(true);
        invitePhoneBtn.setEnabled(false);

        invitePhoneBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        invitePhoneBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
        invitePhoneBtn.setTextSize(16);
        inviteBaseBtn.setTextSize(12);
        inviteBaseBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        inviteBaseBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void onSkipBtn(){
        finish();
    }

    public String changeNumber(String phone){
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
            phone = "+381" + phone;
        }
        return phone;
    }

    public void onSendSms(String eventName, String location, String startDate, String startTime, String num){
        SmsManager sm = SmsManager.getDefault();
        Log.i("INVITE", "" + num);
        String number = "+381645871290";
//        String number = num;
        String dateTime = dateConverter(startDate) + " u " + startTime;
        String msg;

        msg = "Pozvani ste na dogadjaj: " + eventName + " koji pocinje " + dateTime  + ". " +
                "Detalje dogadjaja mozete pogledati putem aplikacije Eventer.";
        sm.sendTextMessage(number,null, msg,null,null);
        Log.i("INVITE","Poruka poslata");

//        sm.sendTextMessage(number,null, msg,null,null);
    }

    private void addInUsers(String name, String number, String demo, String id){

        HashMap<String, Object> invitedMap = new HashMap<>();
        invitedMap.put("name", name);
        invitedMap.put("number", number);
        invitedMap.put("demoId", demo);
        invitedMap.put("userId", id);
        invitedMap.put("eventId", eventId);
        invitedMap.put("arrival", "No answer");

        mFirestore.collection("Events/" + eventId + "/Guests").document(demo).set(invitedMap);
        Toast.makeText(this, "Friends was invited!", Toast.LENGTH_SHORT).show();
        Log.i("INVITE","Push notifikacija poslata");
    }

    private void addInEvents(String id, String demo){
        mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    String eventName = task.getResult().getString("name");
                    String location = task.getResult().getString("eventLocation");
                    String downloadUri = task.getResult().getString("image_url");
                    String theme = task.getResult().getString("theme");
                    String startDate = task.getResult().getString("startDate");
                    String endDate = task.getResult().getString("endDate");
                    String startTime = task.getResult().getString("startTime");
                    String endTime = task.getResult().getString("endTime");
                    String author = task.getResult().getString("authorId");
                    String eventId = task.getResult().getString("eventId");

                    HashMap<String, String> eventMap = new HashMap<>();
                    eventMap.put("eventId", eventId);
                    eventMap.put("image_url", downloadUri);
                    eventMap.put("name", eventName);
                    eventMap.put("theme", theme);
                    eventMap.put("eventLocation", location);
                    eventMap.put("startDate", startDate);
                    eventMap.put("endDate", endDate);
                    eventMap.put("startTime", startTime);
                    eventMap.put("endTime", endTime);
                    eventMap.put("authorId", author);

                    if(id != null){
                        mFirestore.collection("Users/" + id + "/InvitedEvents").document(eventId).set(eventMap);
                    }else {
                        mFirestore.collection("WithoutAcc").document(demo).set(eventMap);
                    }
                }
            }
        });
    }

    private void addInEventsSMS(String number, String id, String demo){
        mFirestore.collection("Events").document(eventId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().exists()){
                    String eventName = task.getResult().getString("name");
                    String location = task.getResult().getString("eventLocation");
                    String downloadUri = task.getResult().getString("image_url");
                    String theme = task.getResult().getString("theme");
                    String startDate = task.getResult().getString("startDate");
                    String endDate = task.getResult().getString("endDate");
                    String startTime = task.getResult().getString("startTime");
                    String endTime = task.getResult().getString("endTime");
                    String author = task.getResult().getString("authorId");
                    String eventId = task.getResult().getString("eventId");

                    HashMap<String, String> eventMap = new HashMap<>();
                    eventMap.put("eventId", eventId);
                    eventMap.put("image_url", downloadUri);
                    eventMap.put("name", eventName);
                    eventMap.put("theme", theme);
                    eventMap.put("eventLocation", location);
                    eventMap.put("startDate", startDate);
                    eventMap.put("endDate", endDate);
                    eventMap.put("startTime", startTime);
                    eventMap.put("endTime", endTime);
                    eventMap.put("authorId", author);

                    onSendSms(eventName,location,startDate,startTime, number);
                    if(id != null){
                        mFirestore.collection("Users/" + id + "/InvitedEvents").document(eventId).set(eventMap);
                    }else {
                        mFirestore.collection("WithoutAcc").document(demo).set(eventMap);
                    }
                }
            }
        });
    }

    private AlertDialog smsDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to invite selected friends via SMS?")
                .setTitle("SMS Service");
        builder.setPositiveButton("Yes", (dialog, id) -> {
            int listSize = mData.friendsList.size();
            NumberCatcher num = new NumberCatcher();

            for(int i = 0; i < listSize; i++){
                String name = mData.friendsList.get(i).getName();
                String number = mData.friendsList.get(i).getNumber();
                String demo = mData.friendsList.get(i).getDemoId();
                String userId = mData.friendsList.get(i).getUserId();

                addInUsers(name, number, demo, userId);
                addInEventsSMS(number, userId,demo);


            }
            mData.friendsList.clear();
            Toast.makeText(this, "Message was sent!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", (dialog, id) -> {
            Toast.makeText(this, "Denied!", Toast.LENGTH_SHORT).show();
        });

        return builder.create();
    }

    public String dateConverter(String date){
        String year = date.substring(0,4);
        String month = date.substring(5,7);
        String day = date.substring(8);

        switch (month){
            case "01":
                month = "Januar";
                break;
            case "02":
                month = "Februar";
                break;
            case "03":
                month = "Mart";
                break;
            case "04":
                month = "April";
                break;
            case "05":
                month = "Maj";
                break;
            case "06":
                month = "Jun";
                break;
            case "07":
                month = "Jul";
                break;
            case "08":
                month = "Avgust";
                break;
            case "09":
                month = "Septembar";
                break;
            case "10":
                month = "Oktobar";
                break;
            case "11":
                month = "Novembar";
                break;
            case "12":
                month = "Decembar";
                break;

        }
        Log.i("EVENT", "Date" + month);
        return day + ". " + month + " " + year + ".";
    }

    @Override
    public void onConfirm(boolean change) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.SEND_SMS)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {

                    }
                }
            }
        }
    }

}

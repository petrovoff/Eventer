package com.example.eventer2.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventer2.R;
import com.example.eventer2.adapters.EventRecyclerAdapter;
import com.example.eventer2.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class InvitedEventFragment extends Fragment {

    View mView;

    private RecyclerView mEventRecyclerView;
    private List<Event> mEventList;
    private EventRecyclerAdapter mEventRecyclerAdapter;

    private Date today;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;


    public InvitedEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_invited_event, container, false);

        mEventList = new ArrayList<>();
        mEventRecyclerView = mView.findViewById(R.id.invited_event_list_view);
        today = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");



        mAuth = FirebaseAuth.getInstance();
        mEventRecyclerAdapter = new EventRecyclerAdapter(mEventList);
        mEventRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mEventRecyclerView.setAdapter(mEventRecyclerAdapter);

        final String currentUserId = mAuth.getCurrentUser().getUid();

        if(mAuth.getCurrentUser() != null){
            mFirestore = FirebaseFirestore.getInstance();


            mFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
               if(task.isSuccessful()){
                   if(task.getResult().exists()){
                       String demoId = task.getResult().getString("demoId");

                       mFirestore.collection("Events").orderBy("startDate", Query.Direction.ASCENDING).addSnapshotListener((queryDocumentSnapshots, e) -> {
                          if(queryDocumentSnapshots != null){
                              for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()) {
                                  if (doc.getType() == DocumentChange.Type.ADDED) {
                                      String eventId = doc.getDocument().getId();

                                      mFirestore.collection("Events/" + eventId + "/Guests").document(demoId).get().addOnCompleteListener(guestTask -> {
                                          if (guestTask.isSuccessful()) {
                                              if (guestTask.getResult().exists()) {
                                                  Event event = doc.getDocument().toObject(Event.class).returnId(eventId);
                                                  String authorId = event.getAuthorId();

                                                  String endDate = event.getEndDate();
                                                  String endTime = event.getEndTime();
                                                  if (endDate != null) {
                                                      try {
                                                          Date eventEndDate = dateFormat.parse(endDate);

                                                          if (today.before(eventEndDate)) {
                                                              if (!currentUserId.equals(authorId)) {
                                                                  mEventList.add(event);
                                                                  mEventRecyclerAdapter.notifyDataSetChanged();
                                                              }
                                                          }
                                                      } catch (ParseException e1) {
                                                          e1.printStackTrace();
                                                      }

                                                  }

                                              }
                                          }
                                      });
                                  }
                              }
                          }
                       });

                   }
               }


            });


        }
        return mView;
    }

}

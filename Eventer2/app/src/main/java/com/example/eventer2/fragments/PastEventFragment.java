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
public class PastEventFragment extends Fragment {

    View mView;

    private RecyclerView mEventRecyclerView;
    private List<Event> mEventList;
    private EventRecyclerAdapter mEventRecyclerAdapter;
    private Date today;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;

    public PastEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_past_event, container, false);

        mEventList = new ArrayList<>();
        mEventRecyclerView = mView.findViewById(R.id.past_event_list_view);
        today = new Date();


        mAuth = FirebaseAuth.getInstance();

        mEventRecyclerAdapter = new EventRecyclerAdapter(mEventList);
        mEventRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mEventRecyclerView.setAdapter(mEventRecyclerAdapter);





        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();


        if(mAuth.getCurrentUser() != null){

            String currentUserId = mAuth.getCurrentUser().getUid();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            mFirestore = FirebaseFirestore.getInstance();

            mFirestore.collection("Events").orderBy("startDate", Query.Direction.ASCENDING).addSnapshotListener((queryDocumentSnapshots, e) -> {
                if(queryDocumentSnapshots != null){
                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                        String eventId = doc.getDocument().getId();

                        Event event = doc.getDocument().toObject(Event.class).returnId(eventId);
                        String endDate = event.getEndDate();
                        String authorId = event.getAuthorId();

                        try {
                            Date endDateEvent = dateFormat.parse(endDate);
                            if(today.after(endDateEvent)){

                                if(!authorId.equals(currentUserId)) {
                                    mFirestore.collection("Users/" + currentUserId + "/InvitedEvents").addSnapshotListener((eventQueryDocument, e1) -> {
                                        if (eventQueryDocument != null) {
                                            for (DocumentChange eventDoc : eventQueryDocument.getDocumentChanges()) {
                                                Event eventGuest = eventDoc.getDocument().toObject(Event.class).returnId(eventId);
                                                String date = eventGuest.getEndDate();

                                                if (date != null) {
                                                    try {
                                                        Date eventEndDate = dateFormat.parse(date);

                                                        if (today.after(eventEndDate)) {

                                                            mEventList.add(eventGuest);
                                                            mEventRecyclerAdapter.notifyDataSetChanged();
                                                        }
                                                    } catch (ParseException e2) {
                                                        e2.printStackTrace();
                                                    }

                                                }
                                            }
                                        }
                                    });
                                }else {
                                    mFirestore.collection("Users/" + currentUserId + "/CreatedEvents").addSnapshotListener((eventQueryDocument, e1) -> {
                                        if (eventQueryDocument != null) {
                                            for (DocumentChange eventDoc : eventQueryDocument.getDocumentChanges()) {
                                                if (eventDoc.getType() == DocumentChange.Type.ADDED) {
                                                    Event eventGuest = eventDoc.getDocument().toObject(Event.class).returnId(eventId);
                                                    String date = eventGuest.getEndDate();

                                                    if (date != null) {
                                                        try {
                                                            Date eventEndDate = dateFormat.parse(date);

                                                            if (today.after(eventEndDate)) {

                                                                mEventList.add(eventGuest);
                                                                mEventRecyclerAdapter.notifyDataSetChanged();
                                                            }
                                                        } catch (ParseException e2) {
                                                            e2.printStackTrace();
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    });
                                }

                            }

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }





                    }
                }
            });
        }
    }
}

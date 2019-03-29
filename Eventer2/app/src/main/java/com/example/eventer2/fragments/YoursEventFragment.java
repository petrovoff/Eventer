package com.example.eventer2.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventer2.R;
import com.example.eventer2.adapters.EventRecyclerAdapter;
import com.example.eventer2.models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class YoursEventFragment extends Fragment {

    View mView;

    private RecyclerView mEventRecyclerView;
    private List<Event> mEventList;
    private EventRecyclerAdapter mEventRecyclerAdapter;

    private Date today;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;


    public YoursEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_yours_event, container, false);

        mEventList = new ArrayList<>();
        mEventRecyclerView = mView.findViewById(R.id.your_event_list_view);
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

            final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            final String currentUserId = mAuth.getCurrentUser().getUid();
            mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("Users/" + currentUserId + "/CreatedEvents").orderBy("startDate", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots != null){
                        for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                String eventId = doc.getDocument().getId();
                                final Event event = doc.getDocument().toObject(Event.class).returnId(eventId);

                                mFirestore.collection("Users/" + currentUserId + "/CreatedEvents").document(eventId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(task.getResult().exists()){

                                                String endDate = task.getResult().getString("endDate");
                                                try {
                                                    Date eventEndDate = dateFormat.parse(endDate);
                                                    if(today.before(eventEndDate)) {
                                                        mEventList.add(event);
                                                        mEventRecyclerAdapter.notifyDataSetChanged();
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
                }
            });
        }
    }
}

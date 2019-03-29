package com.example.eventer2.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventer2.R;
import com.example.eventer2.adapters.EventViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private EventViewPagerAdapter mAdapter;

    public EventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        mTabLayout = view.findViewById(R.id.event_tablayout_id);
        mViewPager = view.findViewById(R.id.event_viewpager_id);



        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getFragmentManager() != null) {
            mAdapter = new EventViewPagerAdapter(getChildFragmentManager());
        }

        mAdapter.addFragments(new YoursEventFragment(), "Yours");
        mAdapter.addFragments(new InvitedEventFragment(), "Invited");
        mAdapter.addFragments(new PastEventFragment(), "Past");

        //Adapter setup
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}

package com.example.eventer2.GoogleMapAndPlaces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.eventer2.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mView;
    private Context mContext;


    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);

    }
    private void newWindowText(Marker marker, View view){
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.custom_info_title);

        if(!title.equals("")){
            tvTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.custom_info_snippet);

        if(!snippet.equals("")){
            tvSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        newWindowText(marker, mView);
        return mView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        newWindowText(marker, mView);
        return mView;
    }
}

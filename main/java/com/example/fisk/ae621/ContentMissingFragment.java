package com.example.fisk.ae621;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by fisk on 3/14/18.
 */

public class ContentMissingFragment extends Fragment {
    public ContentMissingFragment() {
    }

    public static ContentMissingFragment newInstance() {
        ContentMissingFragment fragment = new ContentMissingFragment();
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        return fragment;
    }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    
         View rootView = inflater.inflate(R.layout.content_missing_layout, container, false);
         return rootView;
    
     }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }
}

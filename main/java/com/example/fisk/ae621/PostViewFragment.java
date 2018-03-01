package com.example.fisk.ae621;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fisk on 2/25/18.
 */

public class PostViewFragment extends Fragment {

    JSONObject mPostData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        String rawPostData = getArguments().getString("json", "[]");

        try {
            mPostData = new JSONObject(rawPostData);
        } catch (JSONException e) {
            Log.e("JSONException", "PostViewFragment.onCreateView(): "+e.toString());
        }

        return inflater.inflate(R.layout.post_view_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // When Using Fragments, Initialize Views here as opposed to onCreate.
        // This is because when onCreate is called, the activity may not have been attached to it yet.
        // As such getActivity will return null.

        super.onActivityCreated(savedInstanceState);

        if (mPostData != null) {

            // Initialize widgets
            ImageView mMainImage;

            mMainImage = getActivity().findViewById(R.id.pvMainImage);

            // Fill widgets
            try {
                Glide.with(getContext()).load(mPostData.getString("file_url")).into(mMainImage);
            } catch (JSONException e) {
                Log.e("JSONException", "PostViewFragment.onActivityCreated(): "+e.toString());
            }

        }
        else {
            Log.e("PostViewFragment", "mPostData is not set!");
        }
    }
}

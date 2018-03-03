package com.example.fisk.ae621;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import junit.framework.Test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostViewActivity extends AppCompatActivity {

    // Todo: In the far off future when you can favorite images there needs to be an indicator that you managed to do so.

    JSONObject mPostData;

    ImageView    mMainImage;
    TextView     mPostIdPrimary;
    TextView     mPostArtistPrimary;
    LinearLayout mStatusLayout;
    LinearLayout mStatusFlagged;
    LinearLayout mStatusPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_view_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            mPostData = new JSONObject(getIntent().getStringExtra("post_data"));
        } catch (JSONException e) {
            Log.e("JSONException", "PostViewActivity.onCreate(): "+e.toString());
        }

        if (mPostData != null) {

            // Initialize widgets

            mMainImage         = findViewById(R.id.pvMainImage);
            mPostIdPrimary     = findViewById(R.id.pvPostIdPrimary);
            mPostArtistPrimary = findViewById(R.id.pvPostArtistPrimary);
            mStatusLayout      = findViewById(R.id.pvStatusLayout);
            mStatusFlagged     = findViewById(R.id.pvStatusFlaggedIndicator);
            mStatusPending     = findViewById(R.id.pvStatusPendingIndicator);

            // Fill widgets
            // Todo: There should be at least 2 modes of scale: Fill Screen, and Actual Resolution
            // Todo: Don't forget about this character ▼
            try {

                Glide.with(this).load(mPostData.getString("file_url")).into(mMainImage);
                mPostIdPrimary.setText(buildIdString(mPostData.getString("id")));
                mPostArtistPrimary.setText(buildArtistString(mPostData.getString("artist")));

                String status = mPostData.getString("status");
                if (status.equals("flagged")) {
                    mStatusPending.setVisibility(View.GONE);
                }
                else if (status.equals("pending")) {
                    mStatusFlagged.setVisibility(View.GONE);
                }
                else if (status.equals("active")) {
                    mStatusLayout.setVisibility(View.GONE);
                }

                // Touch Gestures
                // Double Tap
                mMainImage.setOnTouchListener(new View.OnTouchListener() {
                    private GestureDetector gestureDetector = new GestureDetector(PostViewActivity.this, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            Log.d("TEST", "onDoubleTap");
                            setScaleTypeFill();
                            return super.onDoubleTap(e);
                        }

                    });

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                        gestureDetector.onTouchEvent(event);
                        return true;
                    }
                });

            } catch (JSONException e) {
                Log.e("JSONException", "PostViewFragment.onActivityCreated(): "+e.toString());
            }

        }
        else {
            Log.e("PostViewFragment", "mPostData is not set!");
        }

    }

    private void setScaleTypeRes() {
        // Scale values reported in the data to density-independent values

        try {
            int reportedThumbnailHeight = mPostData.getInt("width");
            int trueThumbnailHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reportedThumbnailHeight, this.getResources().getDisplayMetrics());

            int reportedThumbnailWidth = mPostData.getInt("height");
            int trueThumbnailWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reportedThumbnailWidth, this.getResources().getDisplayMetrics());

            LinearLayout.LayoutParams thumbnailParams = new LinearLayout.LayoutParams(trueThumbnailWidth, trueThumbnailHeight);

            mMainImage.setLayoutParams(thumbnailParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setScaleTypeFill() {
        try {
            LinearLayout contentLayout = findViewById(R.id.pvContentLayout);

            // Data required for scaling
            int parentLayoutWidth   = contentLayout.getWidth()-(contentLayout.getPaddingLeft()+contentLayout.getPaddingRight()); // account for layout padding
            int postMainImageWidth  = mPostData.getInt("width");
            int postMainImageHeight = mPostData.getInt("height");

            // Scaling Formula Image Height
            int scaledImageHeight = (postMainImageHeight*parentLayoutWidth)/postMainImageWidth;

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(parentLayoutWidth, scaledImageHeight);
            mMainImage.setLayoutParams(imageParams);
        }
        catch (JSONException e) {
            Log.e("JSONException", "PostViewActivity.setScaleTypeFill(): "+e.toString());
        }
    }

    private String buildIdString(String id) {
        return "#"+id+":";
    }

    private String buildArtistString(String artists) {
        try {

            JSONArray artistData  = new JSONArray(artists);
            String    finalString = "";

            for (int i = 0; i < artistData.length(); i++) {
                finalString += artistData.getString(i)+" "; // append the artist and a space character for formatting purposes
            }

            return finalString;

        } catch (JSONException e) {
            Log.e("JSONException", "PostViewFragment.buildArtistString(): "+e.toString());
            return null;
        }
    }

}

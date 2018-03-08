package com.example.fisk.ae621;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostViewActivity extends AppCompatActivity {

    // Todo: In the far off future when you can favorite images there needs to be an indicator that you managed to do so.

    JSONObject mPostData;

    ImageView    mMainImage;
    VideoView    mMainVideo;
    TextView     mPostIdPrimary;
    TextView     mPostArtistPrimary;
    ImageButton  mDevViewDataModel;

    LinearLayout mStatusLayout;
    LinearLayout mStatusFlagged;
    LinearLayout mStatusPending;
    TextView     mDelReason;

    LinearLayout mChildPostLayout;
    TextView     mChildPostHeader;
    boolean      isChildPostLayoutOpen;

    LinearLayout mDescriptionLayout;
    TextView     mDescriptionHeader;
    TextView     mDescriptionBody;
    boolean      isDescriptionOpen;

    @SuppressLint("ClickableViewAccessibility")
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
            mMainVideo         = findViewById(R.id.pvMainVideo);
            mPostIdPrimary     = findViewById(R.id.pvPostIdPrimary);
            mPostArtistPrimary = findViewById(R.id.pvPostArtistPrimary);
            mDevViewDataModel  = findViewById(R.id.pvDevViewDataModelButton);

            mStatusLayout      = findViewById(R.id.pvStatusLayout);
            mStatusFlagged     = findViewById(R.id.pvStatusFlaggedIndicator);
            mStatusPending     = findViewById(R.id.pvStatusPendingIndicator);
            mDelReason         = findViewById(R.id.pvDelReason);

            mChildPostLayout      = findViewById(R.id.pvChildPostsIndicator);
            mChildPostHeader      = findViewById(R.id.pvChildPostHeader);
            isChildPostLayoutOpen = true;

            mDescriptionLayout = findViewById(R.id.pvDescriptionLayout);
            mDescriptionHeader = findViewById(R.id.pvDescriptionHeader);
            mDescriptionBody   = findViewById(R.id.pvDescriptionBody);
            isDescriptionOpen  = true;

            // Fill widgets
            // Todo: There should be at least 2 modes of scale: Fill Screen, and Actual Resolution
            // Todo: Don't forget about this character ▼ or this one ►
            try {

                setMainView();

                mPostIdPrimary.setText(buildIdString(mPostData.getString("id")));
                mPostArtistPrimary.setText(buildArtistString(mPostData.getString("artist")));

                setStatusBanner();

                setChildPostWidgets();

                setDescriptionWidgets();

                // Touch Gestures
                // Double Tap

                mMainImage.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    private GestureDetector gestureDetector = new GestureDetector(PostViewActivity.this, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            Log.d("TEST", "onDoubleTap");
                            setScaleTypeFill();
                            return super.onDoubleTap(e);
                        }

                    });
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                        gestureDetector.onTouchEvent(event);
                        return true;
                    }
                });

                // Goto Data Model
                mDevViewDataModel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gotoDataModelViewer();
                    }
                });

            } catch (JSONException e) {
                Log.e("JSONException", "PostViewFragment.onCreate(): "+e.toString());
            }

        }
        else {
            Log.e("PostViewFragment", "mPostData is not set!");
        }

        if (savedInstanceState != null) {
            isDescriptionOpen = savedInstanceState.getBoolean("desc_open");
            setDescriptionWidgets();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("desc_open", isDescriptionOpen);
        setDescriptionOpen(isDescriptionOpen);
    }

    private void setMainView() {
        try {
            if (!mPostData.getString("file_ext").equals("webm")) {
                mMainVideo.setVisibility(View.GONE);

                Glide.with(this).load(mPostData.getString("file_url")).into(mMainImage);
            }
            else {
                mMainImage.setVisibility(View.GONE);

                // Layout Params are necessary get the video to display in the first place,
                // as the height that the video is scaled to is based on the height of its container
                int videoScaledHeight = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        mPostData.getInt("height"),
                        this.getResources().getDisplayMetrics());

                LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        videoScaledHeight);

                mMainVideo.setLayoutParams(videoParams);
                mMainVideo.setVideoURI(Uri.parse(mPostData.getString("file_url")));
                mMainVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mMainVideo.start();
                    }
                });
                mMainVideo.start();
            }

        } catch (JSONException e) {
            Log.e("JSONException", "PostViewFragment.setStatusBanner(): "+e.toString());
        }
    }

    private void setStatusBanner() {
        String status = null;
        try {
            status = mPostData.getString("status");

            switch (status) {
                case "flagged":
                    mStatusPending.setVisibility(View.GONE);
                    mDelReason.setText(mPostData.getString("delreason"));
                    break;
                case "pending":
                    mStatusFlagged.setVisibility(View.GONE);
                    break;
                case "active":
                    mStatusLayout.setVisibility(View.GONE);
                    break;
            }
        } catch (JSONException e) {
            Log.e("JSONException", "PostViewFragment.setStatusBanner(): "+e.toString());
        }
    }

    private void setChildPostWidgets() {
        // children will have to be split by commas as it is a string in the data and not an array
    }

    private void setDescriptionWidgets() {
        try {
            if (mPostData.getString("description").equals("")) {
                mDescriptionLayout.setVisibility(View.GONE);
            }
            else {
                mDescriptionBody.setText(mPostData.getString("description"));
                setDescriptionOpen(isDescriptionOpen);
            }

            // Toggle Description
            mDescriptionHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleDescription();
                }
            });


        } catch (JSONException e) {
            Log.e("JSONException", "PostViewFragment.setDescriptionWidgets(): "+e.toString());
        }
    }

    private void setDescriptionOpen(boolean open) {
        if (!open) {
            // if its open, then close it
            mDescriptionHeader.setText("► Description");
            mDescriptionBody.setVisibility(View.GONE);
        }
        else {
            mDescriptionHeader.setText("▼ Description");
            mDescriptionBody.setVisibility(View.VISIBLE);
        }
    }

    private void toggleDescription() {
        if (isDescriptionOpen) {
            // if its open, then close it
            mDescriptionHeader.setText("► Description");
            mDescriptionBody.setVisibility(View.GONE);
            isDescriptionOpen = false;
        }
        else {
            mDescriptionHeader.setText("▼ Description");
            mDescriptionBody.setVisibility(View.VISIBLE);
            isDescriptionOpen = true;
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

    private void gotoDataModelViewer() {
        // Set the data for the selected post, and start the new activity
        Intent intent = new Intent(this, DevDataModelViewerActivity.class);
        intent.putExtra("data", new JSONArray().put(mPostData).toString());
        startActivity(intent);
    }

}

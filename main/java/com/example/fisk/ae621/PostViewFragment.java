package com.example.fisk.ae621;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
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

/**
 * Created by fisk on 2/25/18.
 */

public class PostViewFragment extends Fragment {

    // TODO: PostView should not scroll unless absolutely necessary.
    // Posts are much more comfortably viewed if the entire image is visible at once.
    // Unless it is a comic strip (Consider automatic detection based on aspect ratio)
    // Extremely short and wide images are going to be a pain in the ass (Consider putting this off until the app is revealed to the community)
    // Don't implement pinch zooming until you can figure out how to do it gracefully.
    JSONObject  mPostData;

    ImageView   mMainImage;
    VideoView   mMainVideo;

    ImageButton mDevViewDataModel;

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

    public static PostViewFragment newInstance(String data) {
        PostViewFragment fragment = new PostViewFragment();
        Bundle args = new Bundle();
        args.putString("post_data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.post_view_layout, container, false);

        if (getArguments() != null) {
            String rawPostData = getArguments().getString("post_data");

            try {
                mPostData = new JSONObject(rawPostData);
            } catch (JSONException e) {
                Log.e("JSONException", "PostViewFragment.onCreateView(): " + e.toString());
            }
        }

        if (mPostData != null) {

            // Initialize widgets

            mMainImage         = rootView.findViewById(R.id.pvMainImage);
            mMainVideo         = rootView.findViewById(R.id.pvMainVideo);

            mDevViewDataModel  = rootView.findViewById(R.id.pvDevViewDataModelButton);

            mStatusLayout      = rootView.findViewById(R.id.pvStatusLayout);
            mStatusFlagged     = rootView.findViewById(R.id.pvStatusFlaggedIndicator);
            mStatusPending     = rootView.findViewById(R.id.pvStatusPendingIndicator);
            mDelReason         = rootView.findViewById(R.id.pvDelReason);

            mChildPostLayout      = rootView.findViewById(R.id.pvChildPostsIndicator);
            mChildPostHeader      = rootView.findViewById(R.id.pvChildPostHeader);
            isChildPostLayoutOpen = true;

            mDescriptionLayout = rootView.findViewById(R.id.pvDescriptionLayout);
            mDescriptionHeader = rootView.findViewById(R.id.pvDescriptionHeader);
            mDescriptionBody   = rootView.findViewById(R.id.pvDescriptionBody);
            isDescriptionOpen  = true;

            // Fill widgets
            // Todo: There should be at least 2 modes of scale: Fill Screen, and Actual Resolution
            // Todo: Don't forget about this character ▼ or this one ►

            setMainView();

            setStatusBanner();

            setChildPostWidgets();

            setDescriptionWidgets();

            // Touch Gestures
            // Double Tap

            mMainImage.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
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

        }
        else {
            Log.e("PostViewFragment", "mPostData is not set!");
        }

        if (savedInstanceState != null) {
            isDescriptionOpen = savedInstanceState.getBoolean("desc_open");
            setDescriptionWidgets();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        // When Using Fragments, Initialize Views here as opposed to onCreate.
        // This is because when onCreate is called, the activity may not have been attached to it yet.
        // As such getActivity will return null.

        super.onActivityCreated(savedInstanceState);



    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("desc_open", isDescriptionOpen);
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
            LinearLayout contentLayout = getActivity().findViewById(R.id.pvContentLayout);

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
        Intent intent = new Intent(getContext(), DevDataModelViewerActivity.class);
        intent.putExtra("data", new JSONArray().put(mPostData).toString());
        startActivity(intent);
    }

}

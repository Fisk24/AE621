package com.example.fisk.ae621;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by fisk on 2/17/18.
 */

public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.PostItemViewHolder> {

    private Context        parentContext;
    private LayoutInflater mLayoutInflater;
    private JSONArray      mPostItems;

    private PostItemClickListener postItemClickListener;

    // data is passed into the constructor
    PostItemAdapter(Context context, JSONArray items) {
        parentContext   = context;
        mLayoutInflater = LayoutInflater.from(context);
        mPostItems      = items;
    }

    // inflates the cell layout from xml when needed
    @Override
    public PostItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.postitem, parent, false);
        return new PostItemViewHolder(view);
    }

    // binds the data to the views in each cell
    @Override
    public void onBindViewHolder(PostItemViewHolder holder, int position) {
        try {
            // Todo: create little flags for the thumbnails that signify if it is an animated post or a webm, like e621 does
            JSONObject post = mPostItems.getJSONObject(position);

            holder.postFavorite.setText("♥" + post.getString("fav_count"));

            // set postScore attributes
            holder.postScore.setTextColor(getScoreColor(post.getInt("score")));
            holder.postScore.setText(getScoreText(post.getInt("score")));

            // set postRating attributes
            holder.postRating.setTextColor(getRatingColor(post.getString("rating")));
            holder.postRating.setText(post.getString("rating").toUpperCase());

            // Set thumbnail attributes
            // Scale values reported in the data to density-independent values
            int reportedThumbnailHeight = post.getInt("preview_width");
            int trueThumbnailHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reportedThumbnailHeight, parentContext.getResources().getDisplayMetrics());

            int reportedThumbnailWidth = post.getInt("preview_height");
            int trueThumbnailWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reportedThumbnailWidth, parentContext.getResources().getDisplayMetrics());

            LinearLayout.LayoutParams thumbnailParams = new LinearLayout.LayoutParams(trueThumbnailHeight, trueThumbnailWidth);
            thumbnailParams.gravity = Gravity.CENTER_HORIZONTAL;

            holder.postThumbnail.setLayoutParams(thumbnailParams);

            // set border
            holder.postThumbnail.setBackground(getThumbnailBorder(post));

            // Set as online image
            if (post.getString("file_ext").equals("swf")) {
                Glide.with(parentContext).load(post.getString("preview_url")).into(holder.postThumbnail);
            }
            else {
                // Todo: Make this use sample or full based on user preference
                Glide.with(parentContext).load(post.getString("preview_url")).into(holder.postThumbnail);
            }

            // Set click listener

        }
        catch (JSONException e) {
            Log.e("PostItemAdapter", "onBindViewHolder(): JSONException", e);
        }
    }

    private int getScoreColor(int score) {
        int pos = Color.GREEN;
        int neg = Color.RED;
        int nil = Color.WHITE;

        if (score > 0) {
            return pos;
        }
        else if (score < 0) {
            return neg;
        }
        else if (score == 0) {
            return nil;
        }

        return 0;
    }

    private String getScoreText(int score) {
        String pos = "↑";
        String neg = "↓";
        String nil = "↕";

        String finalScore = Integer.toString(score).replace("-", ""); // Remove negative sign for display purposes.

        if (score > 0) {
            return pos+finalScore;
        }
        else if (score < 0) {
            return neg+finalScore;
        }
        else if (score == 0) {
            return nil+finalScore;
        }

        return "Oopsie Doops";
    }

    private int getRatingColor(String text) {
        if (text.equals("e")) {
            return Color.RED;
        }
        else if (text.equals("q")) {
            return Color.YELLOW;
        }
        else if (text.equals("s")) {
            return Color.GREEN;
        }

        return Color.BLACK;
    }

    private Drawable getThumbnailBorder(JSONObject post) {
        try {
            if (Objects.equals(post.getString("status"), "flagged")) {
                return parentContext.getDrawable(R.drawable.rounded_rectangle_red);
            }
            else if (post.getBoolean("has_children")) {
                return parentContext.getDrawable(R.drawable.rounded_rectangle_green);
            }
            else if (!Objects.equals(post.getString("parent_id"), "null")) {
                return parentContext.getDrawable(R.drawable.rounded_rectangle_yellow);
            }
            else if (Objects.equals(post.getString("status"), "pending")) {
                return parentContext.getDrawable(R.drawable.rounded_rectangle_blue);
            }
            else if (Objects.equals(post.getString("status"), "active")) {
                return parentContext.getDrawable(R.drawable.rounded_rectangle_clear);
            }
        } catch (JSONException e) {
            Log.e("getThumbnailBorder()", e.toString());
        }

        return null;
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mPostItems.length();
    }

    public void setClickListener(PostItemClickListener fragment) {
        this.postItemClickListener = fragment;
    }

    public class PostItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RelativeLayout postLayout;
        ImageView      postThumbnail;
        TextView       postRating;
        TextView       postScore;
        TextView       postFavorite;

        PostItemViewHolder(View itemView) {
            super(itemView);

            postLayout    = itemView.findViewById(R.id.mainPostLayout);
            postThumbnail = itemView.findViewById(R.id.imageView);
            postRating    = itemView.findViewById(R.id.ptRating);
            postScore     = itemView.findViewById(R.id.ptScore);
            postFavorite  = itemView.findViewById(R.id.ptFavorite);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (postItemClickListener != null) {
                postItemClickListener.postItemClicked(view, getAdapterPosition());
            }
        }
    }

    public interface PostItemClickListener {
        void postItemClicked(View view, int position);
    }
}

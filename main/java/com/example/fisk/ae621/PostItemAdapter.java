package com.example.fisk.ae621;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by fisk on 2/17/18.
 */

public class PostItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM   = 1;
    public static final int TYPE_FOOTER = 2;

    private Context        parentContext;

    private JSONArray mPostItems;

    // data is passed into the constructor
    PostItemAdapter(Context context, JSONArray postItems, RecyclerView.LayoutManager layoutManager, final int numberOfColumns) {
        parentContext   = context;
        mPostItems  = postItems;

        // Set the span parameters for individual ViewHolder Types
        ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(getItemViewType(position)){
                    case TYPE_HEADER:
                        return numberOfColumns;
                    case TYPE_ITEM:
                        return 1;
                    case TYPE_FOOTER:
                        return numberOfColumns;
                    default:
                        return -1;
                }
            }
        });
    }

    // Interface based clickListener
    private PostItemClickListener postItemClickListener;

    public interface PostItemClickListener {
        void postItemClicked(View view, JSONObject postData);

        void pageDevDataViewClicked(String data);
    }

    public void setClickListener(PostItemClickListener fragment) {
        this.postItemClickListener = fragment;
    }

    // inflates the cell layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                View view_item = LayoutInflater.from(parent.getContext()).inflate(R.layout.postitem, parent, false);
                return new PostItemViewHolder(view_item);

            case TYPE_HEADER:
                View view_header = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_page_header, parent, false);
                return new HeaderViewHolder(view_header);

            case TYPE_FOOTER:

                View view_footer = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_page_footer, parent, false);
                return new FooterViewHolder(view_footer);

        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    // binds the data to the views in each cell
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Todo: create little flags for the thumbnails that signify if it is an animated post or a webm, like e621 does
        // Todo: Find a way to make the heart icon red, or at least white. Anything but black. (Is this color device dependent?, if so maybe replace with a drawable)
        // Todo: Remove Flash based posts from the postView
        if (holder instanceof HeaderViewHolder) {
            onBindHeader(holder, position);

        }else if (holder instanceof PostItemViewHolder) {
            onBindPostData(holder, position);

        }else if (holder instanceof FooterViewHolder) {
            onBindFooter(holder, position);
        }
    }

    private void onBindHeader(RecyclerView.ViewHolder holder, final int position) {

        ((HeaderViewHolder) holder).viewPageDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postItemClickListener.pageDevDataViewClicked(getPageStringAtIndex(position));

            }
        });

    }

    private void onBindFooter(RecyclerView.ViewHolder holder, int position) {
        // Todo: The header could also house a myriad of developer functions And when we have dev-mode turned off, we just stop injecting header data so they wont be rendered
        try {
            ((FooterViewHolder) holder).pageNumber.setText("Page "+mPostItems.getJSONObject(position).getInt("current_page"));
        } catch (JSONException e) {
            Log.e("FOOTER", "Failed to bind to footer: "+e.toString());
        }
    }

    private void onBindPostData(RecyclerView.ViewHolder holder, int position) {
        try {
            JSONObject post = mPostItems.getJSONObject(position);

            ((PostItemViewHolder) holder).postFavorite.setText("♥" + post.getString("fav_count"));

            // set postScore attributes
            ((PostItemViewHolder) holder).postScore.setTextColor(getScoreColor(post.getInt("score")));
            ((PostItemViewHolder) holder).postScore.setText(getScoreText(post.getInt("score")));

            // set postRating attributes
            ((PostItemViewHolder) holder).postRating.setTextColor(getRatingColor(post.getString("rating")));
            ((PostItemViewHolder) holder).postRating.setText(post.getString("rating").toUpperCase());

            // Set thumbnail attributes
            // Scale values reported in the data to density-independent values
            int reportedThumbnailHeight = post.getInt("preview_width");
            int trueThumbnailHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reportedThumbnailHeight, parentContext.getResources().getDisplayMetrics());

            int reportedThumbnailWidth = post.getInt("preview_height");
            int trueThumbnailWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reportedThumbnailWidth, parentContext.getResources().getDisplayMetrics());

            LinearLayout.LayoutParams thumbnailParams = new LinearLayout.LayoutParams(trueThumbnailHeight, trueThumbnailWidth);
            thumbnailParams.gravity = Gravity.CENTER_HORIZONTAL;

            ((PostItemViewHolder) holder).postThumbnail.setLayoutParams(thumbnailParams);

            // set border
            ((PostItemViewHolder) holder).postThumbnail.setBackground(getThumbnailBorder(post));

            // Set as online image
            if (post.getString("file_ext").equals("swf")) {
                Glide.with(parentContext).load(post.getString("preview_url")).into(((PostItemViewHolder) holder).postThumbnail);
            }
            else {
                // Todo: Make this use sample or preview based on user preference
                Glide.with(parentContext).load(post.getString("preview_url")).into(((PostItemViewHolder) holder).postThumbnail);
            }

        }
        catch (JSONException e) {
            Log.e("PostItemAdapter", "onBindViewHolder(): JSONException", e);
        }
    }

    // #### Items View Holders ####

    public class HeaderAccessory extends JSONObject {
        public HeaderAccessory() {
           // Just an idea for now, maybe it should have a factory and its own file to create accessory data
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        Button viewPageDataButton;

        public HeaderViewHolder(View headerView) {
            super(headerView);
            viewPageDataButton = headerView.findViewById(R.id.piViewPageData);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        TextView pageNumber;

        public FooterViewHolder(View footerView) {
            super(footerView);

            pageNumber = footerView.findViewById(R.id.footerPageNumber);
        }
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
                try {
                    postItemClickListener.postItemClicked(view, mPostItems.getJSONObject(getAdapterPosition()));
                } catch (JSONException e) {
                    Log.e("POSTCLICK", e.toString());
                }
            }
        }
    }

    // #### Data management ####
    public void setPostItems(JSONArray newData) {
        mPostItems = newData;
    }

    private String getPageStringAtIndex(int position) {
        JSONArray pageData = new JSONArray();
        try {
            for (int index = position+1; !mPostItems.getJSONObject(index).has("accessory_type"); index++) {
                pageData.put(mPostItems.getJSONObject(index));
            }
        } catch (JSONException e) {
            Log.e("Page String Builder", "JSONException: "+e.toString());
        }

        return pageData.toString();
    }

    // #### Post thumbnail detailing ####
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

    // #### Item Type Setters ####
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        else if (isPositionFooter(position)) {
            return TYPE_FOOTER;
        }

        return TYPE_ITEM;
    }

    // Determine the beginning of a page
    private boolean isPositionHeader(int position) {
        try {
            if (mPostItems.getJSONObject(position).has("accessory_type")) {
                return mPostItems.getJSONObject(position).getInt("accessory_type") == TYPE_HEADER;
            }
        } catch (JSONException e) {
            Log.e("Header", "PostItemAdapter.isPositionHeader(): " + e.toString());
        }

        return false;
    }

    // Determine the end of a page
    private boolean isPositionFooter(int position) {
        try {
            if (mPostItems.getJSONObject(position).has("accessory_type")) {
                return mPostItems.getJSONObject(position).getInt("accessory_type") == TYPE_FOOTER;
            }
        } catch (JSONException e) {
            Log.e("Footer", "PostItemAdapter.isPositionFooter(): " + e.toString());
        }

        return false;
    }
}

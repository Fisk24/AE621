package com.example.fisk.ae621;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;

/**
 * Created by fisk on 3/3/18.
 */

/*
DText Parsing might need its own library...
First Focus on getting comments to display with the raw text
This may give you some idea of what dtext to handle first
Parse Quotes and other heavy tags out of the comment body string and handle them separately
Parse the text into Spannable objects
Consider the following:
The comment body needs a vertical linear layout
Quotes are extracted and are placed into this layout
Preceding and following text are always placed respectively above or below
Spannable's have there place in painting the text color and bold/italics
The spannable objects DO NOT each need there own TextView
References to posts wherein the post thumbnail is displayed seem to require too much
overhead. Consider simply placing a link in there stead which views the post in question
*/

public class CommentBuilder {
    private JSONArray    commentData;
    private Context      targetContext;
    private LinearLayout targetLayout;

    public CommentBuilder(Context context, LinearLayout linearLayout, JSONArray data) {
        commentData   = data;
        targetContext = context;
        targetLayout  = linearLayout;
    }

    private void onBindViewHolder(CommentViewHolder holder) {
        // Bind onClick and view values here
        String url = "https://static1.e621.net/data/preview/76/bd/76bd584446d1ef685a0ae3bc8d9d28c5.jpg";

        ImageSpan testImage = new ImageSpan(targetContext, R.drawable.ic_launcher_background);

        SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
        spanBuilder.append("Derp, now look at this ");
        spanBuilder.append(" ", testImage, 0);
        spanBuilder.append(" But now there's text here again\n With a new line.............");
        holder.mCommentBody.setText(spanBuilder);
    }

    public void build() {
        LayoutInflater    inflater    = LayoutInflater.from(targetContext);
        View              commentView = inflater.inflate(R.layout.comment, null);
        CommentViewHolder viewHolder  = new CommentViewHolder(commentView);

        onBindViewHolder(viewHolder);
        targetLayout.addView(commentView);
    }

    private class CommentViewHolder {
        TextView mCommentBody;
        public CommentViewHolder(View view) {
            mCommentBody = view.findViewById(R.id.comBodyText);
        }
    }
}

package com.example.fisk.ae621;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.widget.LinearLayout;

import org.json.JSONArray;

public class CommentIndexActivity extends AppCompatActivity {

    JSONArray commentData;

    Toolbar toolBar;
    ActionBar actionBar;

    LinearLayout mCommentHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_index_layout);

        initializeActionBar();

        commentData = new JSONArray();

        mCommentHolder = findViewById(R.id.commentHolder);

        buildComments();
    }

    private void initializeActionBar() {
        // ActionBar
        toolBar = findViewById(R.id.commentToolbar);
        setSupportActionBar(toolBar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void buildComments() {
        CommentBuilder builder = new CommentBuilder(this, mCommentHolder, commentData);
        builder.build();
    }
}

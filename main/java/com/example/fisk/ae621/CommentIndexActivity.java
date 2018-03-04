package com.example.fisk.ae621;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class CommentIndexActivity extends AppCompatActivity {

    Toolbar toolBar;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_index_layout);

        initializeActionBar();
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
}

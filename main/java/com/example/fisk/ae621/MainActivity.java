package com.example.fisk.ae621;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements PostItemAdapter.PostItemClickListener {

    private String queryTags = "";
    private JSONArray postItemsData;

    private DrawerLayout   mDrawerLayout;
    private NavigationView mNavigationView;

    private android.support.v7.widget.Toolbar toolBar;
    private ActionBar actionBar;

    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView       mRecyclerView;

    // Fragments

    FragmentManager fragmentManger = getFragmentManager();

    // The post viewer is created from scratch every time it's needed

    // #### Essential Activity Overrides ####

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeActionBar();
        initializeNavigationView();

        initializeSearchParameters();

        initializeRecyclerView();

        fetchPosts();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (postItemsData!=null) {
            outState.putString("postData", postItemsData.toString());
            outState.putParcelable("post_adapter_state", mRecyclerView.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // This runs after onCreate, and after the adapter is initialized consequently
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            try {
                postItemsData = new JSONArray(savedInstanceState.getString("postData", ""));
                mRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("post_adapter_state"));
            }
            catch (JSONException e) {
                Log.e("JSONException", "MainActivity.onCreate():"+e.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // #### Widget Initialization ####

    private void initializeActionBar() {
        // ActionBar
        toolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    private void initializeNavigationView() {
        // Navigation Drawer
        mDrawerLayout   = (DrawerLayout   ) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView ) findViewById(R.id.nav_view);
        mNavigationView.getMenu().findItem(R.id.nav_posts).setChecked(true); // Set the posts button as checked by default, because this is the post view.
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // set item as selected to persist highlight
                item.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                FragmentTransaction transaction = fragmentManger.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.nav_login:
                        //transaction.replace(R.id.pageView, fLogin);
                        break;
                    case R.id.nav_posts:
                        break;
                    case R.id.nav_comments:
                        //transaction.replace(R.id.pageView, fCommentsIndex);
                        break;
                    case R.id.nav_pools:
                        //transaction.replace(R.id.pageView, fPoolsIndex);
                        break;
                    case R.id.nav_dev_json:
                        //Todo: This is the next logical target for activity conversion. Fix this ASAP!
                        //transaction.replace(R.id.pageView, fDevJson);
                        break;
                }

                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            }
        });
    }

    private void initializeRecyclerView() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width  = (int) (displayMetrics.widthPixels / displayMetrics.density);
        int maxThumbnailWidth = 175;

        int numberOfColumns = width / maxThumbnailWidth;

        mRecyclerView = (RecyclerView      ) findViewById(R.id.recyclerView         );
        mSwipeLayout  = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout   );

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        VerticalSpaceItemDecoration verticalDecoration = new VerticalSpaceItemDecoration(100);
        mRecyclerView.addItemDecoration(verticalDecoration);
        mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {

            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute();
            }
        });

    }

    // #### Search methods ####

    private void initializeSearchParameters() {
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearchQuery(query);
        }
    }

    private void doSearchQuery(String query) {
        queryTags = query;
    }

    // #### Recycler and Adapter methods ####

    @Override
    public void postItemClicked(View view, int position) {
        try {
            // Pass the data for the selected post to the navigation method
            JSONObject postItem   = getPostItemsData().getJSONObject(position);
            gotoPostView(postItem);

        } catch (JSONException e) {
            Log.e("postItemClicked()", e.toString());
        }
    }

    private void initRecyclerAdapter(JSONArray data) {
        // Fill Recycler and make
        PostItemAdapter adapter = new PostItemAdapter(this, data);
        adapter.setClickListener(this);
        mRecyclerView.setAdapter(adapter);
    }

    public void gotoPostView(JSONObject post) {
        // Set the data for the selected post, and start the new activity
        Intent intent = new Intent(MainActivity.this, PostViewActivity.class);
        intent.putExtra("post_data", post.toString());
        startActivity(intent);
    }

    // #### Post data Manipulators ####

    public JSONArray getPostItemsData() {
        return postItemsData;
    }

    public void setPostItemsData(JSONArray postItemsData) {
        this.postItemsData = postItemsData;
    }

    // #### Fetch Feed Methods ####
    private void fetchPosts() {
        if (postItemsData == null) {
            Log.e("Oops", "post data was null");
            new FetchFeedTask().execute();
        }
        else {
            initRecyclerAdapter(postItemsData);
        }
    }

    // Performs api delegations in background
    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiDelegate delegate = new ApiDelegate();
            JSONArray   response;

            try {

                if (!queryTags.equals("")) {
                    response = delegate.performTaggedPostIndexSearchQuery(queryTags, 1);
                } else {
                    response = delegate.performBasicPostIndexQuery(1);
                }

                setPostItemsData(response);

                return true;
            }
            catch (JSONException e) {
                Log.e("JSONException", "FetchFeedTask: "+e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            mSwipeLayout.setRefreshing(false);

            if (success) {
                initRecyclerAdapter(getPostItemsData());
            }
            else {
                Toast.makeText(MainActivity.this, "Failed to refresh!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

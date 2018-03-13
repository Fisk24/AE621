package com.example.fisk.ae621;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PostItemAdapter.PostItemClickListener {

    //private ArrayList<JSONArray> postItemsData = new ArrayList<>();
    private JSONArray postItemsData;

    private String    queryTags = "";
    private boolean   isLoading = false;
    private int       currentPage = 1;

    private SharedPreferences sharedPref;

    private DrawerLayout   mDrawerLayout;
    private NavigationView mNavigationView;

    private android.support.v7.widget.Toolbar toolBar;
    private ActionBar actionBar;

    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView       mRecyclerView;

    // #### Essential Activity Overrides ####

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializeSharedPreferences();
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current_page");
            restorePostData();
        }

        initializeActionBar();
        initializeNavigationView();

        initializeSearchParameters();

        initializeRecyclerView(savedInstanceState);

        fetchPosts();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (postItemsData!=null) {
            // TODO: The postItemsData is simply too large to be saved via this method, consider investigating shared preferences
            // TODO: A Possible alternative would be the application class, however I have been warned that this may cause serious memory issues
            outState.putParcelable("post_layout_manager_state", mRecyclerView.getLayoutManager().onSaveInstanceState());
            outState.putInt("current_page", currentPage);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("post_data", postItemsData.toString());
            editor.apply();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // This runs after onCreate, and after the adapter is initialized consequently
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("post_layout_manager_state"));
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

    private void restorePostData() {
        try {
            String postDataString = sharedPref.getString("post_data", null);
            Log.e("POSTDATA", "Post items data is being restored.");
            if (postDataString != null) {
                postItemsData = new JSONArray(postDataString);
            }
        } catch (JSONException e) {
            Log.e("ActivitySharedPrefs", "post_data: "+e.toString());
        }
    }

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

                switch (item.getItemId()) {
                    case R.id.nav_login:
                        //transaction.replace(R.id.pageView, fLogin);
                        break;
                    case R.id.nav_posts:
                        break;
                    case R.id.nav_comments:
                        gotoCommentIndex();
                        break;
                    case R.id.nav_pools:
                        //transaction.replace(R.id.pageView, fPoolsIndex);
                        break;
                }

                return true;
            }
        });
    }

    private void initializeRecyclerView(Bundle savedInstanceState) {

        mRecyclerView = (RecyclerView      ) findViewById(R.id.recyclerView         );
        mSwipeLayout  = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout   );

        GridLayoutManager layoutManager = new GridLayoutManager(this, getNumberOfColumns());
        mRecyclerView.setLayoutManager(layoutManager);

        VerticalSpaceItemDecoration verticalDecoration = new VerticalSpaceItemDecoration(100);
        mRecyclerView.addItemDecoration(verticalDecoration);

        mRecyclerView.setOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                currentPage += 1; //Increment page index to load the next one
                new FetchFeedTask().execute();
                Log.e("SCROLL", "Tried to load more things");
            }

            @Override
            public boolean isLastPage() {
                return false;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute();
            }
        });

    }

    private void initRecyclerAdapter(JSONArray data) {
        // Fill Recycler and make
        if (mRecyclerView.getAdapter() == null) {
            PostItemAdapter adapter = new PostItemAdapter(this, data, mRecyclerView.getLayoutManager(), getNumberOfColumns());
            adapter.setClickListener(this);
            mRecyclerView.setAdapter(adapter);
        }
        else {
            PostItemAdapter adapter = (PostItemAdapter) mRecyclerView.getAdapter();
            adapter.setPostItems(data);
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
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

    // #### Navigation Methods ####

    private void gotoDataModelViewer(String data) {
        // Set the data for the selected post, and start the new activity
        Intent intent = new Intent(MainActivity.this, DevDataModelViewerActivity.class);

        intent.putExtra("data", data);
        startActivity(intent);
    }

    private void gotoCommentIndex() {
        Intent intent = new Intent(MainActivity.this, CommentIndexActivity.class);
        //intent.putExtra("data", postItemsData.toString());
        startActivity(intent);
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

    @Override
    public void pageDevDataViewClicked(String data) {
        gotoDataModelViewer(data);
    }

    private int getNumberOfColumns() {

        // Determine the number of columns that can fit on the screen, given the actual width of the screen.

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width  = (int) (displayMetrics.widthPixels / displayMetrics.density); // Display width, in density independent pixels.
        int maxThumbnailWidth = 175;

        return width / maxThumbnailWidth;
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

    public void appendPostItemsData(JSONArray newData) {
        try {
            if (postItemsData == null) {
                postItemsData = new JSONArray();
            }
            // Insert Header Data Accessory
            postItemsData.put(new JSONObject("{accessory_type: "+PostItemAdapter.TYPE_HEADER+", current_page: "+(currentPage+1)+"}"));
            for (int i = 0; i < newData.length(); i++) {
                JSONObject jsonObject = newData.getJSONObject(i);
                postItemsData.put(jsonObject);
            }
            postItemsData.put(new JSONObject("{accessory_type: "+PostItemAdapter.TYPE_FOOTER+", current_page: "+(currentPage+1)+"}")); // Insert Footer Token
        }
        catch (JSONException e) {
            Log.e("JSONException", "appendPostData(): "+e.toString());
        }
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
            isLoading = true;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiDelegate delegate = new ApiDelegate();
            JSONArray   response;

            try {

                if (!queryTags.equals("")) {
                    response = delegate.performTaggedPostIndexSearchQuery(queryTags, currentPage);
                } else {
                    response = delegate.performBasicPostIndexQuery(currentPage);
                }

                appendPostItemsData(response);

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
            isLoading = false;

            if (success) {
                initRecyclerAdapter(getPostItemsData());
            }
            else {
                Toast.makeText(MainActivity.this, "Failed to refresh!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

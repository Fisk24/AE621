package com.example.fisk.ae621;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity implements ApiDelegate.ApiCallback {

    private ApiDelegate apiDelegate;

    private TextView mLdMainStatus;

    // #### Essential Activity Overrides ####

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLdMainStatus = findViewById(R.id.ld_main_status);

        /*
        //initializeSharedPreferences();
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current_page");
            //restorePostData();
        }
        */

        // DONT recreate the fragment if it already exists (useful for configuration changes)

        FragmentManager fm = getSupportFragmentManager();
        apiDelegate = (ApiDelegate) fm.findFragmentByTag(ApiDelegate.TAG);

        if (apiDelegate == null) {
            apiDelegate = ApiDelegate.getInstance(fm);
            apiDelegate.setApiCallback(this);
            apiDelegate.performQueryById(ApiDelegate.POLL_REQUEST);

            mLdMainStatus.setText("Trying to reach e621.net");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        if (postItemsData!=null) {
            //outState.putParcelable("post_layout_manager_state", mRecyclerView.getLayoutManager().onSaveInstanceState());
            outState.putInt("current_page", currentPage);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("post_data", postItemsData.toString());
            editor.commit();
        }
        */
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        // Generic internet connectivity test
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        return netInfo;
    }

    public void gotoPostActivity() {
        Intent intent = new Intent(this, PostActivity.class);
        startActivity(intent);
    }

    @Override
    public void onApiResponse(ApiDelegate.ApiResponse apiResponse) {
        Log.e("DEBUG", "onApiResponse: "+apiResponse.wasSuccess());
        if (apiResponse.wasSuccess()) {
            //mLdMainStatus.setText("Success!");
            gotoPostActivity();
        } else {
            //Log.e("DEBUG", "No really it was!");
            mLdMainStatus.setText("Check connection...");
        }
    }

    /*

     //private ArrayList<JSONArray> postItemsData = new ArrayList<>();
    private JSONArray  postItemsData;
    private JSONObject dynamoItemsData;

    private String    queryTags = "";
    private boolean   isLoading = false;
    private int       currentPage = 1;

    private SharedPreferences sharedPref;

    private DrawerLayout   mDrawerLayout;
    private NavigationView mNavigationView;

    private android.support.v7.widget.Toolbar toolBar;
    private ActionBar actionBar;

        // #### Widget Initialization ####

    private void initializeDynamoData() {
        try {
            dynamoItemsData = new JSONObject("" +
                    "{" +
                    "\"created_at\":{\"json_class\":\"Time\",\"s\":1520658590,\"n\":714099000}," +
                    "\"description\":\"You should not be seeing this...\"," +
                    "\"id\":00000," +
                    "\"is_active\":true," +
                    "\"is_locked\":false," +
                    "\"name\":\"Dynamic_Pool\"," +
                    "\"post_count\":0," +
                    "\"updated_at\":{\"json_class\":\"Time\",\"s\":1520993880,\"n\":93137000}," +
                    "\"user_id\":00000," +
                    "\"posts\":[]"+
                    "}"
            );
        } catch (JSONException e) {
            Log.e("DYNAMO", "Failed to initialize: "+e.toString());
        }
    }

    private void initializeActionBar() {
        // ActionBar
        toolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.poolViewToolbar);
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
                        gotoPosts();
                        break;
                    case R.id.nav_dynamo:
                        gotoDynamicPool();
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

    // #### Navigation Methods ####

    private void gotoDataModelViewer(String data) {
        // Set the data for the selected post, and start the new activity
        Intent intent = new Intent(MainActivity.this, DevDataModelViewerActivity.class);

        intent.putExtra("data", data);
        startActivity(intent);
    }

    private void gotoPosts() {
        Intent intent = new Intent(MainActivity.this, PostActivity.class);
        //intent.putExtra("pool_data", dynamoItemsData.toString());
        startActivity(intent);
    }

    private void gotoDynamicPool() {
        Intent intent = new Intent(MainActivity.this, PoolActivity.class);
        intent.putExtra("pool_data", dynamoItemsData.toString());
        startActivity(intent);
    }

    private void gotoCommentIndex() {
        Intent intent = new Intent(MainActivity.this, CommentIndexActivity.class);
        //intent.putExtra("data", postItemsData.toString());
        startActivity(intent);
    }

    public void gotoPostView(JSONObject post) {
        // Set the data for the selected post, and start the new activity
        Intent intent = new Intent(MainActivity.this, PostViewActivity.class);
        intent.putExtra("post_data", post.toString());
        startActivity(intent);
    }
     */

    /*
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
    */
}

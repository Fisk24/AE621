package com.example.fisk.ae621;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

public class PostIndexFragment extends Fragment {

    //TODO: When network operations fail this fragment should changed its layout file to the error layout

    private JSONArray postItemsData;

    private String    queryTags   = "";
    private boolean   isLoading   = false;
    private int       currentPage = 1;

    private SharedPreferences sharedPref;

    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;

    public PostIndexFragment() {
        // Required empty public constructor
    }

    public static PostIndexFragment newInstance() {
        PostIndexFragment fragment = new PostIndexFragment();
        Bundle args = new Bundle();
        // TODO: BUNDLE ARGUMENTS HERE
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: PROCESS BUNDLE ARGS HERE
        }

        initializeSearchParameters();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.post_index_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current_page");
        }
        initializeRecyclerView(savedInstanceState);
        restorePostData();

        // TODO: Possible setting: Toggle on and off this persistence behavior
        // Currently the postItemsData from the last time the app was closed continues to persist
        // Possible feature?
        // Easier fix, proc a refresh operation if savedInstanceState == null
        if (postItemsData == null || savedInstanceState == null) {
            refreshPostData();
        }
        else {
            initializeRecyclerAdapter(postItemsData);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (postItemsData!=null) {
            //outState.putParcelable("post_layout_manager_state", mRecyclerView.getLayoutManager().onSaveInstanceState());
            outState.putInt("current_page", currentPage);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("post_data", postItemsData.toString());
            editor.apply();
        }

    }

    private int getNumberOfColumns() {

        // Determine the number of columns that can fit on the screen, given the actual width of the screen.

        if (getActivity() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int width  = (int) (displayMetrics.widthPixels / displayMetrics.density); // Display width, in density independent pixels.
            int maxThumbnailWidth = 175;

            return width / maxThumbnailWidth;
        }

        return 1;
    }

    // #### Fetch Feed Methods ####
    private void refreshPostData() {
        /*
        * Attempt to re-obtain the post data
        * This should overwrite all the current post data
        * and reset the page number to one
        */
        new RefreshTask().execute();
    }

    // #### Post Items Methods ####

    private void restorePostData() {
        try {
            String postDataString = sharedPref.getString("post_data", null);
            Log.e("POSTDATA", "Post items data is being restored.");
            if (postDataString != null) {
                postItemsData = new JSONArray(postDataString);
            }
            Log.e("DEBUG", "postDataString -> "+postDataString);
            Log.e("DEBUG", "postItemsData -> "+postItemsData);
        } catch (JSONException e) {
            Log.e("ActivitySharedPrefs", "post_data: "+e.toString());
        }
    }

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

    // #### Search methods ####

    private void initializeSearchParameters() {
        Intent intent = getActivity().getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearchQuery(query);
        }
    }

    private void doSearchQuery(String query) {
        queryTags = query;
    }

    // #### Recycler ####

    private void initializeRecyclerView(Bundle savedInstanceState) {

        mRecyclerView = (RecyclerView      ) getActivity().findViewById(R.id.recyclerView         );
        mSwipeLayout  = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshLayout   );

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), getNumberOfColumns());
        mRecyclerView.setLayoutManager(layoutManager);

        VerticalSpaceItemDecoration verticalDecoration = new VerticalSpaceItemDecoration(100);
        mRecyclerView.addItemDecoration(verticalDecoration);

        mRecyclerView.setOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                new NewPageTask().execute();
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
                refreshPostData();
            }
        });

    }

    private void initializeRecyclerAdapter(JSONArray data) {
        // Fill Recycler and make
        if (mRecyclerView.getAdapter() == null) {
            PostItemAdapter adapter = new PostItemAdapter(getContext(), data, mRecyclerView.getLayoutManager(), getNumberOfColumns());
            adapter.setClickListener((PostActivity)getActivity()); // set the main activity as the click listener for the adapter this needs to be PostActivity later
            mRecyclerView.setAdapter(adapter);
        }
        else {
            PostItemAdapter adapter = (PostItemAdapter) mRecyclerView.getAdapter();
            adapter.setPostItems(data);
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    }

    private class RefreshTask extends AsyncTask<Void, Void, Boolean> {

        /*
        * Attempt to re-obtain the post data
        * This should overwrite all the current post data
        * and reset the page number to one
        */

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            isLoading = true;
            currentPage = 1;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiDelegate delegate = new ApiDelegate();
            JSONArray   response;

            try {
                postItemsData = null;
                appendPostItemsData(delegate.performBasicPostIndexQuery(1));
                return true;
            }
            catch (JSONException e) {
                Log.e("JSONException", "FetchFeedTask: "+e.toString());
            } catch (SSLHandshakeException e) {
                //TODO: Implement SSL Error screen
            } catch (UnknownHostException e) {
                //TODO: Implement Failed to Refresh Screen
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            mSwipeLayout.setRefreshing(false);
            isLoading = false;

            if (success) {
                initializeRecyclerAdapter(postItemsData);
            }
            else {
                Toast.makeText(getContext(), "Failed to refresh!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class NewPageTask extends AsyncTask<Void, Void, Boolean> {

        /*
        * Attempt to fetch the metadata for the next page, then add it to the existing data
        */

        @Override
        protected void onPreExecute() {
            isLoading = true;
            currentPage += 1; //Increment page index to load the next one
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ApiDelegate delegate = new ApiDelegate();
            JSONArray   response;

            try {
                appendPostItemsData(delegate.performBasicPostIndexQuery(currentPage));
                return true;
            }
            catch (JSONException e) {
                Log.e("JSONException", "FetchFeedTask: "+e.toString());
            } catch (SSLHandshakeException e) {
                //TODO: Handle with a footer swipe up or tap the footer to retry
            } catch (UnknownHostException e) {
                //TODO: Handle with a footer swipe up or tap the footer to retry
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            isLoading = false;

            if (success) {
                initializeRecyclerAdapter(postItemsData);
            }
            else {
                Toast.makeText(getContext(), "Failed to refresh!", Toast.LENGTH_LONG).show();
            }
        }
    }

}

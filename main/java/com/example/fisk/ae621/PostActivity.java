package com.example.fisk.ae621;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity implements PostItemAdapter.PostItemClickListener{

    //TODO: On phones landscape mode makes the viewport far to short. Combine the actionbar and the tab bar in landscape
    //TODO: TabLayout resets on orientation change. Stop that shit from happening!
    //TODO: The post index restarts from the beginning every time the user navigates more then a few screens away. Stop that shit from happening!
    //TODO: Searches should be in their own tab. Likewise, group up the index tabs.

    //Todo: Consider the possibility of the ui being focused on tab based browsing
    /*
     * When a post is clicked the tab interface will animate into view.
     * The user will then see that a new tab is opened.
     * By swiping left or tapping one of the tabs, the user may begin browsing there selections
     *
     * While the user is viewing a post the navigation button should be replaced with a back arrow
     * If the back arrow, or physical back button is pressed, the view should return to the post index,
     * and if no new posts are added swiping left on the post index again should return the view to
     * the last post viewed.
     *
     * This could easily be accomplished by borrowing a lot of the code from the dynamic-pool,
     * which is totally outmoded by this new ui design anyway.
     *
     * This scheme consolidates the post viewing experience in to one activity. Other site features
     * like pools, comments, and the forum should be set in there own activities as well
     * */

    private TabLayout mTabLayout;
    private TabItem   mPostIndexTab;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager            mViewPager;

    private JSONArray dynaPoolData;
    private String    queryTags = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity_layout);

        try {
            dynaPoolData = new JSONArray("[{indexGoesHere: 0}]");
        } catch (JSONException e) {
            Log.e("DYNAMO", e.toString());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.postTabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Give the index tab its text
        mTabLayout.getTabAt(0).setText("/"+queryTags+" - e621");
    }

    @Override
    public void onBackPressed() {
        int pagerCurrent = mViewPager.getCurrentItem();
        if (pagerCurrent != 0) {
            mViewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isInternetConnected() {
        // Generic internet connectivity test
        // The user may want to specify that the app should not be used with mobile data
        //TODO: Research ACTION_MANAGE_NETWORK_USAGE, This could be the functionality that finally requires a settings activity
        // When the devices connectivity status changes the app should respond accordingly

        //TODO: Connectivity status is still poorly telegraphed to the end user.
        // If the connection is lost and the user does not request additional data. Do Nothing.
        // Simply allow the user to continue viewing the data at their leisure until more data is requested.

        // If the connection is lost and the user refreshes they should be presented with a screen
        // informing them that their connection to the internet has been lost
        // The user should then be given the opportunity to retry the connection
        // this will cause the index to reload (Restart the activity? Maintain the posts that the user had opened in that case.)

        // If the connection is lost and the user requests a new page
        // Replace the post grid that would represent that page with an info box informing the user of their lost connection
        // The user may elect to tap that box to attempt to try again for that page

        // If the connection is fine but e621 can not be reached via a refresh operation
        // The user should be presented with a screen informing them of the error that is causing
        // the interruption if such information is obtainable. Eg: 404 Page not found. 500 Bad Gateway
        // Custom icons should be used in these cases

        ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnected());
    }

    public String formatPostTitle(JSONObject data) {
        try {
            //TODO: Shorten the post title if it would exceed 20 characters. Could be dependent on screen size.
            return String.format("#%1s: %2s", data.getString("id"), formatArtistData(data.getJSONArray("artist")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "Oops";
    }

    public String formatArtistData(JSONArray artistData) throws JSONException {

        String finalString = "";
        for (int i=0; i<artistData.length(); i++) {
            finalString += artistData.getString(i)+" ";
        }

        return finalString;
    }

    public void addTab(String title) {

        // Create a new tab and connect the title and close button

        final TabLayout.Tab newTab = mTabLayout.newTab();

        newTab.setCustomView(R.layout.closable_tab_layout);

        ((TextView) newTab.getCustomView().findViewById(R.id.tab_title)).setText(title);

        newTab.getCustomView().findViewById(R.id.tab_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.e("TAB", "clicked "+newTab.getPosition());
                mSectionsPagerAdapter.removePage(newTab.getPosition());
                removeTab(newTab);
            }
        });
        mTabLayout.addTab(newTab, 1);
    }

    public void removeTab(TabLayout.Tab tab) {
        mTabLayout.removeTab(tab);
    }

    public JSONArray insertData(JSONArray oldRef, int index, JSONObject newData) {
        JSONArray holderArray = new JSONArray();

        if (oldRef.length() <= index) {
            try {
                // In the case that the list is not long enough to accommodate a normal insertion
                // Try just putting it instead
                oldRef.put(index, newData);
                return oldRef;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < (oldRef.length()); i++) {
            try {
                if (i == index) {
                    // Put in the new data before any more old data is reinserted
                    holderArray.put(newData);
                }
                // Put in the old data
                holderArray.put(oldRef.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return holderArray;
    }

    @Override
    public void postItemClicked(View view, JSONObject postData) {
        mSectionsPagerAdapter.addPage(postData);
        addTab(formatPostTitle(postData));
    }

    @Override
    public void pageDevDataViewClicked(String data) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.post_view_layout, container, false);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addPage(JSONObject data) {
            // add the page for the user selected post
            // new pages are always added just after the post index
            // that is to say index 1

            dynaPoolData = insertData(dynaPoolData, 1, data);
            notifyDataSetChanged();
        }

        public void removePage(int index) {
            dynaPoolData.remove(index);
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof PostViewFragment) {
                // Because the post are inserted at the 1st index as opposed to the very end
                // every item after the new post must have its data shifted by one
                return POSITION_NONE;
            } else {
                // if the object is not a postViewFragment then it must be the IndexFragment
                // The index fragment should never change its position
                return POSITION_UNCHANGED;
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //TODO: Can we keep a copy of this object in the parent class? If so we might not be forced to reload it every time.
                    return PostIndexFragment.newInstance();
                default:
                    try {
                        return PostViewFragment.newInstance(dynaPoolData.getString(position));
                    } catch (JSONException e) {
                        Log.e("PAGER", e.toString());
                    }
            }

            return null;
        }

        @Override
        public int getCount() {
            return dynaPoolData.length();
        }
    }

    /*
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            // add the post index to begin with
            fragmentList = new ArrayList<>();
            fragmentList.add(0, (Fragment) PostIndexFragment.newInstance());
        }

        public void addPage(String data) {
            // add the page for the user selected post
            // new pages are always added just after the post index
            // that is to say index 1
            Fragment newPage = PostViewFragment.newInstance(data);
            fragmentList.add(1, (Fragment) newPage);
            notifyDataSetChanged();
        }

        public void removeView(int index) {
            fragmentList.remove(index);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return an instance of the fragment that represents the page the user is on.

            return fragmentList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            if (fragmentList.contains((Fragment) object)) {
                return fragmentList.indexOf((Fragment) object);
            } else {
                return POSITION_NONE;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            // Show as many pages as i need to display the entire dynamic pool
            return fragmentList.size();
        }
    }
    */
}

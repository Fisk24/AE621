package com.example.fisk.ae621;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PoolActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private PoolItemsPagerAdapter mPoolItemsPagerAdapter;

    private Toolbar   mToolBar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private JSONObject poolData = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pool_viewer_layout);

        mToolBar = (Toolbar) findViewById(R.id.poolViewToolbar);
        setSupportActionBar(mToolBar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mViewPager = (ViewPager) findViewById(R.id.poolViewContainer);
        mTabLayout = (TabLayout) findViewById(R.id.poolViewTabs);

        if (getIntent() != null) {
            try {
                poolData = new JSONObject(getIntent().getStringExtra("pool_data"));
                mToolBar.setTitle(poolData.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            if (poolData.getJSONArray("posts").length() == 0) {

                mTabLayout.setVisibility(View.GONE);
                mViewPager.setAdapter(new ContentMissingPagerAdapter(getSupportFragmentManager()));

            } else {

                // Make sure the tabView is visible even if it wasn't previously
                mTabLayout.setVisibility(View.VISIBLE);
                // Set up the ViewPager with the sections adapter.
                mPoolItemsPagerAdapter = new PoolItemsPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mPoolItemsPagerAdapter);

                mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
                mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

            }
        } catch (JSONException e) {
            Log.e("PoolActivity", "onCreate(): "+e.toString());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pool_view, menu);
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

    private class PoolItemsPagerAdapter extends FragmentPagerAdapter {

        public PoolItemsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                return PostViewFragment.newInstance(poolData.getJSONArray("posts").getJSONObject(position).toString());
            } catch (JSONException e) {
                Log.e("POOLVIEW", "getItem(): "+e.toString());
            }

            return null;
        }

        @Override
        public int getCount() {
            try {
                return poolData.getJSONArray("posts").length();
            } catch (JSONException e) {
                Log.e("POOLVIEW", "getItem(): "+e.toString());
            }
            return 0;
        }
    }

    private class ContentMissingPagerAdapter extends FragmentStatePagerAdapter {
        public ContentMissingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ContentMissingFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

}





/***
/**
     * A placeholder fragment containing a simple view.

public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.

    private static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.

    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_pool, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        return rootView;

    }
}

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}
 ***/




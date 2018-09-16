package com.example.fisk.ae621;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by fisk on 2/23/18.
 */

public class DevDataModelExpandableListAdapter extends BaseExpandableListAdapter {

    private JSONArray mDataModel;
    private Context   mContext;

    public DevDataModelExpandableListAdapter(Context context, JSONArray dataModel) {

        mContext   = context;
        mDataModel = dataModel;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {

        String typeOfIndex;
        String headerTitle;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.data_viewer_header, null);
        }

        try {
            // Get class name
            Class cls = mDataModel.get(groupPosition).getClass();
            typeOfIndex = cls.getName();

            // Construct header string
            headerTitle = typeOfIndex + " @ Index " + groupPosition + ":";

            // Set header text
            TextView mHeaderTextView = view.findViewById(R.id.dvHeaderIndex);
            mHeaderTextView.setText(headerTitle);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.data_viewer_child, null);
        }

        try {
            String childKey   = (String) mDataModel.getJSONObject(groupPosition).names().get(childPosition);
            String childValue = (String) mDataModel.getJSONObject(groupPosition).get(childKey).toString();

            TextView mChildKey = view.findViewById(R.id.dvChildKey);
            TextView mChildValue = view.findViewById(R.id.dvChildValue);

            // Determine longest key string and size label appropriately
            mChildKey.setEms(8);

            mChildKey  .setText(childKey);
            mChildValue.setText(childValue);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

        return view;
    }

    @Override
    public int getGroupCount() {
        return mDataModel.length();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            return mDataModel.getJSONObject(groupPosition).length();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
            return groupPosition;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        try {
            return mDataModel.getJSONObject(groupPosition).names().get(childPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int i, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private int getLongestStringLengthAtIndex(int groupPosition) {
        int longest = 1; // The shortest possible key length is probably 1
        try {
            for (int i = 0; i < mDataModel.getJSONObject(groupPosition).names().length(); i++) {
                String key       = mDataModel.getJSONObject(groupPosition).names().getString(i);
                int    keyLength = key.length();
                if ( keyLength > longest ) {
                    longest = keyLength;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return longest;
    }

    private String prettyPrintKey(String key, int groupPosition) {
        int minLength  = getLongestStringLengthAtIndex(groupPosition);
        int keyLength  = key.length();
        int difference = minLength - keyLength;

        Log.e("Difference", ""+difference);

        String prettyKey = key;
        String padding   = "";

        if (keyLength < minLength) {
            for (int i = 0; i > difference; i++) {
                padding += " ";
            }

            prettyKey = key + padding;
        }

        return prettyKey;
    }
}

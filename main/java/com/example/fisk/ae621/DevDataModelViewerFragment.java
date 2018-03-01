package com.example.fisk.ae621;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by fisk on 2/22/18.
 */

public class DevDataModelViewerFragment extends Fragment {

    EditText mEditText;
    ExpandableListView mDataView;

    DevDataModelExpandableListAdapter adapter;

    JSONArray mDataModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dev_data_model_viewer_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDataModel = ((MainActivity)getActivity()).getPostItemsData();

        mEditText = getActivity().findViewById(R.id.editText);
        mDataView = getActivity().findViewById(R.id.dataView);

        adapter = new DevDataModelExpandableListAdapter(getContext(), mDataModel);
        mDataView.setAdapter(adapter);

        mDataView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {

                try {
                    JSONObject subData = mDataModel.getJSONObject(groupPosition);

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("key",   subData.names().getString(childPosition));
                    dataBundle.putString("type",  subData.get(subData.names().getString(childPosition)).getClass().getName());
                    dataBundle.putString("value", ""+subData.get(subData.names().getString(childPosition)));

                    DialogFragment showData = new DevDataViewerDialogFragment();
                    showData.setArguments(dataBundle);
                    showData.show(getFragmentManager(), "Data Dialog");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;

            }
        });
    }

}

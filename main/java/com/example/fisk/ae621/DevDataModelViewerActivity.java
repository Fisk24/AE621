package com.example.fisk.ae621;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DevDataModelViewerActivity extends AppCompatActivity {

    EditText mEditText;
    ExpandableListView mDataView;

    DevDataModelExpandableListAdapter adapter;

    JSONArray mDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_data_model_viewer_layout);

        try {
            mDataModel = new JSONArray(getIntent().getStringExtra("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mEditText = findViewById(R.id.editText);
        mDataView = findViewById(R.id.dataView);

        adapter = new DevDataModelExpandableListAdapter(this, mDataModel);
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

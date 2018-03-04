package com.example.fisk.ae621;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by fisk on 2/24/18.
 */

public class DevDataViewerDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout inflater and create View
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dev_data_viewer_dialog, null);

        // Get arguments
        Bundle arguments = getArguments();

        // Declare widgets
        TextView mDataPointKey    = view.findViewById(R.id.dvDialogDataPointKey   );
        TextView mDataPointType   = view.findViewById(R.id.dvDialogDataPointType  );
        //TextView mDataPointSizeOf = view.findViewById(R.id.dvDialogDataPointSizeOf);
        TextView mDataPointValue  = view.findViewById(R.id.dvDialogDataPointValue );

        // Set widgets
        mDataPointKey  .setText("Data Point: "+arguments.getString("key"));
        mDataPointType .setText(arguments.getString("type" ));
        mDataPointValue.setText(arguments.getString("value"));

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going inside the dialog layout
        builder.setView(view)
                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                    }
                });

        return builder.create();
    }
}

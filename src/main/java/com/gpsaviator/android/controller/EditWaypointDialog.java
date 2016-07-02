package com.gpsaviator.android.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.gpsaviator.android.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by khaines on 01/11/14.
 */
public class EditWaypointDialog {

    public interface EditWaypointDialogOkClick {
        public void okClicked(String editText);
    }

    public EditWaypointDialog(Context context, List<String> names, String name, final EditWaypointDialogOkClick ok) {
        final View editWaypointView = View.inflate(context, R.layout.waypoint_dialog, null);
        final EditText edit = (EditText) editWaypointView.findViewById(R.id.editName);

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("Set Waypoint Name").setView(editWaypointView);
        b.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ok.okClicked(edit.getText().toString());
            }
        });
        b.setNegativeButton("Cancel", null);

        edit.setText(name);
        edit.setSelection(name.length());

        Spinner spinner = (Spinner) editWaypointView.findViewById(R.id.editSuggestionSpinner);
        if (names.size() > 0) {
            spinner.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, names));
        } else {
            final List<String> defaultList = Arrays.asList(name, "Waypoint");
            spinner.setAdapter(new ArrayAdapter(context, android.R.layout.simple_spinner_item, defaultList));
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean ignore = true;
            // 'ignore' is a hack to work around initial click event we seem to get when
            // the dialog is initialised.

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignore) {
                    ignore = false;
                    return;
                }
                EditText et = (EditText) editWaypointView.findViewById(R.id.editName);
                final String text = (String) parent.getItemAtPosition(position);
                et.setText(text);
                et.setSelection(text.length());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AlertDialog d = b.create();
        d.show();
    }
}

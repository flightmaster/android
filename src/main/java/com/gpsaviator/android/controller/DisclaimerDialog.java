package com.gpsaviator.android.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.webkit.WebView;
import com.gpsaviator.android.R;

/**
 * Created by khaines on 25/05/2015.
 */
public class DisclaimerDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        WebView wv = new WebView(getActivity());
        wv.loadData(getString(R.string.disclaimer), "text/html", "utf-8");
        builder.setView(wv);
        builder.setPositiveButton(R.string.i_agree, null);

        Dialog d = builder.create();
        d.setCanceledOnTouchOutside(false);
        this.setCancelable(false);
        return d;
    }
}
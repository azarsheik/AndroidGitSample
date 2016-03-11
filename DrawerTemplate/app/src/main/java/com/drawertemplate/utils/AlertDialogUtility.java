package com.drawertemplate.utils;

import android.app.Activity;
import android.app.AlertDialog;

import com.drawertemplate.R;

public class AlertDialogUtility {

    public static void showInternetErrorDialog(Activity context) {
        Activity activity = context;
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setMessage(activity.getResources().getString(R.string.error_dialog_internet));
        dialog.setNegativeButton(activity.getResources().getString(R.string.ok), null);
        dialog.show();
    }
}

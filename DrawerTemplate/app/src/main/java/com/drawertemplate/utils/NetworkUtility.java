package com.drawertemplate.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtility {

    /* This method checks whether the device has mobile data or Wifi connection*/
    public static boolean hasDataOrWifiConnection(Context context) {
        boolean isNetworkAvailable = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            isNetworkAvailable = true;
        }
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            isNetworkAvailable = true;
        }
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            isNetworkAvailable = true;
        }
        return isNetworkAvailable;
    }
}

package com.sam_chordas.android.stockhawk.utitlity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by harshikesh.kumar on 11/10/16.
 */
public class Util {

  public static boolean isNetworkConnected(Context context){
      ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

  }
}

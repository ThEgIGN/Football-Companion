package com.example.footballapplication.orientation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class ScreenSizeHelper {

    // Screen is being locked for small phones only, tablets can still use landscape
    @SuppressLint("SourceLockedOrientationActivity")
    public static boolean lockOrientationForSmallPhones(Activity activity) {
        if (smallPhone(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        } else {
            return false;
        }
    }

    public static boolean smallPhone(Activity activity) {
        int screenLayoutSize = activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_SMALL || screenLayoutSize == Configuration.SCREENLAYOUT_SIZE_NORMAL);
    }

}

package com.app.shopper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.app.shopper.SettingsActivity;

import java.util.*;

public class SettingsHelper {
    
    public static void setLocale(Context context) {
        Locale locale = new Locale(getLanguageCode(context));
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
    
    private static String getLanguageCode(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return settings.getString(SettingsActivity.LANGUAGE, "en");
    }
    
}

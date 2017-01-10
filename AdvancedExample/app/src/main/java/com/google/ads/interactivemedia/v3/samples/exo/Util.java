package com.google.ads.interactivemedia.v3.samples.exo;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.ads.interactivemedia.v3.samples.samplevideoplayer.MainApplication;
import com.google.ads.interactivemedia.v3.samples.videoplayerapp.BuildConfig;

/**
 * Created by jasmsison on 10/01/17.
 */

public class Util {
    private static MainApplication application = MainApplication.instance;

    private Util() {}

    public static class Preferences {

        private static SharedPreferences preferences;

        private Preferences() {}

        public static SharedPreferences getPreferences() {
            return PreferenceManager.getDefaultSharedPreferences(application);
        }

        private static String concat(String key) {
            return TextUtils.concat(BuildConfig.APPLICATION_ID, ".", key).toString();
        }

        public static void writeString(String key, String value) {
            preferences = getPreferences();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(concat(key), value).apply();
        }

        public static void writeInt(String key, int value) {
            preferences = getPreferences();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(concat(key), value).apply();
        }

        public static void writeBool(String key, boolean value) {
            preferences = getPreferences();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(concat(key), value).apply();
        }

        public static String readString(String key, String defaultValue) {
            preferences = getPreferences();
            return preferences.getString(concat(key), defaultValue);
        }

        public static int readInt(String key, int defaultValue) {
            preferences = getPreferences();
            return preferences.getInt(concat(key), defaultValue);
        }

        public static boolean readBool(String key, boolean defaultValue) {
            preferences = getPreferences();
            return preferences.getBoolean(concat(key), defaultValue);
        }

/*
        // TODO a generic method requires way too much voodoo
        public<T> void write(String name, T value) {
            preferences = getPreferences();
            if (T instanceof String) {

            }else if(T instanceof Integer) {

            }
        }
*/
    }
}

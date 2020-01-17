package com.h5mota.lib;

import android.content.Context;

public class Editor {
    private static String TAG = "com.h5mota";

    public static void remove(Context context, String name) {
        context.getSharedPreferences(TAG, 0).edit().remove(name).apply();
    }

    public static void clear(Context context) {
        context.getSharedPreferences(TAG, 0).edit().clear().apply();
    }

    public static String getString(Context context, String name) {
        return getString(context, name, "");
    }

    public static int getInt(Context context, String name) {
        return getInt(context, name, 0);
    }

    public static boolean getBoolean(Context context, String name) {
        return getBoolean(context, name, false);
    }

    public static long getLong(Context context, String name) {
        return getLong(context, name, 0);
    }

    public static String getString(Context context, String name, String value) {
        return context.getSharedPreferences(TAG, 0).getString(name, value);
    }

    public static int getInt(Context context, String name, int value) {
        return context.getSharedPreferences(TAG, 0).getInt(name, value);
    }

    public static boolean getBoolean(Context context, String name, boolean value) {
        return context.getSharedPreferences(TAG, 0).getBoolean(name, value);
    }

    public static long getLong(Context context, String name, long value) {
        return context.getSharedPreferences(TAG, 0).getLong(name, value);
    }

    public static void putString(Context context, String name, String val) {
        context.getSharedPreferences(TAG, 0).edit().putString(name, val).apply();
    }

    public static void putInt(Context context, String name, int val) {
        context.getSharedPreferences(TAG, 0).edit().putInt(name, val).apply();
    }

    public static void putLong(Context context, String name, long val) {
        context.getSharedPreferences(TAG, 0).edit().putLong(name, val).apply();
    }

    public static void putBoolean(Context context, String name, Boolean val) {
        context.getSharedPreferences(TAG, 0).edit().putBoolean(name, val).apply();
    }

}

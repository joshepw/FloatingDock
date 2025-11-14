package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.content.SharedPreferences;

public class FloatingButtonConfig {
    private static final String PREFS_NAME = "FloatingButtonPrefs";
    private static final String KEY_ICON_SIZE = "icon_size";
    private static final String KEY_POSITION = "position";
    private static final String KEY_BORDER_RADIUS = "border_radius";
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private static final String KEY_BACKGROUND_ALPHA = "background_alpha";
    private static final String KEY_ICON_COLOR = "icon_color";
    private static final String KEY_ICON_ALPHA = "icon_alpha";
    private static final String KEY_ICON_GAP = "icon_gap";
    private static final String KEY_ICON_PADDING = "icon_padding";
    private static final String KEY_POSITION_MARGIN = "position_margin";
    
    private static final int DEFAULT_ICON_SIZE = 24; // en dp
    private static final String DEFAULT_POSITION = "bottom_right";
    private static final int DEFAULT_BORDER_RADIUS = 8; // en dp
    private static final int DEFAULT_BACKGROUND_COLOR = 0x000000; // Negro
    private static final int DEFAULT_BACKGROUND_ALPHA = 128; // 50% (128/255)
    private static final int DEFAULT_ICON_COLOR = 0xFFFFFF; // Blanco
    private static final int DEFAULT_ICON_ALPHA = 255; // 100% (255/255)
    private static final int DEFAULT_ICON_GAP = 8; // en dp
    private static final int DEFAULT_ICON_PADDING = 0; // en dp
    private static final int DEFAULT_POSITION_MARGIN = 16; // en dp
    
    public static void saveIconSize(Context context, int size) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ICON_SIZE, size).apply();
    }
    
    public static int getIconSize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ICON_SIZE, DEFAULT_ICON_SIZE);
    }
    
    public static void savePosition(Context context, String position) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_POSITION, position).apply();
    }
    
    public static String getPosition(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_POSITION, DEFAULT_POSITION);
    }
    
    public static void saveBorderRadius(Context context, int radiusDp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_BORDER_RADIUS, radiusDp).apply();
    }
    
    public static int getBorderRadius(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_BORDER_RADIUS, DEFAULT_BORDER_RADIUS);
    }
    
    public static void saveBackgroundColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_BACKGROUND_COLOR, color).apply();
    }
    
    public static int getBackgroundColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR);
    }
    
    public static void saveBackgroundAlpha(Context context, int alpha) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_BACKGROUND_ALPHA, alpha).apply();
    }
    
    public static int getBackgroundAlpha(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_BACKGROUND_ALPHA, DEFAULT_BACKGROUND_ALPHA);
    }
    
    public static void saveIconColor(Context context, int color) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ICON_COLOR, color).apply();
    }
    
    public static int getIconColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ICON_COLOR, DEFAULT_ICON_COLOR);
    }
    
    public static void saveIconAlpha(Context context, int alpha) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ICON_ALPHA, alpha).apply();
    }
    
    public static int getIconAlpha(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ICON_ALPHA, DEFAULT_ICON_ALPHA);
    }
    
    public static void saveIconGap(Context context, int gapDp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ICON_GAP, gapDp).apply();
    }
    
    public static int getIconGap(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ICON_GAP, DEFAULT_ICON_GAP);
    }
    
    public static void saveIconPadding(Context context, int paddingDp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_ICON_PADDING, paddingDp).apply();
    }
    
    public static int getIconPadding(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ICON_PADDING, DEFAULT_ICON_PADDING);
    }
    
    public static void savePositionMargin(Context context, int marginDp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_POSITION_MARGIN, marginDp).apply();
    }
    
    public static int getPositionMargin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_POSITION_MARGIN, DEFAULT_POSITION_MARGIN);
    }
}


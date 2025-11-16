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
    private static final String KEY_POSITION_MARGIN_X = "position_margin_x";
    private static final String KEY_POSITION_MARGIN_Y = "position_margin_y";
    private static final String KEY_AUTO_START_PACKAGE = "auto_start_package";
    private static final String KEY_AUTO_START_ACTIVITY = "auto_start_activity";
    private static final String KEY_AUTO_START_LAUNCHED = "auto_start_launched"; // Flag para saber si ya se lanzó en esta sesión
    private static final String KEY_DOCK_DRAGGABLE = "dock_draggable";
    private static final String KEY_DOCK_BEHAVIOR = "dock_behavior"; // "fixed", "hide_on_action", "hide_after_time"
    private static final String KEY_DOCK_HIDE_TIMEOUT = "dock_hide_timeout"; // Tiempo en ms para ocultar
    
    private static final int DEFAULT_ICON_SIZE = 24; // en dp
    private static final String DEFAULT_POSITION = "bottom_right";
    private static final int DEFAULT_BORDER_RADIUS = 8; // en dp
    private static final int DEFAULT_BACKGROUND_COLOR = 0x000000; // Negro
    private static final int DEFAULT_BACKGROUND_ALPHA = 128; // 50% (128/255)
    private static final int DEFAULT_ICON_COLOR = 0xFFFFFF; // Blanco
    private static final int DEFAULT_ICON_ALPHA = 255; // 100% (255/255)
    private static final int DEFAULT_ICON_GAP = 8; // en dp
    private static final int DEFAULT_ICON_PADDING = 0; // en dp
    private static final int DEFAULT_POSITION_MARGIN_X = 16; // en dp
    private static final int DEFAULT_POSITION_MARGIN_Y = 16; // en dp
    
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
        String position = prefs.getString(KEY_POSITION, DEFAULT_POSITION);
        
        // Migrar "center_center" a "center_left" si existe (posición eliminada)
        if ("center_center".equals(position)) {
            savePosition(context, "center_left");
            return "center_left";
        }
        
        return position;
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
    
    public static void savePositionMarginX(Context context, int marginDp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_POSITION_MARGIN_X, marginDp).apply();
    }
    
    public static int getPositionMarginX(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_POSITION_MARGIN_X, DEFAULT_POSITION_MARGIN_X);
    }
    
    public static void savePositionMarginY(Context context, int marginDp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_POSITION_MARGIN_Y, marginDp).apply();
    }
    
    public static int getPositionMarginY(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_POSITION_MARGIN_Y, DEFAULT_POSITION_MARGIN_Y);
    }
    
    // Método de compatibilidad: si existe el valor antiguo, migrarlo a X e Y
    public static void migrateOldPositionMargin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains("position_margin") && !prefs.contains(KEY_POSITION_MARGIN_X)) {
            int oldMargin = prefs.getInt("position_margin", DEFAULT_POSITION_MARGIN_X);
            prefs.edit()
                .putInt(KEY_POSITION_MARGIN_X, oldMargin)
                .putInt(KEY_POSITION_MARGIN_Y, oldMargin)
                .remove("position_margin")
                .apply();
        }
    }
    
    // Auto Start App
    public static void saveAutoStartApp(Context context, String packageName, String activityName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_AUTO_START_PACKAGE, packageName)
            .putString(KEY_AUTO_START_ACTIVITY, activityName)
            .apply();
    }
    
    public static void clearAutoStartApp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .remove(KEY_AUTO_START_PACKAGE)
            .remove(KEY_AUTO_START_ACTIVITY)
            .apply();
    }
    
    public static String getAutoStartPackage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AUTO_START_PACKAGE, null);
    }
    
    public static String getAutoStartActivity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AUTO_START_ACTIVITY, null);
    }
    
    public static boolean isAutoStartApp(Context context, String packageName, String activityName) {
        String savedPackage = getAutoStartPackage(context);
        String savedActivity = getAutoStartActivity(context);
        if (savedPackage == null || packageName == null) {
            return false;
        }
        // Comparar package
        if (!savedPackage.equals(packageName)) {
            return false;
        }
        // Comparar activity (puede ser null)
        // Si ambos son null, coinciden
        if (savedActivity == null && activityName == null) {
            return true;
        }
        // Si uno es null y el otro no, no coinciden
        if (savedActivity == null || activityName == null) {
            return false;
        }
        // Ambos tienen valor, comparar
        return savedActivity.equals(activityName);
    }
    
    // Flag para saber si ya se lanzó la app en esta sesión
    public static void setAutoStartLaunched(Context context, boolean launched) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AUTO_START_LAUNCHED, launched).apply();
    }
    
    public static boolean isAutoStartLaunched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_START_LAUNCHED, false);
    }
    
    // Dock Draggable
    public static void saveDockDraggable(Context context, boolean draggable) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DOCK_DRAGGABLE, draggable).apply();
    }
    
    public static boolean isDockDraggable(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DOCK_DRAGGABLE, false);
    }
    
    // Dock Behavior
    public static void saveDockBehavior(Context context, String behavior) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DOCK_BEHAVIOR, behavior).apply();
    }
    
    public static String getDockBehavior(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String behavior = prefs.getString(KEY_DOCK_BEHAVIOR, "hide_on_action");
        // Migrar "fixed" a "hide_on_action" si existe
        if ("fixed".equals(behavior)) {
            behavior = "hide_on_action";
            saveDockBehavior(context, behavior);
        }
        return behavior;
    }
    
    public static void saveDockHideTimeout(Context context, int timeoutMs) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DOCK_HIDE_TIMEOUT, timeoutMs).apply();
    }
    
    public static int getDockHideTimeout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DOCK_HIDE_TIMEOUT, 5000); // Por defecto: 5 segundos
    }
}


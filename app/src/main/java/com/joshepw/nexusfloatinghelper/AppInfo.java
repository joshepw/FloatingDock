package com.joshepw.nexusfloatinghelper;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String packageName;
    private String appName;
    private Drawable icon;
    private boolean isSystemApp;
    private boolean isLaunchable;
    private boolean hasMultipleActivities;
    
    public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp) {
        this(packageName, appName, icon, isSystemApp, true, false);
    }
    
    public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp, boolean isLaunchable) {
        this(packageName, appName, icon, isSystemApp, isLaunchable, false);
    }
    
    public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp, boolean isLaunchable, boolean hasMultipleActivities) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.isSystemApp = isSystemApp;
        this.isLaunchable = isLaunchable;
        this.hasMultipleActivities = hasMultipleActivities;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public Drawable getIcon() {
        return icon;
    }
    
    public boolean isSystemApp() {
        return isSystemApp;
    }
    
    public boolean isLaunchable() {
        return isLaunchable;
    }
    
    public boolean hasMultipleActivities() {
        return hasMultipleActivities;
    }
}


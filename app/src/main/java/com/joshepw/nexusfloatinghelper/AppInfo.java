package com.joshepw.nexusfloatinghelper;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String packageName;
    private String appName;
    private Drawable icon;
    private boolean isSystemApp;
    private boolean isLaunchable;
    
    public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp) {
        this(packageName, appName, icon, isSystemApp, true);
    }
    
    public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp, boolean isLaunchable) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.isSystemApp = isSystemApp;
        this.isLaunchable = isLaunchable;
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
}


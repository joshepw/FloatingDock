package com.joshepw.nexusfloatinghelper;

public class ActivityInfo {
    private String packageName;
    private String activityName;
    private String label;
    private boolean isLaunchable;
    private boolean isMainLauncher; 

    public ActivityInfo(String packageName, String activityName, String label) {
        this(packageName, activityName, label, true, false);
    }

    public ActivityInfo(String packageName, String activityName, String label, boolean isLaunchable) {
        this(packageName, activityName, label, isLaunchable, false);
    }

    public ActivityInfo(String packageName, String activityName, String label, boolean isLaunchable, boolean isMainLauncher) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label;
        this.isLaunchable = isLaunchable;
        this.isMainLauncher = isMainLauncher;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLaunchable() {
        return isLaunchable;
    }

    public boolean isMainLauncher() {
        return isMainLauncher;
    }

    public String getFullName() {
        return packageName + "/" + activityName;
    }
}


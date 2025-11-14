package com.joshepw.nexusfloatinghelper;

public class ActivityInfo {
    private String packageName;
    private String activityName;
    private String label;
    
    public ActivityInfo(String packageName, String activityName, String label) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label;
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
    
    public String getFullName() {
        return packageName + "/" + activityName;
    }
}


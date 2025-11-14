package com.joshepw.nexusfloatinghelper;

public class DockApp {
    private String packageName;
    private String materialIconName;
    private String activityName; // Activity específica a lanzar (opcional)
    private int index;
    
    public DockApp(String packageName, String materialIconName, int index) {
        this(packageName, materialIconName, null, index);
    }
    
    public DockApp(String packageName, String materialIconName, String activityName, int index) {
        this.packageName = packageName;
        this.materialIconName = materialIconName;
        this.activityName = activityName;
        this.index = index;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getMaterialIconName() {
        return materialIconName;
    }
    
    public String getActivityName() {
        return activityName;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setMaterialIconName(String materialIconName) {
        this.materialIconName = materialIconName;
    }
    
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
}


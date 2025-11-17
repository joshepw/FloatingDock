package com.joshepw.nexusfloatinghelper;

public class DockApp {
    private String packageName;
    private String materialIconName;
    private String activityName; 
    private int index;
    private String actionType; 
    private String actionId; 

    public DockApp(String packageName, String materialIconName, int index) {
        this(packageName, materialIconName, null, index);
    }

    public DockApp(String packageName, String materialIconName, String activityName, int index) {
        this.packageName = packageName;
        this.materialIconName = materialIconName;
        this.activityName = activityName;
        this.index = index;
        this.actionType = "app"; 
        this.actionId = null;
    }


    public DockApp(String actionId, String materialIconName, int index, boolean isAction) {
        this.actionId = actionId;
        this.materialIconName = materialIconName;
        this.index = index;
        this.actionType = "action";
        this.packageName = null;
        this.activityName = null;
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

    public String getActionType() {
        return actionType != null ? actionType : "app";
    }

    public String getActionId() {
        return actionId;
    }

    public boolean isAction() {
        return "action".equals(actionType);
    }

    public void setMaterialIconName(String materialIconName) {
        this.materialIconName = materialIconName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}


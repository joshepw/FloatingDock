package com.joshepw.nexusfloatinghelper;

public class SystemAction {
    private String actionId;
    private String actionName;
    private String iconName; 
    private String description;

    public SystemAction(String actionId, String actionName, String iconName, String description) {
        this.actionId = actionId;
        this.actionName = actionName;
        this.iconName = iconName;
        this.description = description;
    }

    public String getActionId() {
        return actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public String getIconName() {
        return iconName;
    }

    public String getDescription() {
        return description;
    }
}


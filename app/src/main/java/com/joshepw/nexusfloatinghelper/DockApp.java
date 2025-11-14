package com.joshepw.nexusfloatinghelper;

public class DockApp {
    private String packageName;
    private String materialIconName;
    private int index;
    
    public DockApp(String packageName, String materialIconName, int index) {
        this.packageName = packageName;
        this.materialIconName = materialIconName;
        this.index = index;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getMaterialIconName() {
        return materialIconName;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setMaterialIconName(String materialIconName) {
        this.materialIconName = materialIconName;
    }
}


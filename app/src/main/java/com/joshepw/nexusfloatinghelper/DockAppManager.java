package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DockAppManager {
    private static final String PREFS_NAME = "DockAppPrefs";
    private static final String KEY_DOCK_APPS = "dock_apps";
    private static Gson gson = new Gson();

    public static void saveDockApps(Context context, List<DockApp> dockApps) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(dockApps);
        prefs.edit().putString(KEY_DOCK_APPS, json).apply();
    }

    public static List<DockApp> getDockApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DOCK_APPS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<DockApp>>(){}.getType();
        List<DockApp> apps = gson.fromJson(json, type);
        return apps != null ? apps : new ArrayList<>();
    }

    public static void addDockApp(Context context, DockApp dockApp) {
        List<DockApp> apps = getDockApps(context);
        apps.add(dockApp);
        saveDockApps(context, apps);
    }

    public static void removeDockApp(Context context, int index) {
        List<DockApp> apps = getDockApps(context);
        if (index >= 0 && index < apps.size()) {
            apps.remove(index);
            saveDockApps(context, apps);
        }
    }

    public static void updateDockApp(Context context, int index, DockApp dockApp) {
        List<DockApp> apps = getDockApps(context);
        if (index >= 0 && index < apps.size()) {
            apps.set(index, dockApp);
            saveDockApps(context, apps);
        }
    }

    public static void reorderDockApps(Context context, int fromPosition, int toPosition) {
        List<DockApp> apps = getDockApps(context);
        if (fromPosition >= 0 && fromPosition < apps.size() && 
            toPosition >= 0 && toPosition < apps.size() && 
            fromPosition != toPosition) {

            DockApp movedApp = apps.remove(fromPosition);
            apps.add(toPosition, movedApp);


            for (int i = 0; i < apps.size(); i++) {
                apps.get(i).setIndex(i);
            }

            saveDockApps(context, apps);
        }
    }
}


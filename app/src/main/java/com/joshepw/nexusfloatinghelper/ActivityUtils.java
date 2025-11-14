package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ActivityUtils {
    private static final String TAG = "ActivityUtils";
    
    /**
     * Obtiene todas las activities lanzables de un paquete
     */
    public static List<ActivityInfo> getLaunchableActivities(Context context, String packageName) {
        List<ActivityInfo> activities = new ArrayList<>();
        
        if (context == null || packageName == null || packageName.isEmpty()) {
            return activities;
        }
        
        try {
            PackageManager pm = context.getPackageManager();
            
            // Buscar todas las activities con ACTION_MAIN y CATEGORY_LAUNCHER
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            
            int flags = PackageManager.MATCH_DISABLED_COMPONENTS;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                flags |= PackageManager.MATCH_ALL;
            }
            
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, flags);
            
            if (resolveInfos != null) {
                for (ResolveInfo resolveInfo : resolveInfos) {
                    if (resolveInfo == null || resolveInfo.activityInfo == null) {
                        continue;
                    }
                    
                    if (packageName.equals(resolveInfo.activityInfo.packageName)) {
                        String activityName = resolveInfo.activityInfo.name;
                        String label = resolveInfo.loadLabel(pm).toString();
                        
                        // Si el label está vacío, usar el nombre de la activity
                        if (label == null || label.isEmpty()) {
                            label = activityName.substring(activityName.lastIndexOf('.') + 1);
                        }
                        
                        activities.add(new ActivityInfo(packageName, activityName, label));
                    }
                }
            }
            
            Log.d(TAG, "Encontradas " + activities.size() + " activities para " + packageName);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener activities para " + packageName, e);
        }
        
        return activities;
    }
    
    /**
     * Verifica si un paquete tiene múltiples activities lanzables
     */
    public static boolean hasMultipleActivities(Context context, String packageName) {
        List<ActivityInfo> activities = getLaunchableActivities(context, packageName);
        return activities.size() > 1;
    }
}


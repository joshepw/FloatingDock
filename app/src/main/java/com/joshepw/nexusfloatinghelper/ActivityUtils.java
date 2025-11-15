package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityUtils {
    private static final String TAG = "ActivityUtils";
    
    /**
     * Obtiene todas las activities lanzables de un paquete
     */
    public static List<com.joshepw.nexusfloatinghelper.ActivityInfo> getLaunchableActivities(Context context, String packageName) {
        List<com.joshepw.nexusfloatinghelper.ActivityInfo> activities = new ArrayList<>();
        Set<String> addedActivities = new HashSet<>(); // Para evitar duplicados
        
        if (context == null || packageName == null || packageName.isEmpty()) {
            return activities;
        }
        
        try {
            PackageManager pm = context.getPackageManager();
            
            // Primero, obtener la activity principal que el launcher usa por defecto
            String mainLauncherActivityName = null;
            try {
                Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
                if (launchIntent != null && launchIntent.getComponent() != null) {
                    mainLauncherActivityName = launchIntent.getComponent().getClassName();
                    Log.d(TAG, "Activity principal del launcher para " + packageName + ": " + mainLauncherActivityName);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al obtener launch intent para " + packageName, e);
            }
            
            // Método 1: Buscar activities con ACTION_MAIN y CATEGORY_LAUNCHER
            try {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mainIntent.setPackage(packageName); // Filtrar por paquete específico
                
                int flags = PackageManager.MATCH_DISABLED_COMPONENTS | PackageManager.MATCH_DEFAULT_ONLY;
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
                            
                            if (!addedActivities.contains(activityName)) {
                                String label = resolveInfo.loadLabel(pm).toString();
                                
                                // Si el label está vacío, usar el nombre de la activity
                                if (label == null || label.isEmpty()) {
                                    label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                }
                                
                                // Verificar si es la activity principal del launcher
                                boolean isMainLauncher = (mainLauncherActivityName != null && 
                                    mainLauncherActivityName.equals(activityName));
                                
                                activities.add(new com.joshepw.nexusfloatinghelper.ActivityInfo(
                                    packageName, activityName, label, true, isMainLauncher));
                                addedActivities.add(activityName);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al obtener activities con CATEGORY_LAUNCHER para " + packageName, e);
            }
            
            // Método 2: Obtener activities del paquete que tengan intent-filters relevantes
            // Solo se usa si el Método 1 no encontró suficientes activities
            // Esto es útil para apps como "Car UI" que tienen múltiples activities internas
            // Nota: mainLauncherActivityName se usa para marcar la principal
            if (activities.size() <= 1) {
                try {
                    int packageFlags = PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        packageFlags |= PackageManager.MATCH_DISABLED_COMPONENTS;
                    }
                    
                    PackageInfo packageInfo = pm.getPackageInfo(packageName, packageFlags);
                    
                    if (packageInfo != null && packageInfo.activities != null) {
                        for (android.content.pm.ActivityInfo activityInfo : packageInfo.activities) {
                            if (activityInfo == null || activityInfo.name == null) {
                                continue;
                            }
                            
                            String activityName = activityInfo.name;
                            
                            // Evitar duplicados
                            if (addedActivities.contains(activityName)) {
                                continue;
                            }
                            
                            // Solo incluir activities que:
                            // 1. Sean exportadas Y tengan un nombre que no sea genérico
                            // 2. O que tengan un intent-filter con ACTION_MAIN (aunque no tengan CATEGORY_LAUNCHER)
                            boolean shouldInclude = false;
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                // Solo incluir si es exportada
                                if (activityInfo.exported) {
                                    // Verificar que tenga un nombre que no sea genérico
                                    String simpleName = activityName.substring(activityName.lastIndexOf('.') + 1);
                                    // Excluir activities genéricas comunes que no queremos mostrar
                                    if (!simpleName.equals("MainActivity") && 
                                        !simpleName.equals("LauncherActivity") &&
                                        !simpleName.equals("SplashActivity") &&
                                        !simpleName.startsWith(".") &&
                                        !simpleName.contains("$")) {
                                        shouldInclude = true;
                                    }
                                }
                            } else {
                                // En versiones anteriores, solo incluir si es exportada
                                if (activityInfo.exported) {
                                    shouldInclude = true;
                                }
                            }
                            
                            // Verificar si tiene un intent-filter con ACTION_MAIN
                            if (!shouldInclude) {
                                try {
                                    Intent testIntent = new Intent(Intent.ACTION_MAIN);
                                    testIntent.setClassName(packageName, activityName);
                                    ResolveInfo resolveInfo = pm.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                                        shouldInclude = true;
                                    }
                                } catch (Exception e) {
                                    // Si falla, no incluir
                                }
                            }
                            
                            if (shouldInclude) {
                                // Intentar obtener el label de la activity
                                String label = activityInfo.name;
                                try {
                                    if (activityInfo.labelRes != 0) {
                                        label = pm.getResourcesForApplication(packageName)
                                                .getString(activityInfo.labelRes);
                                    } else if (activityInfo.nonLocalizedLabel != null) {
                                        label = activityInfo.nonLocalizedLabel.toString();
                                    } else {
                                        // Extraer solo el nombre de la clase
                                        label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                    }
                                } catch (Exception e) {
                                    // Si falla, usar el nombre de la clase
                                    label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                }
                                
                                // Si el label está vacío, usar el nombre de la clase
                                if (label == null || label.isEmpty()) {
                                    label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                }
                                
                                // Verificar si es la activity principal del launcher
                                boolean isMainLauncher = (mainLauncherActivityName != null && 
                                    mainLauncherActivityName.equals(activityName));
                                
                                activities.add(new com.joshepw.nexusfloatinghelper.ActivityInfo(packageName, activityName, label, true, isMainLauncher));
                                addedActivities.add(activityName);
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Paquete no encontrado: " + packageName);
                } catch (Exception e) {
                    Log.w(TAG, "Error al obtener activities del paquete " + packageName, e);
                }
            }
            
            // Método 3: Buscar con diferentes intents para encontrar más activities
            // Solo si aún no tenemos suficientes
            // Nota: mainLauncherActivityName se usa para marcar la principal
            if (activities.size() <= 1) {
                try {
                    // Buscar activities con ACTION_VIEW que sean específicas del paquete
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    viewIntent.setPackage(packageName);
                    
                    int flags = PackageManager.MATCH_DISABLED_COMPONENTS | PackageManager.MATCH_DEFAULT_ONLY;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        flags |= PackageManager.MATCH_ALL;
                    }
                    
                    List<ResolveInfo> viewActivities = pm.queryIntentActivities(viewIntent, flags);
                    
                    if (viewActivities != null) {
                        for (ResolveInfo resolveInfo : viewActivities) {
                            if (resolveInfo == null || resolveInfo.activityInfo == null) {
                                continue;
                            }
                            
                            if (packageName.equals(resolveInfo.activityInfo.packageName)) {
                                String activityName = resolveInfo.activityInfo.name;
                                
                                if (!addedActivities.contains(activityName)) {
                                    String label = resolveInfo.loadLabel(pm).toString();
                                    
                                    if (label == null || label.isEmpty()) {
                                        label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                    }
                                    
                                    // Verificar si es la activity principal del launcher
                                    boolean isMainLauncher = (mainLauncherActivityName != null && 
                                        mainLauncherActivityName.equals(activityName));
                                    
                                    activities.add(new com.joshepw.nexusfloatinghelper.ActivityInfo(packageName, activityName, label, true, isMainLauncher));
                                    addedActivities.add(activityName);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error al obtener activities con ACTION_VIEW para " + packageName, e);
                }
            }
            
            // Log para debug: verificar cuál activity está marcada como principal
            for (com.joshepw.nexusfloatinghelper.ActivityInfo activity : activities) {
                if (activity.isMainLauncher()) {
                    Log.d(TAG, "Activity principal encontrada: " + activity.getActivityName() + " (" + activity.getLabel() + ")");
                }
            }
            
            Log.d(TAG, "Total encontradas " + activities.size() + " activities para " + packageName);
        } catch (Exception e) {
            Log.e(TAG, "Error general al obtener activities para " + packageName, e);
        }
        
        return activities;
    }
    
    /**
     * Obtiene todas las activities (lanzables y no lanzables) de un paquete
     */
    public static List<com.joshepw.nexusfloatinghelper.ActivityInfo> getAllActivities(Context context, String packageName) {
        List<com.joshepw.nexusfloatinghelper.ActivityInfo> allActivities = new ArrayList<>();
        Set<String> addedActivities = new HashSet<>();
        
        if (context == null || packageName == null || packageName.isEmpty()) {
            return allActivities;
        }
        
        try {
            PackageManager pm = context.getPackageManager();
            
            // Primero obtener las lanzables
            List<com.joshepw.nexusfloatinghelper.ActivityInfo> launchableActivities = getLaunchableActivities(context, packageName);
            for (com.joshepw.nexusfloatinghelper.ActivityInfo activity : launchableActivities) {
                allActivities.add(activity);
                addedActivities.add(activity.getActivityName());
            }
            
            // Luego obtener todas las activities del paquete para encontrar las no lanzables
            try {
                int packageFlags = PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    packageFlags |= PackageManager.MATCH_DISABLED_COMPONENTS;
                }
                
                PackageInfo packageInfo = pm.getPackageInfo(packageName, packageFlags);
                
                if (packageInfo != null && packageInfo.activities != null) {
                    for (android.content.pm.ActivityInfo activityInfo : packageInfo.activities) {
                        if (activityInfo == null || activityInfo.name == null) {
                            continue;
                        }
                        
                        String activityName = activityInfo.name;
                        
                        // Evitar duplicados
                        if (addedActivities.contains(activityName)) {
                            continue;
                        }
                        
                        // Verificar si es realmente lanzable
                        boolean isLaunchable = false;
                        try {
                            Intent testIntent = new Intent();
                            testIntent.setClassName(packageName, activityName);
                            ResolveInfo resolveInfo = pm.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                                isLaunchable = true;
                            }
                        } catch (Exception e) {
                            // No es lanzable
                        }
                        
                        // Solo incluir si no es lanzable (las lanzables ya las tenemos)
                        if (!isLaunchable) {
                            // Intentar obtener el label
                            String label = activityInfo.name;
                            try {
                                if (activityInfo.labelRes != 0) {
                                    label = pm.getResourcesForApplication(packageName)
                                            .getString(activityInfo.labelRes);
                                } else if (activityInfo.nonLocalizedLabel != null) {
                                    label = activityInfo.nonLocalizedLabel.toString();
                                } else {
                                    label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                }
                            } catch (Exception e) {
                                label = activityName.substring(activityName.lastIndexOf('.') + 1);
                            }
                            
                            if (label == null || label.isEmpty()) {
                                label = activityName.substring(activityName.lastIndexOf('.') + 1);
                            }
                            
                            allActivities.add(new com.joshepw.nexusfloatinghelper.ActivityInfo(
                                packageName, activityName, label, false, false));
                            addedActivities.add(activityName);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Paquete no encontrado: " + packageName);
            } catch (Exception e) {
                Log.w(TAG, "Error al obtener activities no lanzables para " + packageName, e);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error general al obtener todas las activities para " + packageName, e);
        }
        
        return allActivities;
    }
    
    /**
     * Verifica si un paquete tiene múltiples activities lanzables
     */
    public static boolean hasMultipleActivities(Context context, String packageName) {
        List<com.joshepw.nexusfloatinghelper.ActivityInfo> activities = getLaunchableActivities(context, packageName);
        return activities.size() > 1;
    }
}


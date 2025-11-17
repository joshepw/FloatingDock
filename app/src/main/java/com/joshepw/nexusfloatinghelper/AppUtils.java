package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppUtils {
    private static final String TAG = "AppUtils";

    public static List<AppInfo> getAllInstalledApps(Context context) {
        List<AppInfo> apps = new ArrayList<>();

        if (context == null) {
            Log.e(TAG, "Context es null");
            return apps;
        }

        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "PackageManager es null");
            return apps;
        }

        try {

            Set<String> packageNames = new HashSet<>();


            try {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                int flags = PackageManager.MATCH_DISABLED_COMPONENTS;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    flags |= PackageManager.MATCH_ALL;
                }

                List<ResolveInfo> launcherApps = pm.queryIntentActivities(mainIntent, flags);
                if (launcherApps != null) {
                    for (ResolveInfo resolveInfo : launcherApps) {
                        if (resolveInfo != null && resolveInfo.activityInfo != null) {
                            packageNames.add(resolveInfo.activityInfo.packageName);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al obtener apps con launcher: " + e.getMessage());
            }


            try {
                int appFlags = PackageManager.GET_META_DATA | PackageManager.MATCH_DISABLED_COMPONENTS;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appFlags |= PackageManager.MATCH_ALL;
                }

                List<ApplicationInfo> allApps = pm.getInstalledApplications(appFlags);
                if (allApps != null) {
                    for (ApplicationInfo appInfo : allApps) {
                        if (appInfo == null || appInfo.packageName == null) continue;

                        String packageName = appInfo.packageName;


                        try {
                            boolean isLaunchable = pm.getLaunchIntentForPackage(packageName) != null;
                            if (isLaunchable) {
                                packageNames.add(packageName);
                            }
                        } catch (Exception e) {

                            packageNames.add(packageName);
                        }
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Error de seguridad al obtener apps instaladas. Verifica permisos: " + e.getMessage());

            } catch (Exception e) {
                Log.w(TAG, "Error al obtener todas las apps instaladas: " + e.getMessage());
            }


            for (String packageName : packageNames) {
                if (packageName == null || packageName.isEmpty()) continue;

                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                    if (appInfo == null) continue;

                    String appName = "";
                    android.graphics.drawable.Drawable icon = null;

                    try {
                        appName = pm.getApplicationLabel(appInfo).toString();
                        if (appName == null || appName.isEmpty()) {
                            appName = packageName;
                        }
                    } catch (Exception e) {
                        appName = packageName;
                    }

                    try {
                        icon = pm.getApplicationIcon(appInfo);
                    } catch (Exception e) {

                        icon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon);
                    }

                    boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                            && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;

                    boolean isLaunchable = false;
                    try {
                        isLaunchable = pm.getLaunchIntentForPackage(packageName) != null;
                    } catch (Exception e) {

                    }


                    boolean hasMultipleActivities = false;
                    if (isLaunchable) {
                        try {
                            hasMultipleActivities = ActivityUtils.hasMultipleActivities(context, packageName);
                        } catch (Exception e) {

                        }
                    }

                    apps.add(new AppInfo(packageName, appName, icon, isSystemApp, isLaunchable, hasMultipleActivities));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "App no encontrada: " + packageName);
                } catch (Exception e) {
                    Log.w(TAG, "Error al procesar app " + packageName + ": " + e.getMessage());
                }
            }

            Log.d(TAG, "Total apps encontradas: " + apps.size());
        } catch (Exception e) {
            Log.e(TAG, "Error general al obtener apps", e);
        }

        return apps;
    }
}


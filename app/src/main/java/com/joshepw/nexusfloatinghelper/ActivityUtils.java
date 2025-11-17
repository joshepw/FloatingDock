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


    public static List<com.joshepw.nexusfloatinghelper.ActivityInfo> getLaunchableActivities(Context context, String packageName) {
        List<com.joshepw.nexusfloatinghelper.ActivityInfo> activities = new ArrayList<>();
        Set<String> addedActivities = new HashSet<>(); 

        if (context == null || packageName == null || packageName.isEmpty()) {
            return activities;
        }

        try {
            PackageManager pm = context.getPackageManager();


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


            try {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mainIntent.setPackage(packageName); 

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


                                if (label == null || label.isEmpty()) {
                                    label = activityName.substring(activityName.lastIndexOf('.') + 1);
                                }


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


                            if (addedActivities.contains(activityName)) {
                                continue;
                            }


                            boolean shouldInclude = false;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                if (activityInfo.exported) {

                                    String simpleName = activityName.substring(activityName.lastIndexOf('.') + 1);

                                    if (!simpleName.equals("MainActivity") && 
                                        !simpleName.equals("LauncherActivity") &&
                                        !simpleName.equals("SplashActivity") &&
                                        !simpleName.startsWith(".") &&
                                        !simpleName.contains("$")) {
                                        shouldInclude = true;
                                    }
                                }
                            } else {

                                if (activityInfo.exported) {
                                    shouldInclude = true;
                                }
                            }


                            if (!shouldInclude) {
                                try {
                                    Intent testIntent = new Intent(Intent.ACTION_MAIN);
                                    testIntent.setClassName(packageName, activityName);
                                    ResolveInfo resolveInfo = pm.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                                        shouldInclude = true;
                                    }
                                } catch (Exception e) {

                                }
                            }

                            if (shouldInclude) {

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


            if (activities.size() <= 1) {
                try {

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


    public static List<com.joshepw.nexusfloatinghelper.ActivityInfo> getAllActivities(Context context, String packageName) {
        List<com.joshepw.nexusfloatinghelper.ActivityInfo> allActivities = new ArrayList<>();
        Set<String> addedActivities = new HashSet<>();

        if (context == null || packageName == null || packageName.isEmpty()) {
            return allActivities;
        }

        try {
            PackageManager pm = context.getPackageManager();


            List<com.joshepw.nexusfloatinghelper.ActivityInfo> launchableActivities = getLaunchableActivities(context, packageName);
            for (com.joshepw.nexusfloatinghelper.ActivityInfo activity : launchableActivities) {
                allActivities.add(activity);
                addedActivities.add(activity.getActivityName());
            }


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


                        if (addedActivities.contains(activityName)) {
                            continue;
                        }


                        boolean isLaunchable = false;
                        try {
                            Intent testIntent = new Intent();
                            testIntent.setClassName(packageName, activityName);
                            ResolveInfo resolveInfo = pm.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
                            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                                isLaunchable = true;
                            }
                        } catch (Exception e) {

                        }


                        if (!isLaunchable) {

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


    public static boolean hasMultipleActivities(Context context, String packageName) {
        List<com.joshepw.nexusfloatinghelper.ActivityInfo> activities = getLaunchableActivities(context, packageName);
        return activities.size() > 1;
    }
}


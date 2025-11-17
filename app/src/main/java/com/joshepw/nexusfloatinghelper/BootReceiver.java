package com.joshepw.nexusfloatinghelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final int DELAY_MS = 2000; 

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver recibió acción: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
            "com.htc.intent.action.QUICKBOOT_POWERON".equals(action)) {


            FloatingButtonConfig.setAutoStartLaunched(context, false);
            Log.d(TAG, "Flag de inicio automático limpiado al arrancar el sistema");

            Log.d(TAG, "Boot completado, iniciando servicio con delay de " + DELAY_MS + "ms");


            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startBackgroundService(context);
                }
            }, DELAY_MS);
        }
    }

    private void startBackgroundService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, BackgroundService.class);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    context.startForegroundService(serviceIntent);
                    Log.d(TAG, "Servicio iniciado con startForegroundService");
                } catch (IllegalStateException e) {

                    Log.w(TAG, "startForegroundService falló, intentando con startService", e);
                    context.startService(serviceIntent);
                }
            } else {
                context.startService(serviceIntent);
                Log.d(TAG, "Servicio iniciado con startService");
            }

            Log.d(TAG, "Servicio iniciado correctamente después del boot");
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar servicio en boot", e);


            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent(context, BackgroundService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent);
                        } else {
                            context.startService(serviceIntent);
                        }
                        Log.d(TAG, "Servicio iniciado en segundo intento");
                    } catch (Exception e2) {
                        Log.e(TAG, "Error en segundo intento de iniciar servicio", e2);
                    }
                }
            }, 5000); 
        }
    }
}


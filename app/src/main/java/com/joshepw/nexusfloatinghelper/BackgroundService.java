package com.joshepw.nexusfloatinghelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "BackgroundServiceChannel";
    
    private WindowManager windowManager;
    private View floatingButtonView;
    private WindowManager.LayoutParams params;
    private LinearLayout iconContainer;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
    private View keyboardDetectionView;
    private WindowManager.LayoutParams keyboardDetectionParams;
    private boolean isKeyboardVisible = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");
        createNotificationChannel();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio iniciado");
        
        // Crear notificación para foreground service
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
        
        // Iniciar como foreground service
        startForeground(NOTIFICATION_ID, notification);
        
        // Mostrar botón flotante si tenemos el permiso
        if (Settings.canDrawOverlays(this)) {
            showFloatingButton();
        } else {
            Log.w(TAG, "No se tiene permiso para dibujar sobre otras apps");
        }
        
        // Retornar START_STICKY para que el servicio se reinicie si es terminado
        return START_STICKY;
    }
    
    private void showFloatingButton() {
        if (floatingButtonView != null) {
            // Si ya existe, removerlo primero para aplicar nuevas configuraciones
            removeFloatingButton();
        }
        
        try {
            // Crear contenedor de iconos programáticamente
            iconContainer = new LinearLayout(this);
            iconContainer.setOrientation(LinearLayout.HORIZONTAL);
            iconContainer.setPadding(16, 16, 16, 16);
            
            // Aplicar fondo con border radius configurado
            applyDockBackground();
            
            // Crear todos los iconos del dock
            createDockIcons();
            
            // Configurar parámetros de WindowManager
            params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            
            floatingButtonView = iconContainer;
            
            // Aplicar posición inicial desde configuraciones
            applyInitialPosition();
            
            // Agregar la vista al WindowManager
            windowManager.addView(floatingButtonView, params);
            
            // Actualizar posición después de agregar la vista (necesario para que se aplique correctamente)
            // Esto asegura que el margen se aplique correctamente
            if (params != null && floatingButtonView != null) {
                try {
                    windowManager.updateViewLayout(floatingButtonView, params);
                    Log.d(TAG, "Posición actualizada después de agregar vista");
                } catch (Exception e) {
                    Log.e(TAG, "Error al actualizar posición", e);
                }
            }
            
            // Verificar que los iconos estén creados correctamente
            updateDockIcons();
            
            // Configurar detección de teclado
            setupKeyboardDetection();
            
            Log.d(TAG, "Botón flotante mostrado");
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar botón flotante", e);
        }
    }
    
    private void setupKeyboardDetection() {
        try {
            // Para Android 11+ (API 30+), usar WindowInsets (método más moderno y eficiente)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setupKeyboardDetectionWithWindowInsets();
            } else {
                // Para versiones anteriores, usar ViewTreeObserver con getWindowVisibleDisplayFrame
                setupKeyboardDetectionWithViewTreeObserver();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar detección de teclado", e);
        }
    }
    
    private void setupKeyboardDetectionWithWindowInsets() {
        try {
            // Usar la vista flotante principal como root para detectar insets
            if (floatingButtonView == null) {
                Log.w(TAG, "floatingButtonView es null, no se puede configurar detección con WindowInsets");
                // Fallback al método anterior
                setupKeyboardDetectionWithViewTreeObserver();
                return;
            }
            
            // Configurar listener de WindowInsets en la vista raíz del dock
            View rootView = floatingButtonView.getRootView();
            if (rootView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                    try {
                        boolean keyboardNowVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
                        
                        if (keyboardNowVisible != isKeyboardVisible) {
                            isKeyboardVisible = keyboardNowVisible;
                            
                            if (isKeyboardVisible) {
                                // Teclado visible: ocultar dock
                                if (floatingButtonView != null && floatingButtonView.getVisibility() == View.VISIBLE) {
                                    floatingButtonView.setVisibility(View.GONE);
                                    Log.d(TAG, "Teclado detectado (WindowInsets): Dock ocultado");
                                }
                            } else {
                                // Teclado oculto: mostrar dock (solo si hay apps)
                                if (floatingButtonView != null) {
                                    List<DockApp> dockApps = DockAppManager.getDockApps(BackgroundService.this);
                                    if (dockApps != null && !dockApps.isEmpty()) {
                                        floatingButtonView.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Teclado oculto (WindowInsets): Dock mostrado");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en detección de teclado con WindowInsets", e);
                    }
                    return insets;
                });
                
                Log.d(TAG, "Detección de teclado configurada con WindowInsets (API 30+)");
            } else {
                Log.w(TAG, "rootView es null, usando fallback");
                setupKeyboardDetectionWithViewTreeObserver();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar detección con WindowInsets", e);
            // Fallback al método anterior
            setupKeyboardDetectionWithViewTreeObserver();
        }
    }
    
    private void setupKeyboardDetectionWithViewTreeObserver() {
        try {
            // Crear una vista invisible que cubra toda la pantalla para detectar cambios
            keyboardDetectionView = new View(this);
            keyboardDetectionView.setBackgroundColor(0x00000000); // Completamente transparente
            
            // Obtener dimensiones de la pantalla
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            
            // Configurar parámetros para la vista de detección
            keyboardDetectionParams = new WindowManager.LayoutParams(
                screenWidth,
                screenHeight,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            );
            keyboardDetectionParams.gravity = Gravity.TOP | Gravity.START;
            keyboardDetectionParams.x = 0;
            keyboardDetectionParams.y = 0;
            
            // Agregar la vista de detección al WindowManager
            windowManager.addView(keyboardDetectionView, keyboardDetectionParams);
            
            // Crear listener para detectar cambios en el layout usando getWindowVisibleDisplayFrame
            keyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        if (keyboardDetectionView == null) return;
                        
                        Rect r = new Rect();
                        // Obtener el área visible de la ventana
                        keyboardDetectionView.getWindowVisibleDisplayFrame(r);
                        
                        // Obtener altura total de la pantalla
                        int screenHeight = keyboardDetectionView.getRootView().getHeight();
                        
                        // Calcular altura del teclado virtual
                        // Cuando el teclado aparece, r.bottom se reduce
                        int softKeyboardHeight = screenHeight - r.bottom;
                        
                        // Umbral: si la altura del teclado es mayor a 100px, consideramos que está visible
                        // Este es el método recomendado por Android
                        boolean keyboardNowVisible = softKeyboardHeight > 100;
                        
                        if (keyboardNowVisible != isKeyboardVisible) {
                            isKeyboardVisible = keyboardNowVisible;
                            
                            if (isKeyboardVisible) {
                                // Teclado visible: ocultar dock
                                if (floatingButtonView != null && floatingButtonView.getVisibility() == View.VISIBLE) {
                                    floatingButtonView.setVisibility(View.GONE);
                                    Log.d(TAG, "Teclado detectado (getWindowVisibleDisplayFrame): Dock ocultado (altura: " + softKeyboardHeight + "px)");
                                }
                            } else {
                                // Teclado oculto: mostrar dock (solo si hay apps)
                                if (floatingButtonView != null) {
                                    List<DockApp> dockApps = DockAppManager.getDockApps(BackgroundService.this);
                                    if (dockApps != null && !dockApps.isEmpty()) {
                                        floatingButtonView.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Teclado oculto (getWindowVisibleDisplayFrame): Dock mostrado");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error en detección de teclado", e);
                    }
                }
            };
            
            // Agregar listener a la vista de detección
            keyboardDetectionView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
            
            Log.d(TAG, "Detección de teclado configurada con ViewTreeObserver (getWindowVisibleDisplayFrame)");
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar detección de teclado con ViewTreeObserver", e);
        }
    }
    
    private void removeKeyboardDetection() {
        try {
            if (keyboardDetectionView != null) {
                if (keyboardListener != null) {
                    keyboardDetectionView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
                }
                windowManager.removeView(keyboardDetectionView);
                keyboardDetectionView = null;
                keyboardListener = null;
                Log.d(TAG, "Detección de teclado removida");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al remover detección de teclado", e);
        }
    }
    
    private void applyDockBackground() {
        if (iconContainer == null) return;
        
        try {
            // Obtener border radius configurado (en dp)
            int radiusDp = FloatingButtonConfig.getBorderRadius(this);
            
            // Convertir dp a píxeles
            float density = getResources().getDisplayMetrics().density;
            int radiusPx = (int) (radiusDp * density);
            
            // Obtener color y alpha configurados
            int bgColor = FloatingButtonConfig.getBackgroundColor(this);
            int bgAlpha = FloatingButtonConfig.getBackgroundAlpha(this);
            
            // Combinar color y alpha (ARGB)
            int finalColor = (bgAlpha << 24) | (bgColor & 0x00FFFFFF);
            
            // Crear GradientDrawable para el fondo
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(radiusPx);
            background.setColor(finalColor);
            
            // Aplicar fondo
            iconContainer.setBackground(background);
        } catch (Exception e) {
            Log.e(TAG, "Error al aplicar fondo del dock", e);
        }
    }
    
    private void createDockIcons() {
        if (iconContainer == null) return;
        
        // Limpiar iconos existentes
        iconContainer.removeAllViews();
        
        List<DockApp> dockApps = DockAppManager.getDockApps(this);
        if (dockApps == null || dockApps.isEmpty()) {
            return;
        }
        
        // Obtener tamaño de icono configurado (en dp)
        int iconSizeDp = FloatingButtonConfig.getIconSize(this);
        float density = getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        
        // Obtener gap configurado (en dp)
        int gapDp = FloatingButtonConfig.getIconGap(this);
        int gapPx = (int) (gapDp * density);
        
        // Obtener padding configurado (en dp)
        int paddingDp = FloatingButtonConfig.getIconPadding(this);
        int paddingPx = (int) (paddingDp * density);
        
        // Obtener color y alpha de iconos
        int iconColor = FloatingButtonConfig.getIconColor(this);
        int iconAlpha = FloatingButtonConfig.getIconAlpha(this);
        int finalIconColor = (iconAlpha << 24) | (iconColor & 0x00FFFFFF);
        
        for (int i = 0; i < dockApps.size(); i++) {
            DockApp dockApp = dockApps.get(i);
            
            // Crear ImageView para el icono
            ImageView iconView = new ImageView(this);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));
            
            // Aplicar padding interno al icono
            iconView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            
            // Aplicar gap (margen izquierdo excepto para el primer icono)
            if (i > 0) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) iconView.getLayoutParams();
                layoutParams.setMargins(gapPx, 0, 0, 0);
                iconView.setLayoutParams(layoutParams);
            }
            
            // Verificar si es icono nativo o Material
            String iconName = dockApp.getMaterialIconName();
            if ("native".equals(iconName)) {
                // Mostrar icono nativo del app
                try {
                    PackageManager pm = getPackageManager();
                    android.graphics.drawable.Drawable appIcon = pm.getApplicationIcon(dockApp.getPackageName());
                    iconView.setImageDrawable(appIcon);
                    iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } catch (Exception e) {
                    Log.e(TAG, "Error al obtener icono nativo para " + dockApp.getPackageName(), e);
                    // Si falla, usar icono por defecto
                    iconView.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.sym_def_app_icon));
                }
            } else {
                // Crear y aplicar icono Material
                MaterialIconDrawable iconDrawable = new MaterialIconDrawable(this);
                iconDrawable.setIcon(iconName);
                iconDrawable.setSize(iconSizePx);
                iconDrawable.setColor(finalIconColor);
                // Configurar bounds del drawable
                iconDrawable.setBounds(0, 0, iconSizePx, iconSizePx);
                iconView.setImageDrawable(iconDrawable);
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            
            // Configurar click listener
            final String packageName = dockApp.getPackageName();
            final String activityName = dockApp.getActivityName();
            final String actionId = dockApp.getActionId();
            final boolean isAction = dockApp.isAction();
            
            iconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAction && actionId != null) {
                        // Ejecutar acción del sistema
                        ActionExecutor.executeAction(BackgroundService.this, actionId);
                    } else {
                        // Abrir app normal
                        openApp(packageName, activityName);
                    }
                }
            });
            
            iconContainer.addView(iconView);
        }
    }
    
    private void openApp(String packageName) {
        openApp(packageName, null);
    }
    
    private void openApp(String packageName, String activityName) {
        try {
            PackageManager pm = getPackageManager();
            Intent launchIntent = null;
            
            // Si hay una activity específica, usarla
            if (activityName != null && !activityName.isEmpty()) {
                try {
                    launchIntent = new Intent();
                    launchIntent.setClassName(packageName, activityName);
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    // Verificar que la activity existe
                    if (launchIntent.resolveActivity(pm) == null) {
                        Log.w(TAG, "Activity no encontrada: " + activityName + ", intentando método alternativo");
                        launchIntent = null;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error al crear intent para activity específica: " + activityName, e);
                    launchIntent = null;
                }
            }
            
            // Si no hay activity específica o falló, usar el método estándar
            if (launchIntent == null) {
                launchIntent = pm.getLaunchIntentForPackage(packageName);
            }
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                Log.d(TAG, "App abierta: " + packageName + (activityName != null ? " (" + activityName + ")" : ""));
            } else {
                // Intentar abrir manualmente
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                } else {
                    // Abrir configuración de la app como último recurso
                    Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settingsIntent.setData(android.net.Uri.parse("package:" + packageName));
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingsIntent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir app: " + packageName, e);
        }
    }
    
    private void applyInitialPosition() {
        if (params == null) return;
        
        // Migrar valor antiguo si existe
        FloatingButtonConfig.migrateOldPositionMargin(this);
        
        String position = FloatingButtonConfig.getPosition(this);
        int marginXDp = FloatingButtonConfig.getPositionMarginX(this);
        int marginYDp = FloatingButtonConfig.getPositionMarginY(this);
        float density = getResources().getDisplayMetrics().density;
        int marginXPx = (int) (marginXDp * density);
        int marginYPx = (int) (marginYDp * density);
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // Resetear valores
        params.x = 0;
        params.y = 0;
        
        // Aplicar posición y margen según la configuración
        // Nota: Con Gravity.END, valores positivos de x se aplican desde el borde derecho
        // Con Gravity.BOTTOM, valores positivos de y se aplican desde el borde inferior
        switch (position) {
            case "top_left":
                params.gravity = Gravity.TOP | Gravity.START;
                params.x = marginXPx; // Margen horizontal desde borde izquierdo
                params.y = marginYPx; // Margen vertical desde borde superior
                break;
            case "top_center":
                params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                params.x = 0; // Centrado horizontalmente
                params.y = marginYPx; // Margen vertical desde borde superior
                break;
            case "top_right":
                params.gravity = Gravity.TOP | Gravity.END;
                params.x = marginXPx; // Margen horizontal desde borde derecho (positivo = desde la derecha)
                params.y = marginYPx; // Margen vertical desde borde superior
                break;
            case "center_left":
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                params.x = marginXPx; // Margen horizontal desde borde izquierdo
                params.y = 0; // Centrado verticalmente
                break;
            case "center_center":
                params.gravity = Gravity.CENTER;
                params.x = 0; // Centrado horizontalmente
                params.y = 0; // Centrado verticalmente
                break;
            case "center_right":
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                params.x = marginXPx; // Margen horizontal desde borde derecho
                params.y = 0; // Centrado verticalmente
                break;
            case "bottom_left":
                params.gravity = Gravity.BOTTOM | Gravity.START;
                params.x = marginXPx; // Margen horizontal desde borde izquierdo
                params.y = marginYPx; // Margen vertical desde borde inferior (positivo = desde abajo)
                break;
            case "bottom_center":
                params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                params.x = 0; // Centrado horizontalmente
                params.y = marginYPx; // Margen vertical desde borde inferior
                break;
            case "bottom_right":
            default:
                params.gravity = Gravity.BOTTOM | Gravity.END;
                params.x = marginXPx; // Margen horizontal desde borde derecho
                params.y = marginYPx; // Margen vertical desde borde inferior
                break;
        }
        
        Log.d(TAG, "Posición aplicada: " + position + ", margen X: " + marginXPx + "px (" + marginXDp + "dp), margen Y: " + marginYPx + "px (" + marginYDp + "dp), x=" + params.x + ", y=" + params.y);
    }
    
    private void updateDockIcons() {
        List<DockApp> dockApps = DockAppManager.getDockApps(this);
        if (dockApps == null || dockApps.isEmpty()) {
            // Ocultar el botón si no hay apps
            if (floatingButtonView != null) {
                floatingButtonView.setVisibility(View.GONE);
            }
            return;
        }
        
        // Mostrar el botón si hay apps
        if (floatingButtonView != null) {
            floatingButtonView.setVisibility(View.VISIBLE);
        }
        
        // Verificar si el número de iconos coincide
        int currentIconCount = iconContainer.getChildCount();
        if (currentIconCount != dockApps.size()) {
            // Recrear iconos si el número cambió
            applyDockBackground(); // Asegurar que el fondo esté actualizado
            createDockIcons();
            return;
        }
    }
    
    private void removeFloatingButton() {
        // Remover detección de teclado
        removeKeyboardDetection();
        
        if (floatingButtonView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingButtonView);
                floatingButtonView = null;
                iconContainer = null;
                params = null;
                Log.d(TAG, "Botón flotante removido");
            } catch (Exception e) {
                Log.e(TAG, "Error al remover botón flotante", e);
            }
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Background Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal para el servicio en background");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Servicio destruido");
        removeFloatingButton();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // Servicio no vinculado, retorna null
        return null;
    }
}


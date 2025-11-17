package com.joshepw.nexusfloatinghelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import static android.content.Context.RECEIVER_NOT_EXPORTED;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "BackgroundServiceChannel";
    
    // Constantes configurables para el comportamiento de ocultación
    
    private WindowManager windowManager;
    private View floatingButtonView;
    private WindowManager.LayoutParams params;
    private LinearLayout iconContainer;
    private FrameLayout dockContainer; // Contenedor principal que envuelve iconContainer
    private String hideDirection = ""; // Dirección de ocultación: "left", "right", "top", "bottom"
    
    // Variables para drag
    private boolean isDragging = false;
    private boolean wasDraggableEnabled = false; // Guardar estado anterior del toggle
    private float initialTouchX;
    private float initialTouchY;
    private int initialX;
    private int initialY;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private static final long LONG_PRESS_TIMEOUT = 500; // 500ms para long press
    
    // Variables para ocultar/mostrar dock
    private boolean isDockHidden = false;
    private Handler hideTimeoutHandler;
    private Runnable hideTimeoutRunnable;
    private int originalX;
    private int originalY;
    private int hiddenX;
    private int hiddenY;
    
    // BroadcastReceiver para actualizaciones en tiempo real
    private BroadcastReceiver configUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            
            if (floatingButtonView == null || iconContainer == null) return;
            
            // Si el dock está oculto y se va a actualizar la configuración, mostrarlo primero
            // (excepto para HIDE_DOCK_ACTION que es para ocultar)
            if (isDockHidden && !"HIDE_DOCK_ACTION".equals(action)) {
                // Mostrar el dock inmediatamente (sin animación) antes de aplicar cambios
                showDockImmediately();
            }
            
            switch (action) {
                case "UPDATE_DOCK_CONFIG":
                    // Actualizar todas las propiedades del dock
                    updateDockConfiguration();
                    break;
                case "UPDATE_ICON_SIZE":
                    updateIconSizes();
                    break;
                case "UPDATE_POSITION":
                    updateDockPosition();
                    break;
                case "UPDATE_BACKGROUND":
                    updateDockBackgroundAnimated();
                    break;
                case "UPDATE_ICONS":
                    updateDockIcons();
                    break;
                case "HIDE_DOCK_ACTION":
                    // Ocultar el dock
                    hideDock();
                    break;
            }
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");
        
        // Inicializar MaterialSymbolsMapper para cargar el JSON de iconos
        MaterialSymbolsMapper.initialize(this);
        
        createNotificationChannel();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Registrar BroadcastReceiver para actualizaciones
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_DOCK_CONFIG");
        filter.addAction("UPDATE_ICON_SIZE");
        filter.addAction("UPDATE_POSITION");
        filter.addAction("UPDATE_BACKGROUND");
        filter.addAction("UPDATE_ICONS");
        filter.addAction("HIDE_DOCK_ACTION");
        
        // Especificar que el receiver no es exportado (solo para uso interno de la app)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(configUpdateReceiver, filter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(configUpdateReceiver, filter);
        }
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
        
        // Lanzar app de inicio automático solo la primera vez (al arrancar el OS)
        launchAutoStartApp();
        
        // Retornar START_STICKY para que el servicio se reinicie si es terminado
        return START_STICKY;
    }
    
    private void showFloatingButton() {
        if (floatingButtonView != null) {
            // Si ya existe, removerlo primero para aplicar nuevas configuraciones
            removeFloatingButton();
        }
        
        try {
            // Crear contenedor principal (FrameLayout) que envuelve todo
            dockContainer = new FrameLayout(this);
            
            // Crear contenedor de iconos programáticamente
            iconContainer = new LinearLayout(this);
            iconContainer.setOrientation(LinearLayout.HORIZONTAL);
            iconContainer.setPadding(16, 16, 16, 16);
            
            // Aplicar fondo con border radius configurado
            applyDockBackground();
            
            // Crear todos los iconos del dock
            createDockIcons();
            
            // Agregar iconContainer al dockContainer
            dockContainer.addView(iconContainer);
            
            // Configurar parámetros de WindowManager
            params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // Permite posiciones fuera de la pantalla
                PixelFormat.TRANSLUCENT
            );
            
            floatingButtonView = dockContainer;
            
            // Inicializar estado de draggable
            wasDraggableEnabled = FloatingButtonConfig.isDockDraggable(this);
            
            // Aplicar posición inicial desde configuraciones
            applyInitialPosition();
            
            // Configurar drag si está habilitado
            setupDraggable();
            
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
            
            // Configurar comportamiento del dock (ocultar/mostrar)
            setupDockBehavior();
            
            Log.d(TAG, "Botón flotante mostrado");
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar botón flotante", e);
        }
    }
    private void setupDockBehavior() {
        if (floatingButtonView == null || iconContainer == null) return;
        
        String behavior = FloatingButtonConfig.getDockBehavior(this);
        
        // "fixed" se ha eliminado, ahora "hide_on_action" es el comportamiento por defecto
        // Ambos se comportan igual: el dock está visible y se puede ocultar con una acción
        if ("hide_on_action".equals(behavior)) {
            // Modo ocultar con acción: el dock está visible y se puede ocultar con una acción
            if (isDockHidden) {
                showDock();
            }
            cancelHideTimeout();
        } else if ("hide_after_time".equals(behavior)) {
            // Modo ocultar después de tiempo: iniciar timer si está visible
            if (isDockHidden) {
                showDock();
            } else {
                startHideTimeout();
            }
        }
        
        // Configurar click en la parte expuesta cuando está oculto
        setupHiddenDockClickListener();
    }
    
    private void startHideTimeout() {
        cancelHideTimeout();
        
        if (hideTimeoutHandler == null) {
            hideTimeoutHandler = new Handler(Looper.getMainLooper());
        }
        
        int timeoutMs = FloatingButtonConfig.getDockHideTimeout(this);
        hideTimeoutRunnable = () -> {
            if (!isDockHidden && "hide_after_time".equals(FloatingButtonConfig.getDockBehavior(this))) {
                hideDock();
            }
        };
        hideTimeoutHandler.postDelayed(hideTimeoutRunnable, timeoutMs);
    }
    
    private void cancelHideTimeout() {
        if (hideTimeoutHandler != null && hideTimeoutRunnable != null) {
            hideTimeoutHandler.removeCallbacks(hideTimeoutRunnable);
            hideTimeoutRunnable = null;
        }
    }
    
    private void resetHideTimeout() {
        // Reiniciar timer cuando se hace click en un icono
        String behavior = FloatingButtonConfig.getDockBehavior(this);
        if ("hide_after_time".equals(behavior) && !isDockHidden) {
            startHideTimeout();
        }
    }
    
    private void hideDock() {
        if (isDockHidden || floatingButtonView == null || params == null || iconContainer == null) return;
        
        try {
            isDockHidden = true;
            
            // Guardar posición original
            originalX = params.x;
            originalY = params.y;
            
            // Calcular posición oculta (hacia el borde más cercano)
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            
            float density = getResources().getDisplayMetrics().density;
            
            // Calcular tamaño expuesto basado en el tamaño del icono (icono * 0.5)
            int iconSizeDp = FloatingButtonConfig.getIconSize(this);
            int exposedSizeDp = (int) (iconSizeDp * 0.5);
            int exposedSizePx = (int) (exposedSizeDp * density);
            
            // Obtener dimensiones del dock
            // Medir el contenedor completo (dockContainer) que incluye el iconContainer
            if (dockContainer != null) {
                dockContainer.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
            }
            
            floatingButtonView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            
            int dockWidth = floatingButtonView.getMeasuredWidth();
            int dockHeight = floatingButtonView.getMeasuredHeight();
            
            // Si las dimensiones son 0, intentar obtenerlas del layout
            if (dockWidth == 0 || dockHeight == 0) {
                dockWidth = floatingButtonView.getWidth();
                dockHeight = floatingButtonView.getHeight();
            }
            
            // Si aún son 0, usar dimensiones del iconContainer como fallback
            if ((dockWidth == 0 || dockHeight == 0) && iconContainer != null) {
                iconContainer.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                dockWidth = iconContainer.getMeasuredWidth();
                dockHeight = iconContainer.getMeasuredHeight();
            }
            
            Log.d(TAG, "hideDock - Dimensiones calculadas: dockWidth=" + dockWidth + ", dockHeight=" + dockHeight);
            
            // Determinar hacia qué dirección ocultar
            boolean isDraggable = FloatingButtonConfig.isDockDraggable(this);
            
            if (isDraggable) {
                // Si el dock es draggable, ocultar hacia el lado de la pantalla más cercano
                // Calcular distancias a cada borde
                int dockCenterX = originalX + (dockWidth / 2);
                int dockCenterY = originalY + (dockHeight / 2);
                
                int distanceToLeft = dockCenterX;
                int distanceToRight = screenWidth - dockCenterX;
                int distanceToTop = dockCenterY;
                int distanceToBottom = screenHeight - dockCenterY;
                
                // Encontrar la distancia mínima
                int minDistance = Math.min(Math.min(distanceToLeft, distanceToRight), 
                                          Math.min(distanceToTop, distanceToBottom));
                
                // Determinar hacia qué dirección ocultar basado en la distancia mínima
                if (minDistance == distanceToLeft) {
                    // Más cerca del borde izquierdo → ocultar hacia la izquierda
                    hiddenX = -(dockWidth - exposedSizePx);
                    hiddenY = originalY;
                } else if (minDistance == distanceToRight) {
                    // Más cerca del borde derecho → ocultar hacia la derecha
                    // Queremos que solo se vean exposedSizePx desde el borde derecho de la pantalla
                    // El borde derecho del dock debe estar en: screenWidth + exposedSizePx
                    // El borde izquierdo del dock (hiddenX) debe estar en: screenWidth + exposedSizePx - dockWidth
                    hiddenX = screenWidth - exposedSizePx;
                    hiddenY = originalY;
                    Log.d(TAG, "hideDock - Ocultando hacia derecha: screenWidth=" + screenWidth + ", exposedSizePx=" + exposedSizePx + ", dockWidth=" + dockWidth + ", hiddenX=" + hiddenX + ", bordeDerecho=" + (hiddenX + dockWidth) + ", visible=" + (screenWidth - hiddenX));
                } else if (minDistance == distanceToTop) {
                    // Más cerca del borde superior → ocultar hacia arriba
                    hiddenY = -(dockHeight - exposedSizePx);
                    hiddenX = originalX;
                } else {
                    // Más cerca del borde inferior → ocultar hacia abajo
                    hiddenY = screenHeight + exposedSizePx - dockHeight;
                    hiddenX = originalX;
                }
            } else {
                // Si no es draggable, usar la posición inicial configurada
                String position = FloatingButtonConfig.getPosition(this);
                
                // Determinar dirección de ocultación según la posición configurada
                if (position.contains("right")) {
                    // Posiciones con "right": top_right, center_right, bottom_right
                    // Ocultar hacia la derecha
                    hiddenX = screenWidth - exposedSizePx;
                    hiddenY = originalY;
                    Log.d(TAG, "hideDock - Ocultando hacia derecha (config): screenWidth=" + screenWidth + ", exposedSizePx=" + exposedSizePx + ", dockWidth=" + dockWidth + ", hiddenX=" + hiddenX + ", bordeDerecho=" + (hiddenX + dockWidth) + ", visible=" + (screenWidth - hiddenX));
                } else if (position.contains("left")) {
                    // Posiciones con "left": top_left, center_left, bottom_left
                    // Ocultar hacia la izquierda
                    hiddenX = -(dockWidth - exposedSizePx);
                    hiddenY = originalY;
                } else if (position.contains("center")) {
                    // Posiciones con "center": top_center, bottom_center
                    // Ocultar según la dirección vertical
                    if (position.contains("top")) {
                        // top_center → ocultar hacia arriba
                        hiddenY = -(dockHeight - exposedSizePx);
                        hiddenX = originalX;
                    } else if (position.contains("bottom")) {
                        // bottom_center → ocultar hacia abajo
                        hiddenY = screenHeight + exposedSizePx - dockHeight;
                        hiddenX = originalX;
                    } else {
                        // Fallback para center (no debería ocurrir, pero por seguridad)
                        hiddenX = screenWidth + exposedSizePx - dockWidth;
                        hiddenY = originalY;
                    }
                } else {
                    // Fallback: ocultar hacia la derecha por defecto
                    hiddenX = screenWidth + exposedSizePx - dockWidth;
                    hiddenY = originalY;
                }
            }
            
            // Determinar dirección de ocultación para posicionar el indicador
            if (hiddenX < originalX) {
                hideDirection = "left";
            } else if (hiddenX > originalX) {
                hideDirection = "right";
            } else if (hiddenY < originalY) {
                hideDirection = "top";
            } else {
                hideDirection = "bottom";
            }
            
            // Mostrar en consola las coordenadas donde se ocultará el dock
            Log.d(TAG, "Ocultando dock - Posición original: X=" + originalX + ", Y=" + originalY);
            Log.d(TAG, "Ocultando dock - Coordenadas ocultas: hiddenX=" + hiddenX + ", hiddenY=" + hiddenY);
            Log.d(TAG, "Ocultando dock - Dimensiones: dockWidth=" + dockWidth + ", dockHeight=" + dockHeight + ", exposedSizePx=" + exposedSizePx);
            Log.d(TAG, "Ocultando dock - Pantalla: screenWidth=" + screenWidth + ", screenHeight=" + screenHeight);
            Log.d(TAG, "Ocultando dock - Dirección: " + hideDirection);
            
            // Ocultar iconos (transparencia total)
            hideIcons();
            
            // Mostrar contorno blanco cuando está oculto
            updateDockHiddenBorder(true);
            
            // Habilitar click en la parte expuesta
            enableHiddenDockClick();
            
            // Animar posición (sin cambiar border radius)
            animateHidePosition();
            
        } catch (Exception e) {
            Log.e(TAG, "Error al ocultar dock", e);
            isDockHidden = false;
        }
    }
    
    private void showDock() {
        if (!isDockHidden || floatingButtonView == null || params == null || iconContainer == null) return;
        
        try {
            isDockHidden = false;
            cancelHideTimeout();
            
            // Deshabilitar click en parte expuesta
            disableHiddenDockClick();
            
            // Ocultar contorno blanco
            updateDockHiddenBorder(false);
            
            // Animar posición de vuelta
            animateShowPosition(() -> {
                // Después de animar posición, mostrar iconos (sin cambiar border radius)
                showIcons();
                // Reiniciar timer si está configurado
                startHideTimeout();
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar dock", e);
            isDockHidden = false;
        }
    }
    
    private void showDockImmediately() {
        // Versión sin animación para cuando se necesita mostrar el dock antes de aplicar cambios
        if (!isDockHidden || floatingButtonView == null || params == null || iconContainer == null) return;
        
        try {
            isDockHidden = false;
            cancelHideTimeout();
            
            // Deshabilitar click en parte expuesta
            disableHiddenDockClick();
            
            // Ocultar contorno blanco
            updateDockHiddenBorder(false);
            
            // Restaurar posición inmediatamente (sin animación)
            params.x = originalX;
            params.y = originalY;
            
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición durante showDockImmediately", e);
            }
            
            // Mostrar iconos inmediatamente (sin cambiar border radius)
            showIconsImmediately();
            
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar dock inmediatamente", e);
            isDockHidden = false;
        }
    }
    
    private void showIconsImmediately() {
        if (iconContainer == null) return;
        
        for (int i = 0; i < iconContainer.getChildCount(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                child.setAlpha(1f);
            }
        }
    }
    
    private void animateHidePosition() {
        if (params == null || floatingButtonView == null) return;
        
        ValueAnimator animator = ValueAnimator.ofInt(0, 1);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        
        final int startX = params.x;
        final int startY = params.y;
        final int deltaX = hiddenX - startX;
        final int deltaY = hiddenY - startY;
        
        animator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();
            params.x = startX + (int) (deltaX * progress);
            params.y = startY + (int) (deltaY * progress);
            
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición durante animación", e);
            }
        });
        
        animator.start();
    }
    
    private void animateShowPosition(Runnable onComplete) {
        if (params == null || floatingButtonView == null) return;
        
        ValueAnimator animator = ValueAnimator.ofInt(0, 1);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        
        final int startX = params.x;
        final int startY = params.y;
        final int deltaX = originalX - startX;
        final int deltaY = originalY - startY;
        
        animator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();
            params.x = startX + (int) (deltaX * progress);
            params.y = startY + (int) (deltaY * progress);
            
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición durante animación", e);
            }
        });
        
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        animator.start();
    }
    
    private void animateBorderRadius(int targetRadiusPx, Runnable onComplete) {
        if (iconContainer == null) return;
        
        android.graphics.drawable.Drawable currentBackground = iconContainer.getBackground();
        if (!(currentBackground instanceof GradientDrawable)) return;
        
        GradientDrawable background = (GradientDrawable) currentBackground;
        float currentRadius = background.getCornerRadius();
        
        ValueAnimator animator = ValueAnimator.ofFloat(currentRadius, targetRadiusPx);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float radius = (Float) animation.getAnimatedValue();
            background.setCornerRadius(radius);
            iconContainer.invalidate();
        });
        
        if (onComplete != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onComplete.run();
                }
            });
        }
        
        animator.start();
    }
    
    private void hideIcons() {
        if (iconContainer == null) return;
        
        for (int i = 0; i < iconContainer.getChildCount(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                child.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .start();
            }
        }
    }
    
    private void showIcons() {
        if (iconContainer == null) return;
        
        for (int i = 0; i < iconContainer.getChildCount(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                child.setAlpha(0f);
                child.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            }
        }
    }
    
    private void setupHiddenDockClickListener() {
        if (floatingButtonView == null) return;
        
        // El click listener solo debe funcionar cuando está oculto
        // Se configurará dinámicamente en hideDock() y showDock()
    }
    
    private void enableHiddenDockClick() {
        if (floatingButtonView == null) return;
        
        floatingButtonView.setOnClickListener(v -> {
            if (isDockHidden) {
                showDock();
            }
        });
    }
    
    private void disableHiddenDockClick() {
        if (floatingButtonView == null) return;
        
        // No remover el listener si el dock es draggable, ya que setupDraggable() lo maneja
        if (!FloatingButtonConfig.isDockDraggable(this)) {
            floatingButtonView.setOnClickListener(null);
        }
    }
    
    private void updateDraggableState() {
        // Actualizar estado de draggable cuando cambia la configuración
        if (floatingButtonView == null || params == null) {
            wasDraggableEnabled = FloatingButtonConfig.isDockDraggable(this);
            setupDraggable();
            return;
        }
        
        boolean isNowDraggable = FloatingButtonConfig.isDockDraggable(this);
        
        // Si se deshabilitó el draggable, resetear posición a la configuración inicial
        if (wasDraggableEnabled && !isNowDraggable) {
            resetPositionToInitialSettings();
        }
        
        // Guardar el nuevo estado
        wasDraggableEnabled = isNowDraggable;
        
        // El borde naranja solo aparece durante el drag, no cuando solo está habilitado
        // Por lo tanto, no mostramos el borde aquí, solo configuramos el drag
        setupDraggable();
    }
    
    private void resetPositionToInitialSettings() {
        if (params == null || iconContainer == null) return;
        
        try {
            // Obtener configuración de posición y márgenes
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
            
            // Obtener el ancho y alto del dock
            int dockWidth = 0;
            int dockHeight = 0;
            if (floatingButtonView != null) {
                floatingButtonView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                dockWidth = floatingButtonView.getMeasuredWidth();
                dockHeight = floatingButtonView.getMeasuredHeight();
            }
            
            // Calcular posición absoluta basada en la posición predefinida
            int absoluteX = 0;
            int absoluteY = 0;
            
            switch (position) {
                case "top_left":
                    absoluteX = marginXPx;
                    absoluteY = marginYPx;
                    break;
                case "top_center":
                    absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0);
                    absoluteY = marginYPx;
                    break;
                case "top_right":
                    absoluteX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0);
                    absoluteY = marginYPx;
                    break;
                case "center_left":
                    absoluteX = marginXPx;
                    absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0);
                    break;
                case "center_right":
                    absoluteX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0);
                    absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0);
                    break;
                case "bottom_left":
                    absoluteX = marginXPx;
                    absoluteY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0);
                    break;
                case "bottom_center":
                    absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0);
                    absoluteY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0);
                    break;
                case "bottom_right":
                default:
                    absoluteX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0);
                    absoluteY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0);
                    break;
            }
            
            // Cambiar a modo no-draggable (usar sistema de posiciones predefinidas)
            // Resetear valores
            params.x = 0;
            params.y = 0;
            
            // Aplicar posición y margen según la configuración (sistema predefinido)
            switch (position) {
                case "top_left":
                    params.gravity = Gravity.TOP | Gravity.START;
                    params.x = marginXPx;
                    params.y = marginYPx;
                    break;
                case "top_center":
                    params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    params.x = 0;
                    params.y = marginYPx;
                    break;
                case "top_right":
                    params.gravity = Gravity.TOP | Gravity.END;
                    params.x = marginXPx;
                    params.y = marginYPx;
                    break;
                case "center_left":
                    params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    params.x = marginXPx;
                    params.y = 0;
                    break;
                case "center_right":
                    params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                    params.x = marginXPx;
                    params.y = 0;
                    break;
                case "bottom_left":
                    params.gravity = Gravity.BOTTOM | Gravity.START;
                    params.x = marginXPx;
                    params.y = marginYPx;
                    break;
                case "bottom_center":
                    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    params.x = 0;
                    params.y = marginYPx;
                    break;
                case "bottom_right":
                default:
                    params.gravity = Gravity.BOTTOM | Gravity.END;
                    params.x = marginXPx;
                    params.y = marginYPx;
                    break;
            }
            
            // Actualizar la vista
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición al deshabilitar draggable", e);
            }
            
            // Los márgenes ya están en la configuración, no necesitamos resetearlos
            Log.d(TAG, "Posición reseteada a configuración inicial: " + position + ", margen X: " + marginXPx + "px (" + marginXDp + "dp), margen Y: " + marginYPx + "px (" + marginYDp + "dp)");
        } catch (Exception e) {
            Log.e(TAG, "Error al resetear posición a configuración inicial", e);
        }
    }
    
    private void setupDraggable() {
        if (floatingButtonView == null) return;
        
        boolean isDraggable = FloatingButtonConfig.isDockDraggable(this);
        if (!isDraggable) {
            floatingButtonView.setOnTouchListener(null);
            // Asegurar que el borde naranja esté oculto
            updateDockBorder(false);
            return;
        }
        
        if (longPressHandler == null) {
            longPressHandler = new Handler(android.os.Looper.getMainLooper());
        }
        
        floatingButtonView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (!FloatingButtonConfig.isDockDraggable(BackgroundService.this)) {
                    return false;
                }
                
                // No permitir drag si el dock está oculto
                if (isDockHidden) {
                    return false;
                }
                
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // Guardar posición inicial del touch
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        
                        // Guardar posición actual del dock antes de empezar a arrastrar
                        if (params != null) {
                            initialX = params.x;
                            initialY = params.y;
                        }
                        
                        // Iniciar timer para long press
                        longPressRunnable = () -> {
                            if (!isDragging) {
                                isDragging = true;
                                updateDockBorder(true); // Mostrar borde naranja
                                // Resetear timer de ocultación si está configurado "hide_after_time"
                                resetHideTimeout();
                                Log.d(TAG, "Long press detectado, iniciando drag");
                            }
                        };
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                        return true;
                        
                    case android.view.MotionEvent.ACTION_MOVE:
                        if (isDragging || shouldStartDragging(event)) {
                            if (!isDragging) {
                                isDragging = true;
                                updateDockBorder(true); // Mostrar borde naranja
                                // Resetear timer de ocultación si está configurado "hide_after_time"
                                resetHideTimeout();
                                longPressHandler.removeCallbacks(longPressRunnable);
                            }
                            
                            // Si ya está arrastrando, resetear el timer continuamente
                            if (isDragging) {
                                resetHideTimeout();
                            }
                            
                            // Calcular nueva posición
                            float deltaX = event.getRawX() - initialTouchX;
                            float deltaY = event.getRawY() - initialTouchY;
                            
                            if (params != null) {
                                params.x = initialX + (int) deltaX;
                                params.y = initialY + (int) deltaY;
                                
                                try {
                                    windowManager.updateViewLayout(floatingButtonView, params);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al actualizar posición durante drag", e);
                                }
                            }
                            return true;
                        }
                        return false;
                        
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        
                        if (isDragging) {
                            isDragging = false;
                            updateDockBorder(false); // Ocultar borde naranja
                            // Reiniciar timer de ocultación después de terminar el drag
                            resetHideTimeout();
                            Log.d(TAG, "Drag finalizado, posición guardada");
                        }
                        return isDragging;
                }
                return false;
            }
            
            private boolean shouldStartDragging(android.view.MotionEvent event) {
                // Si el usuario se ha movido significativamente, iniciar drag
                float deltaX = Math.abs(event.getRawX() - initialTouchX);
                float deltaY = Math.abs(event.getRawY() - initialTouchY);
                float threshold = 20; // 20 píxeles de umbral
                return deltaX > threshold || deltaY > threshold;
            }
        });
    }
    
    private void setupIconDragListener(ImageView iconView, String packageName, String activityName, String actionId, boolean isAction) {
        if (longPressHandler == null) {
            longPressHandler = new Handler(Looper.getMainLooper());
        }
        
        final long ICON_LONG_PRESS_TIMEOUT = 1000; // 1 segundo para iconos
        final Runnable[] iconLongPressRunnable = {null};
        final boolean[] longPressActivated = {false}; // Indica si ya pasó el segundo
        final float[] iconInitialTouchX = {0};
        final float[] iconInitialTouchY = {0};
        final boolean[] isCurrentlyDragging = {false};
        final boolean[] touchStarted = {false};
        
        iconView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (!FloatingButtonConfig.isDockDraggable(BackgroundService.this)) {
                    return false;
                }
                
                // No permitir drag si el dock está oculto
                if (isDockHidden) {
                    return false;
                }
                
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        touchStarted[0] = true;
                        iconInitialTouchX[0] = event.getRawX();
                        iconInitialTouchY[0] = event.getRawY();
                        longPressActivated[0] = false;
                        isCurrentlyDragging[0] = false;
                        
                        // Guardar posición actual del dock antes de empezar a arrastrar
                        if (params != null) {
                            initialX = params.x;
                            initialY = params.y;
                        }
                        
                        // Iniciar timer para long press (1 segundo para iconos)
                        iconLongPressRunnable[0] = () -> {
                            // Solo activar drag si aún se está presionando
                            if (touchStarted[0] && !longPressActivated[0]) {
                                longPressActivated[0] = true;
                                isDragging = true;
                                updateDockBorder(true); // Mostrar borde naranja
                                // Resetear timer de ocultación si está configurado "hide_after_time"
                                resetHideTimeout();
                                Log.d(TAG, "Long press en icono detectado (1 segundo), drag habilitado");
                            }
                        };
                        longPressHandler.postDelayed(iconLongPressRunnable[0], ICON_LONG_PRESS_TIMEOUT);
                        return true; // Interceptar para controlar el comportamiento
                        
                    case android.view.MotionEvent.ACTION_MOVE:
                        // Solo permitir drag si ya pasó el segundo (long press activado)
                        if (longPressActivated[0]) {
                            isCurrentlyDragging[0] = true;
                            
                            // Si ya está arrastrando, resetear el timer continuamente
                            resetHideTimeout();
                            
                            // Calcular nueva posición
                            float moveDeltaX = event.getRawX() - iconInitialTouchX[0];
                            float moveDeltaY = event.getRawY() - iconInitialTouchY[0];
                            
                            if (params != null) {
                                params.x = initialX + (int) moveDeltaX;
                                params.y = initialY + (int) moveDeltaY;
                                
                                try {
                                    windowManager.updateViewLayout(floatingButtonView, params);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al actualizar posición durante drag desde icono", e);
                                }
                            }
                            return true; // Consumir el evento cuando se está haciendo drag
                        }
                        // Si no pasó el segundo, no hacer nada pero seguir interceptando
                        return true;
                        
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        touchStarted[0] = false;
                        // Cancelar el timer si aún no se completó
                        longPressHandler.removeCallbacks(iconLongPressRunnable[0]);
                        
                        if (isCurrentlyDragging[0]) {
                            // Hubo drag (se mantuvo presionado más de 1 segundo y se movió)
                            Log.d(TAG, "Drag desde icono finalizado, posición guardada");
                            isDragging = false;
                            updateDockBorder(false); // Ocultar borde naranja
                            // Reiniciar timer de ocultación después de terminar el drag
                            resetHideTimeout();
                            longPressActivated[0] = false;
                            isCurrentlyDragging[0] = false;
                            return true; // Consumir el evento para evitar click
                        } else {
                            // No pasó el segundo o no hubo movimiento, ejecutar click normal
                            isDragging = false;
                            longPressActivated[0] = false;
                            
                            // Ejecutar click con un pequeño delay para asegurar que el evento termine
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                handleIconClick(isAction, actionId, packageName, activityName);
                            }, 50);
                            return true; // Consumir el evento
                        }
                }
                return true;
            }
        });
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
            
            // El borde naranja solo se agrega cuando se está haciendo drag, no por defecto
            // Se agregará dinámicamente mediante updateDockBorder() cuando inicie el drag
            
            // Aplicar fondo
            iconContainer.setBackground(background);
        } catch (Exception e) {
            Log.e(TAG, "Error al aplicar fondo del dock", e);
        }
    }
    
    private void updateDockBorder(boolean showBorder) {
        if (iconContainer == null) return;
        
        try {
            android.graphics.drawable.Drawable currentBackground = iconContainer.getBackground();
            if (currentBackground instanceof GradientDrawable) {
                GradientDrawable background = (GradientDrawable) currentBackground;
                
                if (showBorder) {
                    // Agregar o actualizar borde naranja de 3dp
                    float density = getResources().getDisplayMetrics().density;
                    int borderWidthDp = 3;
                    int borderWidthPx = (int) (borderWidthDp * density);
                    int orangeColor = 0xFFFF9800; // Naranja en ARGB
                    background.setStroke(borderWidthPx, orangeColor);
                    
                    // Resetear timer de ocultación cuando se muestra el borde naranja
                    // (indica que el dock está siendo arrastrado o está listo para arrastrarse)
                    resetHideTimeout();
                } else {
                    // Remover borde
                    background.setStroke(0, 0);
                }
                
                // Forzar redibujado
                iconContainer.invalidate();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar borde del dock", e);
        }
    }
    
    private void updateDockHiddenBorder(boolean showBorder) {
        if (iconContainer == null) return;
        
        try {
            android.graphics.drawable.Drawable currentBackground = iconContainer.getBackground();
            
            if (showBorder) {
                // Crear borde externo usando LayerDrawable
                float density = getResources().getDisplayMetrics().density;
                int borderWidthDp = 5;
                int borderWidthPx = (int) (borderWidthDp * density);
                final int targetAlpha = 150; // Alpha objetivo (0x96 = 150 en decimal)
                
                // Obtener el fondo original
                GradientDrawable originalBackground = null;
                if (currentBackground instanceof GradientDrawable) {
                    originalBackground = (GradientDrawable) currentBackground;
                } else if (currentBackground instanceof android.graphics.drawable.LayerDrawable) {
                    // Si ya es un LayerDrawable, obtener la capa interna
                    android.graphics.drawable.LayerDrawable layerDrawable = (android.graphics.drawable.LayerDrawable) currentBackground;
                    android.graphics.drawable.Drawable innerDrawable = layerDrawable.getDrawable(1);
                    if (innerDrawable instanceof GradientDrawable) {
                        originalBackground = (GradientDrawable) innerDrawable;
                    }
                }
                
                if (originalBackground == null) {
                    // Si no hay fondo original, crear uno básico
                    originalBackground = new GradientDrawable();
                    originalBackground.setShape(GradientDrawable.RECTANGLE);
                    int radiusDp = FloatingButtonConfig.getBorderRadius(this);
                    int radiusPx = (int) (radiusDp * density);
                    originalBackground.setCornerRadius(radiusPx);
                    int bgColor = FloatingButtonConfig.getBackgroundColor(this);
                    int bgAlpha = FloatingButtonConfig.getBackgroundAlpha(this);
                    int finalColor = (bgAlpha << 24) | (bgColor & 0x00FFFFFF);
                    originalBackground.setColor(finalColor);
                }
                
                // Crear capa externa con el stroke (borde blanco) - inicialmente transparente
                final GradientDrawable strokeLayer = new GradientDrawable();
                strokeLayer.setShape(GradientDrawable.RECTANGLE);
                strokeLayer.setCornerRadius(originalBackground.getCornerRadius());
                strokeLayer.setColor(0x00000000); // Transparente
                strokeLayer.setStroke(borderWidthPx, 0x00FFFFFF); // Inicialmente transparente
                
                // Crear LayerDrawable con stroke externo y fondo interno
                android.graphics.drawable.Drawable[] layers = new android.graphics.drawable.Drawable[2];
                layers[0] = strokeLayer; // Capa externa (stroke)
                layers[1] = originalBackground; // Capa interna (fondo)
                
                final android.graphics.drawable.LayerDrawable layerDrawable = new android.graphics.drawable.LayerDrawable(layers);
                // Ajustar el inset de la capa interna para que el stroke sea externo
                layerDrawable.setLayerInset(1, borderWidthPx, borderWidthPx, borderWidthPx, borderWidthPx);
                
                iconContainer.setBackground(layerDrawable);
                
                // Animar alpha del contorno de 0 a targetAlpha
                ValueAnimator alphaAnimator = ValueAnimator.ofInt(0, targetAlpha);
                alphaAnimator.setDuration(300);
                alphaAnimator.setInterpolator(new DecelerateInterpolator());
                alphaAnimator.addUpdateListener(animation -> {
                    int alpha = (Integer) animation.getAnimatedValue();
                    int whiteColor = (alpha << 24) | 0x00FFFFFF; // Blanco con alpha animado
                    strokeLayer.setStroke(borderWidthPx, whiteColor);
                    iconContainer.invalidate();
                });
                alphaAnimator.start();
            } else {
                // Remover borde blanco - restaurar fondo original con animación
                GradientDrawable originalBackground = null;
                GradientDrawable strokeLayer = null;
                
                if (currentBackground instanceof android.graphics.drawable.LayerDrawable) {
                    // Si es un LayerDrawable, obtener ambas capas
                    android.graphics.drawable.LayerDrawable layerDrawable = (android.graphics.drawable.LayerDrawable) currentBackground;
                    android.graphics.drawable.Drawable outerDrawable = layerDrawable.getDrawable(0);
                    android.graphics.drawable.Drawable innerDrawable = layerDrawable.getDrawable(1);
                    if (outerDrawable instanceof GradientDrawable) {
                        strokeLayer = (GradientDrawable) outerDrawable;
                    }
                    if (innerDrawable instanceof GradientDrawable) {
                        originalBackground = (GradientDrawable) innerDrawable;
                    }
                } else if (currentBackground instanceof GradientDrawable) {
                    originalBackground = (GradientDrawable) currentBackground;
                }
                
                if (strokeLayer != null && originalBackground != null) {
                    // Animar alpha del contorno de targetAlpha a 0
                    final int currentAlpha = 150; // Alpha actual
                    final GradientDrawable finalStrokeLayer = strokeLayer;
                    final GradientDrawable finalOriginalBackground = originalBackground;
                    
                    ValueAnimator alphaAnimator = ValueAnimator.ofInt(currentAlpha, 0);
                    alphaAnimator.setDuration(300);
                    alphaAnimator.setInterpolator(new DecelerateInterpolator());
                    alphaAnimator.addUpdateListener(animation -> {
                        int alpha = (Integer) animation.getAnimatedValue();
                        int whiteColor = (alpha << 24) | 0x00FFFFFF; // Blanco con alpha animado
                        float density = getResources().getDisplayMetrics().density;
                        int borderWidthPx = (int) (5 * density);
                        finalStrokeLayer.setStroke(borderWidthPx, whiteColor);
                        iconContainer.invalidate();
                    });
                    alphaAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Después de la animación, restaurar fondo original
                            if (finalOriginalBackground != null) {
                                // Si está en modo drag, restaurar el borde naranja
                                if (isDragging) {
                                    float density = getResources().getDisplayMetrics().density;
                                    int borderWidthDp = 3;
                                    int borderWidthPx = (int) (borderWidthDp * density);
                                    int orangeColor = 0xFFFF9800; // Naranja en ARGB
                                    finalOriginalBackground.setStroke(borderWidthPx, orangeColor);
                                } else {
                                    finalOriginalBackground.setStroke(0, 0);
                                }
                                iconContainer.setBackground(finalOriginalBackground);
                                iconContainer.invalidate();
                            }
                        }
                    });
                    alphaAnimator.start();
                } else if (originalBackground != null) {
                    // Si no hay strokeLayer, restaurar directamente
                    if (isDragging) {
                        float density = getResources().getDisplayMetrics().density;
                        int borderWidthDp = 3;
                        int borderWidthPx = (int) (borderWidthDp * density);
                        int orangeColor = 0xFFFF9800; // Naranja en ARGB
                        originalBackground.setStroke(borderWidthPx, orangeColor);
                    } else {
                        originalBackground.setStroke(0, 0);
                    }
                    iconContainer.setBackground(originalBackground);
                    iconContainer.invalidate();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar borde del dock oculto", e);
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
            
            // Configurar click y drag listener
            final String packageName = dockApp.getPackageName();
            final String activityName = dockApp.getActivityName();
            final String actionId = dockApp.getActionId();
            final boolean isAction = dockApp.isAction();
            
            // Configurar listener según si el dock es draggable
            if (FloatingButtonConfig.isDockDraggable(this)) {
                // Si es draggable, usar OnTouchListener para permitir drag desde iconos
                setupIconDragListener(iconView, packageName, activityName, actionId, isAction);
            } else {
                // Si no es draggable, usar OnClickListener normal
                iconView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleIconClick(isAction, actionId, packageName, activityName);
                    }
                });
            }
            
            iconContainer.addView(iconView);
        }
    }
    
    private void launchAutoStartApp() {
        try {
            // Verificar si ya se lanzó en esta sesión
            if (FloatingButtonConfig.isAutoStartLaunched(this)) {
                Log.d(TAG, "App de inicio automático ya fue lanzada en esta sesión");
                return;
            }
            
            // Obtener app configurada para inicio automático
            String packageName = FloatingButtonConfig.getAutoStartPackage(this);
            String activityName = FloatingButtonConfig.getAutoStartActivity(this);
            
            if (packageName == null || packageName.isEmpty()) {
                return; // No hay app configurada
            }
            
            Log.d(TAG, "Lanzando app de inicio automático: " + packageName + (activityName != null ? " (" + activityName + ")" : ""));
            
            // Lanzar la app con un pequeño delay para asegurar que el servicio esté completamente iniciado
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    openApp(packageName, activityName);
                    // Marcar como lanzada
                    FloatingButtonConfig.setAutoStartLaunched(this, true);
                    Log.d(TAG, "App de inicio automático lanzada exitosamente");
                } catch (Exception e) {
                    Log.e(TAG, "Error al lanzar app de inicio automático", e);
                }
            }, 2000); // Delay de 2 segundos para dar tiempo al sistema
            
        } catch (Exception e) {
            Log.e(TAG, "Error en launchAutoStartApp", e);
        }
    }
    
    private void openApp(String packageName) {
        openApp(packageName, null);
    }
    
    private void handleIconClick(boolean isAction, String actionId, String packageName, String activityName) {
        // Si el dock está oculto, solo mostrarlo, NO ejecutar la acción
        if (isDockHidden) {
            showDock();
            return; // No ejecutar la acción cuando está oculto
        }
        
        // Si está visible, ejecutar la acción normalmente
        executeIconAction(isAction, actionId, packageName, activityName);
    }
    
    private void executeIconAction(boolean isAction, String actionId, String packageName, String activityName) {
        // Reiniciar timer de ocultar si está configurado
        resetHideTimeout();
        
        if (isAction && actionId != null) {
            // Ejecutar acción del sistema
            ActionExecutor.executeAction(this, actionId);
        } else {
            // Abrir app normal
            openApp(packageName, activityName);
        }
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
        
        boolean isDraggable = FloatingButtonConfig.isDockDraggable(this);
        int marginXDp = FloatingButtonConfig.getPositionMarginX(this);
        int marginYDp = FloatingButtonConfig.getPositionMarginY(this);
        float density = getResources().getDisplayMetrics().density;
        int marginXPx = (int) (marginXDp * density);
        int marginYPx = (int) (marginYDp * density);
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // Si es draggable, calcular posición absoluta basada en la posición predefinida y márgenes
        if (isDraggable) {
            String position = FloatingButtonConfig.getPosition(this);
            params.gravity = Gravity.TOP | Gravity.START;
            
            // Calcular posición absoluta basada en la posición predefinida
            int absoluteX = 0;
            int absoluteY = 0;
            
            // Obtener el ancho del dock si está disponible
            int dockWidth = 0;
            int dockHeight = 0;
            if (floatingButtonView != null) {
                floatingButtonView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                dockWidth = floatingButtonView.getMeasuredWidth();
                dockHeight = floatingButtonView.getMeasuredHeight();
            }
            
            switch (position) {
                case "top_left":
                    absoluteX = marginXPx;
                    absoluteY = marginYPx;
                    break;
                case "top_center":
                    absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); // Centrado considerando el ancho del dock
                    absoluteY = marginYPx;
                    break;
                case "top_right":
                    absoluteX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0); // Desde la derecha
                    absoluteY = marginYPx;
                    break;
                case "center_left":
                    absoluteX = marginXPx;
                    absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0); // Centrado verticalmente
                    break;
                case "center_right":
                    absoluteX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0); // Desde la derecha
                    absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0); // Centrado verticalmente
                    break;
                case "bottom_left":
                    absoluteX = marginXPx;
                    absoluteY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0); // Desde abajo
                    break;
                case "bottom_center":
                    absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); // Centrado horizontalmente
                    absoluteY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0); // Desde abajo
                    break;
                case "bottom_right":
                default:
                    absoluteX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0); // Desde la derecha
                    absoluteY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0); // Desde abajo
                    break;
            }
            
            params.x = absoluteX;
            params.y = absoluteY;
            
            // NO guardar los márgenes aquí - mantener los márgenes configurados por el usuario
            // Los márgenes solo deben cambiar cuando el usuario los modifica explícitamente en la configuración
            Log.d(TAG, "Posición draggable calculada desde " + position + ": X=" + absoluteX + "px, Y=" + absoluteY + "px, márgenes configurados: X=" + marginXDp + "dp, Y=" + marginYDp + "dp");
            return;
        }
        
        // Si no es draggable, usar sistema de posiciones predefinidas con coordenadas absolutas
        // Esto permite márgenes negativos y posiciones fuera de la pantalla
        String position = FloatingButtonConfig.getPosition(this);
        
        // Usar siempre coordenadas absolutas con Gravity.TOP | Gravity.START
        // para permitir posiciones fuera de la pantalla
        params.gravity = Gravity.TOP | Gravity.START;
        
        // Obtener dimensiones del dock si está disponible
        int dockWidth = 0;
        int dockHeight = 0;
        if (floatingButtonView != null) {
            floatingButtonView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            dockWidth = floatingButtonView.getMeasuredWidth();
            dockHeight = floatingButtonView.getMeasuredHeight();
        }
        
        // Calcular posición absoluta basada en la posición predefinida y márgenes
        int absoluteX = 0;
        int absoluteY = 0;
        
        switch (position) {
            case "top_left":
                absoluteX = marginXPx; // Margen horizontal desde borde izquierdo (puede ser negativo)
                absoluteY = marginYPx; // Margen vertical desde borde superior (puede ser negativo)
                break;
            case "top_center":
                absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); // Centrado (sin margen horizontal)
                absoluteY = marginYPx; // Margen vertical desde borde superior (puede ser negativo)
                break;
            case "top_right":
                absoluteX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; // Desde la derecha - margen X
                absoluteY = marginYPx; // Margen vertical desde borde superior (puede ser negativo)
                break;
            case "center_left":
                absoluteX = marginXPx; // Margen horizontal desde borde izquierdo (puede ser negativo)
                absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; // Centrado + margen Y
                break;
            case "center_right":
                absoluteX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; // Desde la derecha - margen X
                absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; // Centrado + margen Y
                break;
            case "bottom_left":
                absoluteX = marginXPx; // Margen horizontal desde borde izquierdo (puede ser negativo)
                absoluteY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; // Desde abajo - margen Y
                break;
            case "bottom_center":
                absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); // Centrado (sin margen horizontal)
                absoluteY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; // Desde abajo - margen Y
                break;
            case "bottom_right":
            default:
                absoluteX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; // Desde la derecha - margen X
                absoluteY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; // Desde abajo - margen Y
                break;
        }
        
        params.x = absoluteX;
        params.y = absoluteY;
        
        // Asegurar que FLAG_LAYOUT_NO_LIMITS esté activo para permitir posiciones fuera de la pantalla
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        
        Log.d(TAG, "Posición aplicada: " + position + ", margen X: " + marginXPx + "px (" + marginXDp + "dp), margen Y: " + marginYPx + "px (" + marginYDp + "dp), x=" + params.x + ", y=" + params.y + ", absoluteX=" + absoluteX + ", absoluteY=" + absoluteY);
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
            setupDraggable(); // Reconfigurar drag después de recrear iconos
            setupDockBehavior(); // Reconfigurar comportamiento después de recrear iconos
            return;
        }
        
        // Si el número de iconos es el mismo, actualizar los iconos existentes
        // (por si cambió algún icono o configuración)
        updateIconSizes();
        updateIconDrawables(dockApps);
    }
    
    private void updateIconDrawables(List<DockApp> dockApps) {
        if (iconContainer == null || dockApps == null) return;
        
        int iconSizeDp = FloatingButtonConfig.getIconSize(this);
        float density = getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        
        int iconColor = FloatingButtonConfig.getIconColor(this);
        int iconAlpha = FloatingButtonConfig.getIconAlpha(this);
        int finalIconColor = (iconAlpha << 24) | (iconColor & 0x00FFFFFF);
        
        // Actualizar drawables de cada icono
        for (int i = 0; i < iconContainer.getChildCount() && i < dockApps.size(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView iconView = (ImageView) child;
                DockApp dockApp = dockApps.get(i);
                
                if (dockApp != null) {
                    String iconName = dockApp.getMaterialIconName();
                    
                    // Verificar si es icono nativo o Material
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
                }
            }
        }
    }
    
    private void removeFloatingButton() {
        // Limpiar handlers de drag
        if (longPressHandler != null) {
            longPressHandler.removeCallbacksAndMessages(null);
        }
        isDragging = false;
        
        if (floatingButtonView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingButtonView);
                floatingButtonView = null;
                iconContainer = null;
                dockContainer = null;
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
        // Desregistrar BroadcastReceiver
        try {
            unregisterReceiver(configUpdateReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error al desregistrar receiver", e);
        }
        removeFloatingButton();
    }
    
    // Métodos para actualizar el dock sin reiniciar el servicio
    private void updateDockConfiguration() {
        // Actualizar todas las propiedades
        updateDockBackgroundAnimated();
        updateIconSizes();
        updateDockPosition();
        updateDockIcons();
        // Actualizar comportamiento del dock (puede haber cambiado)
        setupDockBehavior();
        // Actualizar estado de draggable
        updateDraggableState();
    }
    
    private void updateIconSizes() {
        if (iconContainer == null) return;
        
        int iconSizeDp = FloatingButtonConfig.getIconSize(this);
        float density = getResources().getDisplayMetrics().density;
        int iconSizePx = (int) (iconSizeDp * density);
        
        int gapDp = FloatingButtonConfig.getIconGap(this);
        int gapPx = (int) (gapDp * density);
        
        int paddingDp = FloatingButtonConfig.getIconPadding(this);
        int paddingPx = (int) (paddingDp * density);
        
        int iconColor = FloatingButtonConfig.getIconColor(this);
        int iconAlpha = FloatingButtonConfig.getIconAlpha(this);
        int finalIconColor = (iconAlpha << 24) | (iconColor & 0x00FFFFFF);
        
        // Animar cambio de tamaño de iconos
        for (int i = 0; i < iconContainer.getChildCount(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView iconView = (ImageView) child;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iconView.getLayoutParams();
                
                // Animar cambio de tamaño
                ValueAnimator sizeAnimator = ValueAnimator.ofInt(params.width, iconSizePx);
                sizeAnimator.setDuration(200);
                sizeAnimator.addUpdateListener(animator -> {
                    int newSize = (Integer) animator.getAnimatedValue();
                    params.width = newSize;
                    params.height = newSize;
                    iconView.setLayoutParams(params);
                });
                sizeAnimator.start();
                
                // Actualizar padding y color
                iconView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                iconView.setColorFilter(finalIconColor);
                
                // Actualizar margen (gap)
                if (i > 0) {
                    params.setMargins(gapPx, 0, 0, 0);
                } else {
                    params.setMargins(0, 0, 0, 0);
                }
            }
        }
    }
    
    private void updateDockPosition() {
        if (params == null || floatingButtonView == null) return;
        
        // Migrar valor antiguo si existe
        FloatingButtonConfig.migrateOldPositionMargin(this);
        
        boolean isDraggable = FloatingButtonConfig.isDockDraggable(this);
        int marginXDp = FloatingButtonConfig.getPositionMarginX(this);
        int marginYDp = FloatingButtonConfig.getPositionMarginY(this);
        float density = getResources().getDisplayMetrics().density;
        int marginXPx = (int) (marginXDp * density);
        int marginYPx = (int) (marginYDp * density);
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        String position = FloatingButtonConfig.getPosition(this);
        
        // Obtener dimensiones del dock
        floatingButtonView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int dockWidth = floatingButtonView.getMeasuredWidth();
        int dockHeight = floatingButtonView.getMeasuredHeight();
        
        int newX = params.x;
        int newY = params.y;
        int newGravity = params.gravity;
        
        // Si es draggable, calcular posición absoluta
        if (isDraggable) {
            newGravity = Gravity.TOP | Gravity.START;
            
            switch (position) {
                case "top_left":
                    newX = marginXPx;
                    newY = marginYPx;
                    break;
                case "top_center":
                    newX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0);
                    newY = marginYPx;
                    break;
                case "top_right":
                    newX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0);
                    newY = marginYPx;
                    break;
                case "center_left":
                    newX = marginXPx;
                    newY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0);
                    break;
                case "center_right":
                    newX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0);
                    newY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0);
                    break;
                case "bottom_left":
                    newX = marginXPx;
                    newY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0);
                    break;
                case "bottom_center":
                    newX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0);
                    newY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0);
                    break;
                case "bottom_right":
                default:
                    newX = screenWidth - marginXPx - (dockWidth > 0 ? dockWidth : 0);
                    newY = screenHeight - marginYPx - (dockHeight > 0 ? dockHeight : 0);
                    break;
            }
        } else {
            // Si no es draggable, usar sistema de posiciones predefinidas con coordenadas absolutas
            // Esto permite márgenes negativos y posiciones fuera de la pantalla
            newGravity = Gravity.TOP | Gravity.START;
            
            switch (position) {
                case "top_left":
                    newX = marginXPx; // Margen horizontal desde borde izquierdo (puede ser negativo)
                    newY = marginYPx; // Margen vertical desde borde superior (puede ser negativo)
                    break;
                case "top_center":
                    newX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); // Centrado (sin margen horizontal)
                    newY = marginYPx; // Margen vertical desde borde superior (puede ser negativo)
                    break;
                case "top_right":
                    newX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; // Desde la derecha - margen X
                    newY = marginYPx; // Margen vertical desde borde superior (puede ser negativo)
                    break;
                case "center_left":
                    newX = marginXPx; // Margen horizontal desde borde izquierdo (puede ser negativo)
                    newY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; // Centrado + margen Y
                    break;
                case "center_right":
                    newX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; // Desde la derecha - margen X
                    newY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; // Centrado + margen Y
                    break;
                case "bottom_left":
                    newX = marginXPx; // Margen horizontal desde borde izquierdo (puede ser negativo)
                    newY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; // Desde abajo - margen Y
                    break;
                case "bottom_center":
                    newX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); // Centrado (sin margen horizontal)
                    newY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; // Desde abajo - margen Y
                    break;
                case "bottom_right":
                default:
                    newX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; // Desde la derecha - margen X
                    newY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; // Desde abajo - margen Y
                    break;
            }
        }
        
        // Asegurar que FLAG_LAYOUT_NO_LIMITS esté activo para permitir posiciones fuera de la pantalla
        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        
        // Animar cambio de posición y gravity
        final int finalGravity = newGravity;
        ValueAnimator xAnimator = ValueAnimator.ofInt(params.x, newX);
        ValueAnimator yAnimator = ValueAnimator.ofInt(params.y, newY);
        
        xAnimator.setDuration(200);
        yAnimator.setDuration(200);
        
        xAnimator.addUpdateListener(animator -> {
            params.x = (Integer) animator.getAnimatedValue();
            params.gravity = finalGravity; // Actualizar gravity también
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // Mantener flag activo
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición", e);
            }
        });
        
        yAnimator.addUpdateListener(animator -> {
            params.y = (Integer) animator.getAnimatedValue();
            params.gravity = finalGravity; // Actualizar gravity también
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // Mantener flag activo
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición", e);
            }
        });
        
        xAnimator.start();
        yAnimator.start();
    }
    
    private void updateDockBackgroundAnimated() {
        if (iconContainer == null) return;
        
        try {
            int radiusDp = FloatingButtonConfig.getBorderRadius(this);
            float density = getResources().getDisplayMetrics().density;
            int radiusPx = (int) (radiusDp * density);
            
            int bgColor = FloatingButtonConfig.getBackgroundColor(this);
            int bgAlpha = FloatingButtonConfig.getBackgroundAlpha(this);
            int finalColor = (bgAlpha << 24) | (bgColor & 0x00FFFFFF);
            
            android.graphics.drawable.Drawable currentBg = iconContainer.getBackground();
            int currentRadius = 0;
            int currentColor = 0;
            
            if (currentBg instanceof GradientDrawable) {
                GradientDrawable gd = (GradientDrawable) currentBg;
                currentRadius = (int) gd.getCornerRadius();
                // Obtener color actual es más complejo, usar el configurado
                currentColor = finalColor;
            }
            
            // Animar border radius
            ValueAnimator radiusAnimator = ValueAnimator.ofInt(currentRadius, radiusPx);
            radiusAnimator.setDuration(200);
            radiusAnimator.addUpdateListener(animator -> {
                int newRadius = (Integer) animator.getAnimatedValue();
                GradientDrawable background = new GradientDrawable();
                background.setShape(GradientDrawable.RECTANGLE);
                background.setCornerRadius(newRadius);
                background.setColor(finalColor);
                iconContainer.setBackground(background);
            });
            radiusAnimator.start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar fondo del dock", e);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // Servicio no vinculado, retorna null
        return null;
    }
}


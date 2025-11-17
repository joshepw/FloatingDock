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


    private WindowManager windowManager;
    private View floatingButtonView;
    private WindowManager.LayoutParams params;
    private LinearLayout iconContainer;
    private FrameLayout dockContainer; 
    private String hideDirection = ""; 


    private boolean isDragging = false;
    private boolean wasDraggableEnabled = false; 
    private float initialTouchX;
    private float initialTouchY;
    private int initialX;
    private int initialY;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private static final long LONG_PRESS_TIMEOUT = 500; 


    private boolean isDockHidden = false;
    private Handler hideTimeoutHandler;
    private Runnable hideTimeoutRunnable;
    private int originalX;
    private int originalY;
    private int hiddenX;
    private int hiddenY;


    private BroadcastReceiver configUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (floatingButtonView == null || iconContainer == null) return;


            if (isDockHidden && !"HIDE_DOCK_ACTION".equals(action)) {

                showDockImmediately();
            }

            switch (action) {
                case "UPDATE_DOCK_CONFIG":

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

                    hideDock();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");


        MaterialSymbolsMapper.initialize(this);

        createNotificationChannel();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_DOCK_CONFIG");
        filter.addAction("UPDATE_ICON_SIZE");
        filter.addAction("UPDATE_POSITION");
        filter.addAction("UPDATE_BACKGROUND");
        filter.addAction("UPDATE_ICONS");
        filter.addAction("HIDE_DOCK_ACTION");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(configUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(configUpdateReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio iniciado");


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


        startForeground(NOTIFICATION_ID, notification);


        if (Settings.canDrawOverlays(this)) {
            showFloatingButton();
        } else {
            Log.w(TAG, "No se tiene permiso para dibujar sobre otras apps");
        }


        launchAutoStartApp();


        return START_STICKY;
    }

    private void showFloatingButton() {
        if (floatingButtonView != null) {

            removeFloatingButton();
        }

        try {

            dockContainer = new FrameLayout(this);


            iconContainer = new LinearLayout(this);
            iconContainer.setOrientation(LinearLayout.HORIZONTAL);
            iconContainer.setPadding(16, 16, 16, 16);


            applyDockBackground();


            createDockIcons();


            dockContainer.addView(iconContainer);


            params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
                PixelFormat.TRANSLUCENT
            );

            floatingButtonView = dockContainer;


            wasDraggableEnabled = FloatingButtonConfig.isDockDraggable(this);


            applyInitialPosition();


            setupDraggable();


            windowManager.addView(floatingButtonView, params);


            if (params != null && floatingButtonView != null) {
                try {
                    windowManager.updateViewLayout(floatingButtonView, params);
                    Log.d(TAG, "Posición actualizada después de agregar vista");
                } catch (Exception e) {
                    Log.e(TAG, "Error al actualizar posición", e);
                }
            }


            updateDockIcons();


            setupDockBehavior();

            Log.d(TAG, "Botón flotante mostrado");
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar botón flotante", e);
        }
    }
    private void setupDockBehavior() {
        if (floatingButtonView == null || iconContainer == null) return;

        String behavior = FloatingButtonConfig.getDockBehavior(this);


        if ("hide_on_action".equals(behavior)) {

            if (isDockHidden) {
                showDock();
            }
            cancelHideTimeout();
        } else if ("hide_after_time".equals(behavior)) {

            if (isDockHidden) {
                showDock();
            } else {
                startHideTimeout();
            }
        }


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

        String behavior = FloatingButtonConfig.getDockBehavior(this);
        if ("hide_after_time".equals(behavior) && !isDockHidden) {
            startHideTimeout();
        }
    }

    private void hideDock() {
        if (isDockHidden || floatingButtonView == null || params == null || iconContainer == null) return;

        try {
            isDockHidden = true;


            originalX = params.x;
            originalY = params.y;


            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            float density = getResources().getDisplayMetrics().density;


            int iconSizeDp = FloatingButtonConfig.getIconSize(this);
            int exposedSizeDp = (int) (iconSizeDp * 0.5);
            int exposedSizePx = (int) (exposedSizeDp * density);


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


            if (dockWidth == 0 || dockHeight == 0) {
                dockWidth = floatingButtonView.getWidth();
                dockHeight = floatingButtonView.getHeight();
            }


            if ((dockWidth == 0 || dockHeight == 0) && iconContainer != null) {
                iconContainer.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                dockWidth = iconContainer.getMeasuredWidth();
                dockHeight = iconContainer.getMeasuredHeight();
            }

            Log.d(TAG, "hideDock - Dimensiones calculadas: dockWidth=" + dockWidth + ", dockHeight=" + dockHeight);


            boolean isDraggable = FloatingButtonConfig.isDockDraggable(this);

            if (isDraggable) {


                int dockCenterX = originalX + (dockWidth / 2);
                int dockCenterY = originalY + (dockHeight / 2);

                int distanceToLeft = dockCenterX;
                int distanceToRight = screenWidth - dockCenterX;
                int distanceToTop = dockCenterY;
                int distanceToBottom = screenHeight - dockCenterY;


                int minDistance = Math.min(Math.min(distanceToLeft, distanceToRight), 
                                          Math.min(distanceToTop, distanceToBottom));


                if (minDistance == distanceToLeft) {

                    hiddenX = -(dockWidth - exposedSizePx);
                    hiddenY = originalY;
                } else if (minDistance == distanceToRight) {


                    hiddenX = screenWidth - exposedSizePx;
                    hiddenY = originalY;
                    Log.d(TAG, "hideDock - Ocultando hacia derecha: screenWidth=" + screenWidth + ", exposedSizePx=" + exposedSizePx + ", dockWidth=" + dockWidth + ", hiddenX=" + hiddenX + ", bordeDerecho=" + (hiddenX + dockWidth) + ", visible=" + (screenWidth - hiddenX));
                } else if (minDistance == distanceToTop) {

                    hiddenY = -(dockHeight - exposedSizePx);
                    hiddenX = originalX;
                } else {

                    hiddenY = screenHeight + exposedSizePx - dockHeight;
                    hiddenX = originalX;
                }
            } else {

                String position = FloatingButtonConfig.getPosition(this);


                if (position.contains("right")) {


                    hiddenX = screenWidth - exposedSizePx;
                    hiddenY = originalY;
                    Log.d(TAG, "hideDock - Ocultando hacia derecha (config): screenWidth=" + screenWidth + ", exposedSizePx=" + exposedSizePx + ", dockWidth=" + dockWidth + ", hiddenX=" + hiddenX + ", bordeDerecho=" + (hiddenX + dockWidth) + ", visible=" + (screenWidth - hiddenX));
                } else if (position.contains("left")) {


                    hiddenX = -(dockWidth - exposedSizePx);
                    hiddenY = originalY;
                } else if (position.contains("center")) {


                    if (position.contains("top")) {

                        hiddenY = -(dockHeight - exposedSizePx);
                        hiddenX = originalX;
                    } else if (position.contains("bottom")) {

                        hiddenY = screenHeight + exposedSizePx - dockHeight;
                        hiddenX = originalX;
                    } else {

                        hiddenX = screenWidth + exposedSizePx - dockWidth;
                        hiddenY = originalY;
                    }
                } else {

                    hiddenX = screenWidth + exposedSizePx - dockWidth;
                    hiddenY = originalY;
                }
            }


            if (hiddenX < originalX) {
                hideDirection = "left";
            } else if (hiddenX > originalX) {
                hideDirection = "right";
            } else if (hiddenY < originalY) {
                hideDirection = "top";
            } else {
                hideDirection = "bottom";
            }


            Log.d(TAG, "Ocultando dock - Posición original: X=" + originalX + ", Y=" + originalY);
            Log.d(TAG, "Ocultando dock - Coordenadas ocultas: hiddenX=" + hiddenX + ", hiddenY=" + hiddenY);
            Log.d(TAG, "Ocultando dock - Dimensiones: dockWidth=" + dockWidth + ", dockHeight=" + dockHeight + ", exposedSizePx=" + exposedSizePx);
            Log.d(TAG, "Ocultando dock - Pantalla: screenWidth=" + screenWidth + ", screenHeight=" + screenHeight);
            Log.d(TAG, "Ocultando dock - Dirección: " + hideDirection);


            hideIcons();


            updateDockHiddenBorder(true);


            enableHiddenDockClick();


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


            disableHiddenDockClick();


            updateDockHiddenBorder(false);


            animateShowPosition(() -> {

                showIcons();

                startHideTimeout();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar dock", e);
            isDockHidden = false;
        }
    }

    private void showDockImmediately() {

        if (!isDockHidden || floatingButtonView == null || params == null || iconContainer == null) return;

        try {
            isDockHidden = false;
            cancelHideTimeout();


            disableHiddenDockClick();


            updateDockHiddenBorder(false);


            params.x = originalX;
            params.y = originalY;

            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición durante showDockImmediately", e);
            }


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


        if (!FloatingButtonConfig.isDockDraggable(this)) {
            floatingButtonView.setOnClickListener(null);
        }
    }

    private void updateDraggableState() {

        if (floatingButtonView == null || params == null) {
            wasDraggableEnabled = FloatingButtonConfig.isDockDraggable(this);
            setupDraggable();
            return;
        }

        boolean isNowDraggable = FloatingButtonConfig.isDockDraggable(this);


        if (wasDraggableEnabled && !isNowDraggable) {
            resetPositionToInitialSettings();
        }


        wasDraggableEnabled = isNowDraggable;


        setupDraggable();
    }

    private void resetPositionToInitialSettings() {
        if (params == null || iconContainer == null) return;

        try {

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


            params.x = 0;
            params.y = 0;


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


            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición al deshabilitar draggable", e);
            }


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


                if (isDockHidden) {
                    return false;
                }

                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();


                        if (params != null) {
                            initialX = params.x;
                            initialY = params.y;
                        }


                        longPressRunnable = () -> {
                            if (!isDragging) {
                                isDragging = true;
                                updateDockBorder(true); 

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
                                updateDockBorder(true); 

                                resetHideTimeout();
                                longPressHandler.removeCallbacks(longPressRunnable);
                            }


                            if (isDragging) {
                                resetHideTimeout();
                            }


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
                            updateDockBorder(false); 

                            resetHideTimeout();
                            Log.d(TAG, "Drag finalizado, posición guardada");
                        }
                        return isDragging;
                }
                return false;
            }

            private boolean shouldStartDragging(android.view.MotionEvent event) {

                float deltaX = Math.abs(event.getRawX() - initialTouchX);
                float deltaY = Math.abs(event.getRawY() - initialTouchY);
                float threshold = 20; 
                return deltaX > threshold || deltaY > threshold;
            }
        });
    }

    private void setupIconDragListener(ImageView iconView, String packageName, String activityName, String actionId, boolean isAction) {
        if (longPressHandler == null) {
            longPressHandler = new Handler(Looper.getMainLooper());
        }

        final long ICON_LONG_PRESS_TIMEOUT = 1000; 
        final Runnable[] iconLongPressRunnable = {null};
        final boolean[] longPressActivated = {false}; 
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


                        if (params != null) {
                            initialX = params.x;
                            initialY = params.y;
                        }


                        iconLongPressRunnable[0] = () -> {

                            if (touchStarted[0] && !longPressActivated[0]) {
                                longPressActivated[0] = true;
                                isDragging = true;
                                updateDockBorder(true); 

                                resetHideTimeout();
                                Log.d(TAG, "Long press en icono detectado (1 segundo), drag habilitado");
                            }
                        };
                        longPressHandler.postDelayed(iconLongPressRunnable[0], ICON_LONG_PRESS_TIMEOUT);
                        return true; 

                    case android.view.MotionEvent.ACTION_MOVE:

                        if (longPressActivated[0]) {
                            isCurrentlyDragging[0] = true;


                            resetHideTimeout();


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
                            return true; 
                        }

                        return true;

                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        touchStarted[0] = false;

                        longPressHandler.removeCallbacks(iconLongPressRunnable[0]);

                        if (isCurrentlyDragging[0]) {

                            Log.d(TAG, "Drag desde icono finalizado, posición guardada");
                            isDragging = false;
                            updateDockBorder(false); 

                            resetHideTimeout();
                            longPressActivated[0] = false;
                            isCurrentlyDragging[0] = false;
                            return true; 
                        } else {

                            isDragging = false;
                            longPressActivated[0] = false;


                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                handleIconClick(isAction, actionId, packageName, activityName);
                            }, 50);
                            return true; 
                        }
                }
                return true;
            }
        });
    }

    private void applyDockBackground() {
        if (iconContainer == null) return;

        try {

            int radiusDp = FloatingButtonConfig.getBorderRadius(this);


            float density = getResources().getDisplayMetrics().density;
            int radiusPx = (int) (radiusDp * density);


            int bgColor = FloatingButtonConfig.getBackgroundColor(this);
            int bgAlpha = FloatingButtonConfig.getBackgroundAlpha(this);


            int finalColor = (bgAlpha << 24) | (bgColor & 0x00FFFFFF);


            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(radiusPx);
            background.setColor(finalColor);


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

                    float density = getResources().getDisplayMetrics().density;
                    int borderWidthDp = 3;
                    int borderWidthPx = (int) (borderWidthDp * density);
                    int orangeColor = 0xFFFF9800; 
                    background.setStroke(borderWidthPx, orangeColor);


                    resetHideTimeout();
                } else {

                    background.setStroke(0, 0);
                }


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

                float density = getResources().getDisplayMetrics().density;
                int borderWidthDp = 5;
                int borderWidthPx = (int) (borderWidthDp * density);
                final int targetAlpha = 150; 


                GradientDrawable originalBackground = null;
                if (currentBackground instanceof GradientDrawable) {
                    originalBackground = (GradientDrawable) currentBackground;
                } else if (currentBackground instanceof android.graphics.drawable.LayerDrawable) {

                    android.graphics.drawable.LayerDrawable layerDrawable = (android.graphics.drawable.LayerDrawable) currentBackground;
                    android.graphics.drawable.Drawable innerDrawable = layerDrawable.getDrawable(1);
                    if (innerDrawable instanceof GradientDrawable) {
                        originalBackground = (GradientDrawable) innerDrawable;
                    }
                }

                if (originalBackground == null) {

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


                final GradientDrawable strokeLayer = new GradientDrawable();
                strokeLayer.setShape(GradientDrawable.RECTANGLE);
                strokeLayer.setCornerRadius(originalBackground.getCornerRadius());
                strokeLayer.setColor(0x00000000); 
                strokeLayer.setStroke(borderWidthPx, 0x00FFFFFF); 


                android.graphics.drawable.Drawable[] layers = new android.graphics.drawable.Drawable[2];
                layers[0] = strokeLayer; 
                layers[1] = originalBackground; 

                final android.graphics.drawable.LayerDrawable layerDrawable = new android.graphics.drawable.LayerDrawable(layers);

                layerDrawable.setLayerInset(1, borderWidthPx, borderWidthPx, borderWidthPx, borderWidthPx);

                iconContainer.setBackground(layerDrawable);


                ValueAnimator alphaAnimator = ValueAnimator.ofInt(0, targetAlpha);
                alphaAnimator.setDuration(300);
                alphaAnimator.setInterpolator(new DecelerateInterpolator());
                alphaAnimator.addUpdateListener(animation -> {
                    int alpha = (Integer) animation.getAnimatedValue();
                    int whiteColor = (alpha << 24) | 0x00FFFFFF; 
                    strokeLayer.setStroke(borderWidthPx, whiteColor);
                    iconContainer.invalidate();
                });
                alphaAnimator.start();
            } else {

                GradientDrawable originalBackground = null;
                GradientDrawable strokeLayer = null;

                if (currentBackground instanceof android.graphics.drawable.LayerDrawable) {

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

                    final int currentAlpha = 150; 
                    final GradientDrawable finalStrokeLayer = strokeLayer;
                    final GradientDrawable finalOriginalBackground = originalBackground;

                    ValueAnimator alphaAnimator = ValueAnimator.ofInt(currentAlpha, 0);
                    alphaAnimator.setDuration(300);
                    alphaAnimator.setInterpolator(new DecelerateInterpolator());
                    alphaAnimator.addUpdateListener(animation -> {
                        int alpha = (Integer) animation.getAnimatedValue();
                        int whiteColor = (alpha << 24) | 0x00FFFFFF; 
                        float density = getResources().getDisplayMetrics().density;
                        int borderWidthPx = (int) (5 * density);
                        finalStrokeLayer.setStroke(borderWidthPx, whiteColor);
                        iconContainer.invalidate();
                    });
                    alphaAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            if (finalOriginalBackground != null) {

                                if (isDragging) {
                                    float density = getResources().getDisplayMetrics().density;
                                    int borderWidthDp = 3;
                                    int borderWidthPx = (int) (borderWidthDp * density);
                                    int orangeColor = 0xFFFF9800; 
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

                    if (isDragging) {
                        float density = getResources().getDisplayMetrics().density;
                        int borderWidthDp = 3;
                        int borderWidthPx = (int) (borderWidthDp * density);
                        int orangeColor = 0xFFFF9800; 
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


        iconContainer.removeAllViews();

        List<DockApp> dockApps = DockAppManager.getDockApps(this);
        if (dockApps == null || dockApps.isEmpty()) {
            return;
        }


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

        for (int i = 0; i < dockApps.size(); i++) {
            DockApp dockApp = dockApps.get(i);


            ImageView iconView = new ImageView(this);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(iconSizePx, iconSizePx));


            iconView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);


            if (i > 0) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) iconView.getLayoutParams();
                layoutParams.setMargins(gapPx, 0, 0, 0);
                iconView.setLayoutParams(layoutParams);
            }


            String iconName = dockApp.getMaterialIconName();
            if ("native".equals(iconName)) {

                try {
                    PackageManager pm = getPackageManager();
                    android.graphics.drawable.Drawable appIcon = pm.getApplicationIcon(dockApp.getPackageName());
                    iconView.setImageDrawable(appIcon);
                    iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } catch (Exception e) {
                    Log.e(TAG, "Error al obtener icono nativo para " + dockApp.getPackageName(), e);

                    iconView.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.sym_def_app_icon));
                }
            } else {

                MaterialIconDrawable iconDrawable = new MaterialIconDrawable(this);
                iconDrawable.setIcon(iconName);
                iconDrawable.setSize(iconSizePx);
                iconDrawable.setColor(finalIconColor);

                iconDrawable.setBounds(0, 0, iconSizePx, iconSizePx);
                iconView.setImageDrawable(iconDrawable);
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }


            final String packageName = dockApp.getPackageName();
            final String activityName = dockApp.getActivityName();
            final String actionId = dockApp.getActionId();
            final boolean isAction = dockApp.isAction();


            if (FloatingButtonConfig.isDockDraggable(this)) {

                setupIconDragListener(iconView, packageName, activityName, actionId, isAction);
            } else {

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

            if (FloatingButtonConfig.isAutoStartLaunched(this)) {
                Log.d(TAG, "App de inicio automático ya fue lanzada en esta sesión");
                return;
            }


            String packageName = FloatingButtonConfig.getAutoStartPackage(this);
            String activityName = FloatingButtonConfig.getAutoStartActivity(this);

            if (packageName == null || packageName.isEmpty()) {
                return; 
            }

            Log.d(TAG, "Lanzando app de inicio automático: " + packageName + (activityName != null ? " (" + activityName + ")" : ""));


            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    openApp(packageName, activityName);

                    FloatingButtonConfig.setAutoStartLaunched(this, true);
                    Log.d(TAG, "App de inicio automático lanzada exitosamente");
                } catch (Exception e) {
                    Log.e(TAG, "Error al lanzar app de inicio automático", e);
                }
            }, 2000); 

        } catch (Exception e) {
            Log.e(TAG, "Error en launchAutoStartApp", e);
        }
    }

    private void openApp(String packageName) {
        openApp(packageName, null);
    }

    private void handleIconClick(boolean isAction, String actionId, String packageName, String activityName) {

        if (isDockHidden) {
            showDock();
            return; 
        }


        executeIconAction(isAction, actionId, packageName, activityName);
    }

    private void executeIconAction(boolean isAction, String actionId, String packageName, String activityName) {

        resetHideTimeout();

        if (isAction && actionId != null) {

            ActionExecutor.executeAction(this, actionId);
        } else {

            openApp(packageName, activityName);
        }
    }

    private void openApp(String packageName, String activityName) {
        try {
            PackageManager pm = getPackageManager();
            Intent launchIntent = null;


            if (activityName != null && !activityName.isEmpty()) {
                try {
                    launchIntent = new Intent();
                    launchIntent.setClassName(packageName, activityName);
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                    if (launchIntent.resolveActivity(pm) == null) {
                        Log.w(TAG, "Activity no encontrada: " + activityName + ", intentando método alternativo");
                        launchIntent = null;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error al crear intent para activity específica: " + activityName, e);
                    launchIntent = null;
                }
            }


            if (launchIntent == null) {
                launchIntent = pm.getLaunchIntentForPackage(packageName);
            }

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                Log.d(TAG, "App abierta: " + packageName + (activityName != null ? " (" + activityName + ")" : ""));
            } else {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                } else {

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


        if (isDraggable) {
            String position = FloatingButtonConfig.getPosition(this);
            params.gravity = Gravity.TOP | Gravity.START;


            int absoluteX = 0;
            int absoluteY = 0;


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

            params.x = absoluteX;
            params.y = absoluteY;


            Log.d(TAG, "Posición draggable calculada desde " + position + ": X=" + absoluteX + "px, Y=" + absoluteY + "px, márgenes configurados: X=" + marginXDp + "dp, Y=" + marginYDp + "dp");
            return;
        }


        String position = FloatingButtonConfig.getPosition(this);


        params.gravity = Gravity.TOP | Gravity.START;


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
                absoluteX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; 
                absoluteY = marginYPx; 
                break;
            case "center_left":
                absoluteX = marginXPx; 
                absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; 
                break;
            case "center_right":
                absoluteX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; 
                absoluteY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; 
                break;
            case "bottom_left":
                absoluteX = marginXPx; 
                absoluteY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; 
                break;
            case "bottom_center":
                absoluteX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); 
                absoluteY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; 
                break;
            case "bottom_right":
            default:
                absoluteX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; 
                absoluteY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; 
                break;
        }

        params.x = absoluteX;
        params.y = absoluteY;


        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        Log.d(TAG, "Posición aplicada: " + position + ", margen X: " + marginXPx + "px (" + marginXDp + "dp), margen Y: " + marginYPx + "px (" + marginYDp + "dp), x=" + params.x + ", y=" + params.y + ", absoluteX=" + absoluteX + ", absoluteY=" + absoluteY);
    }

    private void updateDockIcons() {
        List<DockApp> dockApps = DockAppManager.getDockApps(this);
        if (dockApps == null || dockApps.isEmpty()) {

            if (floatingButtonView != null) {
                floatingButtonView.setVisibility(View.GONE);
            }
            return;
        }


        if (floatingButtonView != null) {
            floatingButtonView.setVisibility(View.VISIBLE);
        }


        int currentIconCount = iconContainer.getChildCount();
        if (currentIconCount != dockApps.size()) {

            applyDockBackground(); 
            createDockIcons();
            setupDraggable(); 
            setupDockBehavior(); 
            return;
        }


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


        for (int i = 0; i < iconContainer.getChildCount() && i < dockApps.size(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView iconView = (ImageView) child;
                DockApp dockApp = dockApps.get(i);

                if (dockApp != null) {
                    String iconName = dockApp.getMaterialIconName();


                    if ("native".equals(iconName)) {

                        try {
                            PackageManager pm = getPackageManager();
                            android.graphics.drawable.Drawable appIcon = pm.getApplicationIcon(dockApp.getPackageName());
                            iconView.setImageDrawable(appIcon);
                            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al obtener icono nativo para " + dockApp.getPackageName(), e);

                            iconView.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.sym_def_app_icon));
                        }
                    } else {

                        MaterialIconDrawable iconDrawable = new MaterialIconDrawable(this);
                        iconDrawable.setIcon(iconName);
                        iconDrawable.setSize(iconSizePx);
                        iconDrawable.setColor(finalIconColor);

                        iconDrawable.setBounds(0, 0, iconSizePx, iconSizePx);
                        iconView.setImageDrawable(iconDrawable);
                        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                }
            }
        }
    }

    private void removeFloatingButton() {

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

        try {
            unregisterReceiver(configUpdateReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error al desregistrar receiver", e);
        }
        removeFloatingButton();
    }


    private void updateDockConfiguration() {

        updateDockBackgroundAnimated();
        updateIconSizes();
        updateDockPosition();
        updateDockIcons();

        setupDockBehavior();

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


        for (int i = 0; i < iconContainer.getChildCount(); i++) {
            View child = iconContainer.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView iconView = (ImageView) child;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iconView.getLayoutParams();


                ValueAnimator sizeAnimator = ValueAnimator.ofInt(params.width, iconSizePx);
                sizeAnimator.setDuration(200);
                sizeAnimator.addUpdateListener(animator -> {
                    int newSize = (Integer) animator.getAnimatedValue();
                    params.width = newSize;
                    params.height = newSize;
                    iconView.setLayoutParams(params);
                });
                sizeAnimator.start();


                iconView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                iconView.setColorFilter(finalIconColor);


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


        floatingButtonView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int dockWidth = floatingButtonView.getMeasuredWidth();
        int dockHeight = floatingButtonView.getMeasuredHeight();

        int newX = params.x;
        int newY = params.y;
        int newGravity = params.gravity;


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
                    newX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; 
                    newY = marginYPx; 
                    break;
                case "center_left":
                    newX = marginXPx; 
                    newY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; 
                    break;
                case "center_right":
                    newX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; 
                    newY = (screenHeight / 2) - (dockHeight > 0 ? dockHeight / 2 : 0) + marginYPx; 
                    break;
                case "bottom_left":
                    newX = marginXPx; 
                    newY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; 
                    break;
                case "bottom_center":
                    newX = (screenWidth / 2) - (dockWidth > 0 ? dockWidth / 2 : 0); 
                    newY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; 
                    break;
                case "bottom_right":
                default:
                    newX = screenWidth - (dockWidth > 0 ? dockWidth : 0) - marginXPx; 
                    newY = screenHeight - (dockHeight > 0 ? dockHeight : 0) - marginYPx; 
                    break;
            }
        }


        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;


        final int finalGravity = newGravity;
        ValueAnimator xAnimator = ValueAnimator.ofInt(params.x, newX);
        ValueAnimator yAnimator = ValueAnimator.ofInt(params.y, newY);

        xAnimator.setDuration(200);
        yAnimator.setDuration(200);

        xAnimator.addUpdateListener(animator -> {
            params.x = (Integer) animator.getAnimatedValue();
            params.gravity = finalGravity; 
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; 
            try {
                windowManager.updateViewLayout(floatingButtonView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición", e);
            }
        });

        yAnimator.addUpdateListener(animator -> {
            params.y = (Integer) animator.getAnimatedValue();
            params.gravity = finalGravity; 
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; 
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

                currentColor = finalColor;
            }


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

        return null;
    }
}


package com.joshepw.nexusfloatinghelper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private RecyclerView dockAppsRecycler;
    private DockAppAdapter dockAppAdapter;
    private EditText iconSizeInput;
    private Spinner positionSpinner;
    private EditText borderRadiusInput;
    private EditText backgroundColorInput;
    private EditText backgroundAlphaInput;
    private EditText iconColorInput;
    private EditText iconAlphaInput;
    private EditText iconGapInput;
    private EditText iconPaddingInput;
    private EditText positionMarginXInput;
    private EditText positionMarginYInput;
    private Spinner dockBehaviorSpinner;
    private LinearLayout hideTimeoutContainer;
    private EditText hideTimeoutInput;
    private Switch dockDraggableSwitch;
    private Button saveButton;
    private Button startServiceButton;
    private Button addAppButton;
    private Button checkUpdatesButton;

    private ActivityResultLauncher<Intent> overlayPermissionLauncher;
    private boolean isInitializing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        MaterialSymbolsMapper.initialize(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupOverlayPermissionLauncher();
        setupRecyclerView();
        setupListeners();
        setupAutoSaveListeners();
        loadCurrentSettings();
        isInitializing = false;


        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else {

            startBackgroundService();
        }
    }

    private void initViews() {
        dockAppsRecycler = findViewById(R.id.dock_apps_recycler);


        View iconSizeContainer = findViewById(R.id.icon_size_container);
        iconSizeInput = iconSizeContainer.findViewById(R.id.number_input);

        positionSpinner = findViewById(R.id.position_spinner);

        View borderRadiusContainer = findViewById(R.id.border_radius_container);
        borderRadiusInput = borderRadiusContainer.findViewById(R.id.number_input);

        backgroundColorInput = findViewById(R.id.background_color_input);

        View backgroundAlphaContainer = findViewById(R.id.background_alpha_container);
        backgroundAlphaInput = backgroundAlphaContainer.findViewById(R.id.number_input);

        iconColorInput = findViewById(R.id.icon_color_input);

        View iconAlphaContainer = findViewById(R.id.icon_alpha_container);
        iconAlphaInput = iconAlphaContainer.findViewById(R.id.number_input);

        View iconGapContainer = findViewById(R.id.icon_gap_container);
        iconGapInput = iconGapContainer.findViewById(R.id.number_input);

        View iconPaddingContainer = findViewById(R.id.icon_padding_container);
        iconPaddingInput = iconPaddingContainer.findViewById(R.id.number_input);

        View positionMarginXContainer = findViewById(R.id.position_margin_x_container);
        positionMarginXInput = positionMarginXContainer.findViewById(R.id.number_input);

        positionMarginXInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);

        View positionMarginYContainer = findViewById(R.id.position_margin_y_container);
        positionMarginYInput = positionMarginYContainer.findViewById(R.id.number_input);

        positionMarginYInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);

        dockBehaviorSpinner = findViewById(R.id.dock_behavior_spinner);
        hideTimeoutContainer = findViewById(R.id.hide_timeout_container);
        hideTimeoutInput = hideTimeoutContainer.findViewById(R.id.number_input);
        dockDraggableSwitch = findViewById(R.id.dock_draggable_switch);

        saveButton = findViewById(R.id.save_button);
        startServiceButton = findViewById(R.id.start_service_button);
        addAppButton = findViewById(R.id.add_app_button);
        checkUpdatesButton = findViewById(R.id.check_updates_button);


        setupNumberControls();
    }

    private void setupNumberControls() {

        setupNumberControl(R.id.icon_size_container, 1, 200);


        setupNumberControl(R.id.border_radius_container, 0, 100);


        setupNumberControl(R.id.background_alpha_container, 0, 255);


        setupNumberControl(R.id.icon_alpha_container, 0, 255);


        setupNumberControl(R.id.icon_gap_container, 0, 50);


        setupNumberControl(R.id.icon_padding_container, 0, 50);


        setupNumberControl(R.id.position_margin_x_container, -200, 200);


        setupNumberControl(R.id.position_margin_y_container, -200, 200);


        setupNumberControl(R.id.hide_timeout_container_input, 1, 60);
    }

    private void setupNumberControl(int containerId, int min, int max) {
        View container = findViewById(containerId);
        EditText input = container.findViewById(R.id.number_input);
        Button incrementBtn = container.findViewById(R.id.increment_button);
        Button decrementBtn = container.findViewById(R.id.decrement_button);

        incrementBtn.setOnClickListener(v -> {
            try {
                String currentValue = input.getText().toString();
                int value;
                if (TextUtils.isEmpty(currentValue)) {
                    value = min;
                } else {
                    value = Integer.parseInt(currentValue);
                }
                value = Math.min(value + 1, max);
                input.setText(String.valueOf(value));
            } catch (NumberFormatException e) {
                input.setText(String.valueOf(min));
            }
        });

        decrementBtn.setOnClickListener(v -> {
            try {
                String currentValue = input.getText().toString();
                int value;
                if (TextUtils.isEmpty(currentValue)) {
                    value = min;
                } else {
                    value = Integer.parseInt(currentValue);
                }
                value = Math.max(value - 1, min);
                input.setText(String.valueOf(value));
            } catch (NumberFormatException e) {
                input.setText(String.valueOf(min));
            }
        });
    }

    private void setupOverlayPermissionLauncher() {
        overlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();

                        startBackgroundService();
                    } else {
                        Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                overlayPermissionLauncher.launch(intent);
            }
        }
    }

    private void setupRecyclerView() {
        dockAppAdapter = new DockAppAdapter(this, new DockAppAdapter.OnDockAppClickListener() {
            @Override
            public void onDeleteClick(int position) {

                List<DockApp> appsBeforeDelete = new ArrayList<>(DockAppManager.getDockApps(MainActivity.this));


                DockAppManager.removeDockApp(MainActivity.this, position);


                refreshDockAppsList();


                sendConfigUpdateBroadcast("UPDATE_ICONS");
            }

            @Override
            public void onAutoStartClick(int position) {
                showAutoStartDialog(position);
            }

            @Override
            public void onEditClick(int position) {
                try {

                    List<DockApp> dockApps = DockAppManager.getDockApps(MainActivity.this);
                    if (position >= 0 && position < dockApps.size()) {
                        DockApp dockApp = dockApps.get(position);
                        if (dockApp != null) {

                            if (dockApp.isAction()) {

                                SystemAction action = SystemActionHelper.getActionById(dockApp.getActionId(), MainActivity.this);
                                Intent intent = new Intent(MainActivity.this, SelectIconActivity.class);
                                intent.putExtra("action_id", dockApp.getActionId());
                                intent.putExtra("action_name", action != null ? action.getActionName() : dockApp.getActionId());
                                intent.putExtra("current_icon", dockApp.getMaterialIconName());
                                intent.putExtra("index", position);
                                intent.putExtra("is_action", true);
                                startActivity(intent);
                            } else {

                                String packageName = dockApp.getPackageName();


                                if (ActivityUtils.hasMultipleActivities(MainActivity.this, packageName)) {

                                    Intent intent = new Intent(MainActivity.this, SelectActivityActivity.class);
                                    intent.putExtra("package_name", packageName);
                                    intent.putExtra("current_icon", dockApp.getMaterialIconName());
                                    intent.putExtra("index", position);
                                    intent.putExtra("is_editing", true); 
                                    startActivity(intent);
                                } else {

                                    Intent intent = new Intent(MainActivity.this, SelectIconActivity.class);
                                    intent.putExtra("package_name", packageName);
                                    intent.putExtra("current_icon", dockApp.getMaterialIconName());
                                    if (dockApp.getActivityName() != null && !dockApp.getActivityName().isEmpty()) {
                                        intent.putExtra("activity_name", dockApp.getActivityName());
                                    }
                                    intent.putExtra("index", position);
                                    startActivity(intent);
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_invalid_position), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error al editar app/acción", e);
                    Toast.makeText(MainActivity.this, getString(R.string.error_editing, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dockAppsRecycler.setLayoutManager(new LinearLayoutManager(this));
        dockAppsRecycler.setAdapter(dockAppAdapter);


        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                if (fromPosition < 0 || toPosition < 0) {
                    return false;
                }


                DockAppManager.reorderDockApps(MainActivity.this, fromPosition, toPosition);


                dockAppAdapter.notifyItemMoved(fromPosition, toPosition);


                previousDockApps = new ArrayList<>(DockAppManager.getDockApps(MainActivity.this));


                sendConfigUpdateBroadcast("UPDATE_ICONS");

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true; 
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(dockAppsRecycler);

        refreshDockAppsList();
    }

    private void showAutoStartDialog(int position) {
        try {
            List<DockApp> dockApps = DockAppManager.getDockApps(this);
            if (position < 0 || position >= dockApps.size()) {
                return;
            }

            DockApp dockApp = dockApps.get(position);
            if (dockApp == null || dockApp.isAction()) {
                return; 
            }

            String packageName = dockApp.getPackageName();
            String activityName = dockApp.getActivityName();
            boolean isCurrentlyActive = FloatingButtonConfig.isAutoStartApp(this, packageName, activityName);


            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.auto_start_dialog_title));
            builder.setMessage(getString(R.string.auto_start_dialog_message));

            if (isCurrentlyActive) {

                builder.setPositiveButton(getString(R.string.auto_start_dialog_deactivate), (dialog, which) -> {
                    FloatingButtonConfig.clearAutoStartApp(MainActivity.this);
                    refreshDockAppsList();
                });
            } else {

                builder.setPositiveButton(getString(R.string.auto_start_dialog_activate), (dialog, which) -> {

                    FloatingButtonConfig.clearAutoStartApp(MainActivity.this);

                    FloatingButtonConfig.saveAutoStartApp(MainActivity.this, packageName, activityName);
                    refreshDockAppsList();
                });
            }

            builder.setNegativeButton(getString(R.string.auto_start_dialog_cancel), null);
            builder.show();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error al mostrar diálogo de inicio automático", e);
            Toast.makeText(this, getString(R.string.error_generic, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private List<DockApp> previousDockApps = null;

    private void refreshDockAppsList() {
        List<DockApp> dockApps = DockAppManager.getDockApps(this);
        dockAppAdapter.updateList(dockApps);


        if (previousDockApps != null) {
            if (!dockAppsEqual(previousDockApps, dockApps)) {


                if (!isInitializing) {
                    sendConfigUpdateBroadcast("UPDATE_ICONS");
                }
            }
        }
        previousDockApps = new ArrayList<>(dockApps); 
    }

    private boolean dockAppsEqual(List<DockApp> list1, List<DockApp> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;

        for (int i = 0; i < list1.size(); i++) {
            DockApp app1 = list1.get(i);
            DockApp app2 = list2.get(i);
            if (app1 == null && app2 == null) continue;
            if (app1 == null || app2 == null) return false;


            String actionType1 = app1.getActionType();
            String actionType2 = app2.getActionType();
            if (actionType1 == null) actionType1 = "app";
            if (actionType2 == null) actionType2 = "app";
            if (!actionType1.equals(actionType2)) return false;


            if ("action".equals(actionType1)) {
                String actionId1 = app1.getActionId();
                String actionId2 = app2.getActionId();
                if (actionId1 == null) actionId1 = "";
                if (actionId2 == null) actionId2 = "";
                if (!actionId1.equals(actionId2)) return false;
            } else {

                String packageName1 = app1.getPackageName();
                String packageName2 = app2.getPackageName();
                if (packageName1 == null) packageName1 = "";
                if (packageName2 == null) packageName2 = "";
                if (!packageName1.equals(packageName2)) return false;


                String activity1 = app1.getActivityName();
                String activity2 = app2.getActivityName();
                if (activity1 == null) activity1 = "";
                if (activity2 == null) activity2 = "";
                if (!activity1.equals(activity2)) return false;
            }


            String iconName1 = app1.getMaterialIconName();
            String iconName2 = app2.getMaterialIconName();
            if (iconName1 == null) iconName1 = "";
            if (iconName2 == null) iconName2 = "";
            if (!iconName1.equals(iconName2)) return false;
        }
        return true;
    }

    private void setupListeners() {
        addAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectAppActivity.class);
            startActivity(intent);
        });

        saveButton.setOnClickListener(v -> saveSettings());

        checkUpdatesButton.setOnClickListener(v -> {
            checkForUpdates();
        });

        startServiceButton.setOnClickListener(v -> {
            startBackgroundService();
        });
    }

    private void setupAutoSaveListeners() {

        iconSizeInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconSizeInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconSize = Integer.parseInt(value);
                    if (iconSize > 0 && iconSize <= 200) {
                        FloatingButtonConfig.saveIconSize(this, iconSize);
                        sendConfigUpdateBroadcast("UPDATE_ICON_SIZE");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitializing) {
                    return; 
                }
                String[] positions = {
                    "top_left", "top_center", "top_right",
                    "center_left", "center_right",
                    "bottom_left", "bottom_center", "bottom_right"
                };
                if (position >= 0 && position < positions.length) {
                    FloatingButtonConfig.savePosition(MainActivity.this, positions[position]);
                    sendConfigUpdateBroadcast("UPDATE_POSITION");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        String[] behaviors = {
            getString(R.string.dock_behavior_hide_on_action),
            getString(R.string.dock_behavior_hide_after_time)
        };
        ArrayAdapter<String> behaviorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, behaviors);
        behaviorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dockBehaviorSpinner.setAdapter(behaviorAdapter);


        dockBehaviorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] behaviorValues = {"hide_on_action", "hide_after_time"};
                if (position >= 0 && position < behaviorValues.length) {
                    String selectedBehavior = behaviorValues[position];


                    if ("hide_after_time".equals(selectedBehavior)) {
                        hideTimeoutContainer.setVisibility(View.VISIBLE);
                    } else {
                        hideTimeoutContainer.setVisibility(View.GONE);
                    }

                    if (!isInitializing) {
                        FloatingButtonConfig.saveDockBehavior(MainActivity.this, selectedBehavior);
                        sendConfigUpdateBroadcast("UPDATE_DOCK_CONFIG");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        hideTimeoutInput.addTextChangedListener(createTextWatcher(() -> {
            String value = hideTimeoutInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int timeoutSeconds = Integer.parseInt(value);
                    if (timeoutSeconds >= 1 && timeoutSeconds <= 60) {

                        int timeoutMs = timeoutSeconds * 1000;
                        FloatingButtonConfig.saveDockHideTimeout(MainActivity.this, timeoutMs);
                        sendConfigUpdateBroadcast("UPDATE_DOCK_CONFIG");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        borderRadiusInput.addTextChangedListener(createTextWatcher(() -> {
            String value = borderRadiusInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int borderRadius = Integer.parseInt(value);
                    if (borderRadius >= 0 && borderRadius <= 100) {
                        FloatingButtonConfig.saveBorderRadius(this, borderRadius);
                        sendConfigUpdateBroadcast("UPDATE_BACKGROUND");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        backgroundColorInput.addTextChangedListener(createTextWatcher(() -> {
            String value = backgroundColorInput.getText().toString();
            if (!TextUtils.isEmpty(value) && value.length() == 6) {
                try {
                    int bgColor = Integer.parseInt(value, 16);
                    FloatingButtonConfig.saveBackgroundColor(this, bgColor);
                    sendConfigUpdateBroadcast("UPDATE_BACKGROUND");
                } catch (NumberFormatException e) {

                }
            }
        }));


        backgroundAlphaInput.addTextChangedListener(createTextWatcher(() -> {
            String value = backgroundAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int bgAlpha = Integer.parseInt(value);
                    if (bgAlpha >= 0 && bgAlpha <= 255) {
                        FloatingButtonConfig.saveBackgroundAlpha(this, bgAlpha);
                        sendConfigUpdateBroadcast("UPDATE_BACKGROUND");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        iconColorInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconColorInput.getText().toString();
            if (!TextUtils.isEmpty(value) && value.length() == 6) {
                try {
                    int iconColor = Integer.parseInt(value, 16);
                    FloatingButtonConfig.saveIconColor(this, iconColor);
                    sendConfigUpdateBroadcast("UPDATE_ICON_SIZE");
                } catch (NumberFormatException e) {

                }
            }
        }));


        iconAlphaInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconAlpha = Integer.parseInt(value);
                    if (iconAlpha >= 0 && iconAlpha <= 255) {
                        FloatingButtonConfig.saveIconAlpha(this, iconAlpha);
                        sendConfigUpdateBroadcast("UPDATE_ICON_SIZE");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        iconGapInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconGapInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconGap = Integer.parseInt(value);
                    if (iconGap >= 0 && iconGap <= 50) {
                        FloatingButtonConfig.saveIconGap(this, iconGap);
                        sendConfigUpdateBroadcast("UPDATE_ICON_SIZE");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        iconPaddingInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconPaddingInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconPadding = Integer.parseInt(value);
                    if (iconPadding >= 0 && iconPadding <= 50) {
                        FloatingButtonConfig.saveIconPadding(this, iconPadding);
                        sendConfigUpdateBroadcast("UPDATE_ICON_SIZE");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        positionMarginXInput.addTextChangedListener(createTextWatcher(() -> {
            String value = positionMarginXInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int positionMarginX = Integer.parseInt(value);
                    if (positionMarginX >= -200 && positionMarginX <= 200) {
                        FloatingButtonConfig.savePositionMarginX(this, positionMarginX);
                        sendConfigUpdateBroadcast("UPDATE_POSITION");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        positionMarginYInput.addTextChangedListener(createTextWatcher(() -> {
            String value = positionMarginYInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int positionMarginY = Integer.parseInt(value);
                    if (positionMarginY >= -200 && positionMarginY <= 200) {
                        FloatingButtonConfig.savePositionMarginY(this, positionMarginY);
                        sendConfigUpdateBroadcast("UPDATE_POSITION");
                    }
                } catch (NumberFormatException e) {

                }
            }
        }));


        dockDraggableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isInitializing) {
                return; 
            }
            FloatingButtonConfig.saveDockDraggable(MainActivity.this, isChecked);

            saveAndRestartService();
        });
    }

    private TextWatcher createTextWatcher(Runnable onTextChanged) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isInitializing) {
                    onTextChanged.run();
                }
            }
        };
    }

    private void sendConfigUpdateBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void saveAndRestartService() {
        if (!Settings.canDrawOverlays(this)) {
            return;
        }

        try {
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            stopService(serviceIntent);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }, 300);
        } catch (Exception e) {
            Log.e(TAG, "Error al reiniciar servicio", e);
        }
    }

    private void startBackgroundService() {
        if (Settings.canDrawOverlays(this)) {
            try {
                Intent serviceIntent = new Intent(this, BackgroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Toast.makeText(this, getString(R.string.service_started), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error al iniciar servicio", e);
                Toast.makeText(this, getString(R.string.service_start_error, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.overlay_permission_required), Toast.LENGTH_SHORT).show();
            requestOverlayPermission();
        }
    }

    private void loadCurrentSettings() {

        int iconSize = FloatingButtonConfig.getIconSize(this);
        iconSizeInput.setHint(getString(R.string.hint_icon_size));
        iconSizeInput.setText(String.valueOf(iconSize));


        String position = FloatingButtonConfig.getPosition(this);
        String[] positions = {
            "top_left", "top_center", "top_right",
            "center_left", "center_right",
            "bottom_left", "bottom_center", "bottom_right"
        };
        String[] positionLabels = {
            getString(R.string.position_top_left),
            getString(R.string.position_top_center),
            getString(R.string.position_top_right),
            getString(R.string.position_center_left),
            getString(R.string.position_center_right),
            getString(R.string.position_bottom_left),
            getString(R.string.position_bottom_center),
            getString(R.string.position_bottom_right)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positionLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(adapter);


        for (int i = 0; i < positions.length; i++) {
            if (positions[i].equals(position)) {
                positionSpinner.setSelection(i);
                break;
            }
        }


        int borderRadius = FloatingButtonConfig.getBorderRadius(this);
        borderRadiusInput.setHint(getString(R.string.hint_border_radius));
        borderRadiusInput.setText(String.valueOf(borderRadius));


        int bgColor = FloatingButtonConfig.getBackgroundColor(this);
        backgroundColorInput.setText(String.format("%06X", bgColor & 0x00FFFFFF));


        int bgAlpha = FloatingButtonConfig.getBackgroundAlpha(this);
        backgroundAlphaInput.setHint(getString(R.string.hint_alpha));
        backgroundAlphaInput.setText(String.valueOf(bgAlpha));


        int iconColor = FloatingButtonConfig.getIconColor(this);
        iconColorInput.setText(String.format("%06X", iconColor & 0x00FFFFFF));


        int iconAlpha = FloatingButtonConfig.getIconAlpha(this);
        iconAlphaInput.setHint(getString(R.string.hint_icon_alpha));
        iconAlphaInput.setText(String.valueOf(iconAlpha));


        int iconGap = FloatingButtonConfig.getIconGap(this);
        iconGapInput.setHint(getString(R.string.hint_icon_gap));
        iconGapInput.setText(String.valueOf(iconGap));


        int iconPadding = FloatingButtonConfig.getIconPadding(this);
        iconPaddingInput.setHint(getString(R.string.hint_icon_padding));
        iconPaddingInput.setText(String.valueOf(iconPadding));


        FloatingButtonConfig.migrateOldPositionMargin(this);

        int positionMarginX = FloatingButtonConfig.getPositionMarginX(this);
        positionMarginXInput.setHint(getString(R.string.hint_margin));
        positionMarginXInput.setText(String.valueOf(positionMarginX));

        int positionMarginY = FloatingButtonConfig.getPositionMarginY(this);
        positionMarginYInput.setHint(getString(R.string.hint_margin));
        positionMarginYInput.setText(String.valueOf(positionMarginY));


        boolean isDraggable = FloatingButtonConfig.isDockDraggable(this);
        dockDraggableSwitch.setChecked(isDraggable);


        String behavior = FloatingButtonConfig.getDockBehavior(this);

        if ("fixed".equals(behavior)) {
            behavior = "hide_on_action";
            FloatingButtonConfig.saveDockBehavior(this, behavior);
        }
        String[] behaviorValues = {"hide_on_action", "hide_after_time"};
        int behaviorIndex = 0;
        for (int i = 0; i < behaviorValues.length; i++) {
            if (behaviorValues[i].equals(behavior)) {
                behaviorIndex = i;
                break;
            }
        }
        dockBehaviorSpinner.setSelection(behaviorIndex);


        if ("hide_after_time".equals(behavior)) {
            hideTimeoutContainer.setVisibility(View.VISIBLE);
        } else {
            hideTimeoutContainer.setVisibility(View.GONE);
        }


        int timeoutMs = FloatingButtonConfig.getDockHideTimeout(this);
        int timeoutSeconds = timeoutMs / 1000;
        hideTimeoutInput.setText(String.valueOf(timeoutSeconds));
    }

    private void saveSettings() {
        try {

            String iconSizeStr = iconSizeInput.getText().toString();
            if (!TextUtils.isEmpty(iconSizeStr)) {
                int iconSize = Integer.parseInt(iconSizeStr);
                if (iconSize > 0 && iconSize <= 200) {
                    FloatingButtonConfig.saveIconSize(this, iconSize);
                } else {
                    Toast.makeText(this, getString(R.string.error_icon_size_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String[] positions = {
                "top_left", "top_center", "top_right",
                "center_left", "center_right",
                "bottom_left", "bottom_center", "bottom_right"
            };
            int selectedPosition = positionSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < positions.length) {
                FloatingButtonConfig.savePosition(this, positions[selectedPosition]);
            }


            String borderRadiusStr = borderRadiusInput.getText().toString();
            if (!TextUtils.isEmpty(borderRadiusStr)) {
                int borderRadius = Integer.parseInt(borderRadiusStr);
                if (borderRadius >= 0 && borderRadius <= 100) {
                    FloatingButtonConfig.saveBorderRadius(this, borderRadius);
                } else {
                    Toast.makeText(this, getString(R.string.error_border_radius_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String bgColorStr = backgroundColorInput.getText().toString();
            if (!TextUtils.isEmpty(bgColorStr)) {
                try {
                    int bgColor = Integer.parseInt(bgColorStr, 16);
                    FloatingButtonConfig.saveBackgroundColor(this, bgColor);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.error_background_color_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String bgAlphaStr = backgroundAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(bgAlphaStr)) {
                int bgAlpha = Integer.parseInt(bgAlphaStr);
                if (bgAlpha >= 0 && bgAlpha <= 255) {
                    FloatingButtonConfig.saveBackgroundAlpha(this, bgAlpha);
                } else {
                    Toast.makeText(this, getString(R.string.error_alpha_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String iconColorStr = iconColorInput.getText().toString();
            if (!TextUtils.isEmpty(iconColorStr)) {
                try {
                    int iconColor = Integer.parseInt(iconColorStr, 16);
                    FloatingButtonConfig.saveIconColor(this, iconColor);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.error_icon_color_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String iconAlphaStr = iconAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(iconAlphaStr)) {
                int iconAlpha = Integer.parseInt(iconAlphaStr);
                if (iconAlpha >= 0 && iconAlpha <= 255) {
                    FloatingButtonConfig.saveIconAlpha(this, iconAlpha);
                } else {
                    Toast.makeText(this, getString(R.string.error_alpha_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String iconGapStr = iconGapInput.getText().toString();
            if (!TextUtils.isEmpty(iconGapStr)) {
                int iconGap = Integer.parseInt(iconGapStr);
                if (iconGap >= 0 && iconGap <= 50) {
                    FloatingButtonConfig.saveIconGap(this, iconGap);
                } else {
                    Toast.makeText(this, getString(R.string.error_gap_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String iconPaddingStr = iconPaddingInput.getText().toString();
            if (!TextUtils.isEmpty(iconPaddingStr)) {
                int iconPadding = Integer.parseInt(iconPaddingStr);
                if (iconPadding >= 0 && iconPadding <= 50) {
                    FloatingButtonConfig.saveIconPadding(this, iconPadding);
                } else {
                    Toast.makeText(this, getString(R.string.error_padding_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String positionMarginXStr = positionMarginXInput.getText().toString();
            if (!TextUtils.isEmpty(positionMarginXStr)) {
                int positionMarginX = Integer.parseInt(positionMarginXStr);
                if (positionMarginX >= -200 && positionMarginX <= 200) {
                    FloatingButtonConfig.savePositionMarginX(this, positionMarginX);
                } else {
                    Toast.makeText(this, getString(R.string.error_margin_x_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            String positionMarginYStr = positionMarginYInput.getText().toString();
            if (!TextUtils.isEmpty(positionMarginYStr)) {
                int positionMarginY = Integer.parseInt(positionMarginYStr);
                if (positionMarginY >= -200 && positionMarginY <= 200) {
                    FloatingButtonConfig.savePositionMarginY(this, positionMarginY);
                } else {
                    Toast.makeText(this, getString(R.string.error_margin_y_range), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Toast.makeText(this, getString(R.string.config_saved), Toast.LENGTH_SHORT).show();


            if (Settings.canDrawOverlays(this)) {
                try {
                    Intent serviceIntent = new Intent(this, BackgroundService.class);
                    stopService(serviceIntent);

                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent);
                        } else {
                            startService(serviceIntent);
                        }
                    }, 300);
                } catch (Exception e) {
                    Log.e(TAG, "Error al reiniciar servicio", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar configuración", e);
            Toast.makeText(this, getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDockAppsList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        List<DockApp> currentApps = DockAppManager.getDockApps(this);
        if (previousDockApps != null && !dockAppsEqual(previousDockApps, currentApps)) {

            previousDockApps = new ArrayList<>(currentApps);
            refreshDockAppsList();

            sendConfigUpdateBroadcast("UPDATE_ICONS");
        } else {

            refreshDockAppsList();
        }
    }

    private void checkForUpdates() {
        try {

            Toast.makeText(this, getString(R.string.update_checking), Toast.LENGTH_SHORT).show();


            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            final String currentVersion = packageInfo.versionName;


            UpdateChecker.checkForUpdates(this, new UpdateChecker.UpdateCheckCallback() {
                @Override
                public void onUpdateAvailable(UpdateChecker.UpdateInfo updateInfo) {
                    showUpdateDialog(updateInfo, currentVersion);
                }

                @Override
                public void onNoUpdateAvailable() {
                    Toast.makeText(MainActivity.this, getString(R.string.update_no_update), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, getString(R.string.update_check_error, error), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar actualizaciones", e);
            Toast.makeText(this, getString(R.string.update_check_error, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDialog(UpdateChecker.UpdateInfo updateInfo, String currentVersion) {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.update_available_title));
            builder.setMessage(getString(R.string.update_available_message, updateInfo.getVersionName(), currentVersion));

            builder.setPositiveButton(getString(R.string.update_download), (dialog, which) -> {

                UpdateChecker.downloadUpdate(MainActivity.this, updateInfo.getDownloadUrl(), updateInfo.getVersionName());
                Toast.makeText(MainActivity.this, getString(R.string.downloading_update_title, updateInfo.getVersionName()), Toast.LENGTH_LONG).show();
            });

            builder.setNeutralButton(getString(R.string.update_open_github), (dialog, which) -> {

                UpdateChecker.openGitHubReleases(MainActivity.this);
            });

            builder.setNegativeButton(getString(R.string.update_later), null);
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar diálogo de actualización", e);
            Toast.makeText(this, getString(R.string.error_generic, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}

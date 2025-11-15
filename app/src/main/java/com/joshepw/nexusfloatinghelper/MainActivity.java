package com.joshepw.nexusfloatinghelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private Button saveButton;
    private Button startServiceButton;
    private Button addAppButton;
    
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;
    private Handler settingsUpdateHandler;
    private Runnable settingsUpdateRunnable;
    private boolean isInitializing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar mapeo dinámico de iconos Material Symbols
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
        
        // Solicitar permiso de overlay si no lo tiene
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else {
            // Si ya tiene el permiso, iniciar el servicio automáticamente
            startBackgroundService();
        }
    }
    
    private void initViews() {
        dockAppsRecycler = findViewById(R.id.dock_apps_recycler);
        
        // Encontrar EditText dentro de los layouts incluidos
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
        
        View positionMarginYContainer = findViewById(R.id.position_margin_y_container);
        positionMarginYInput = positionMarginYContainer.findViewById(R.id.number_input);
        
        saveButton = findViewById(R.id.save_button);
        startServiceButton = findViewById(R.id.start_service_button);
        addAppButton = findViewById(R.id.add_app_button);
        
        // Configurar controles de incremento/decremento
        setupNumberControls();
    }
    
    private void setupNumberControls() {
        // Tamaño de icono: 1-200
        setupNumberControl(R.id.icon_size_container, 1, 200);
        
        // Border radius: 0-100
        setupNumberControl(R.id.border_radius_container, 0, 100);
        
        // Alpha de fondo: 0-255
        setupNumberControl(R.id.background_alpha_container, 0, 255);
        
        // Alpha de iconos: 0-255
        setupNumberControl(R.id.icon_alpha_container, 0, 255);
        
        // Gap de iconos: 0-50
        setupNumberControl(R.id.icon_gap_container, 0, 50);
        
        // Padding de iconos: 0-50
        setupNumberControl(R.id.icon_padding_container, 0, 50);
        
        // Margen de posición X: 0-200
        setupNumberControl(R.id.position_margin_x_container, 0, 200);
        
        // Margen de posición Y: 0-200
        setupNumberControl(R.id.position_margin_y_container, 0, 200);
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
                        Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                        // Iniciar servicio automáticamente cuando se concede el permiso
                        startBackgroundService();
                    } else {
                        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
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
                DockAppManager.removeDockApp(MainActivity.this, position);
                refreshDockAppsList();
                // Reiniciar servicio para aplicar cambios
                scheduleServiceRestart();
            }
            
            @Override
            public void onEditClick(int position) {
                try {
                    // Abrir actividad para editar (primero activity, luego icono)
                    List<DockApp> dockApps = DockAppManager.getDockApps(MainActivity.this);
                    if (position >= 0 && position < dockApps.size()) {
                        DockApp dockApp = dockApps.get(position);
                        if (dockApp != null) {
                            // Verificar si es una acción del sistema
                            if (dockApp.isAction()) {
                                // Para acciones, ir directamente a seleccionar icono
                                SystemAction action = SystemActionHelper.getActionById(dockApp.getActionId());
                                Intent intent = new Intent(MainActivity.this, SelectIconActivity.class);
                                intent.putExtra("action_id", dockApp.getActionId());
                                intent.putExtra("action_name", action != null ? action.getActionName() : dockApp.getActionId());
                                intent.putExtra("current_icon", dockApp.getMaterialIconName());
                                intent.putExtra("index", position);
                                intent.putExtra("is_action", true);
                                startActivity(intent);
                            } else {
                                // Es una app normal
                                String packageName = dockApp.getPackageName();
                                
                                // Verificar si la app tiene múltiples activities
                                if (ActivityUtils.hasMultipleActivities(MainActivity.this, packageName)) {
                                    // Abrir SelectActivityActivity para seleccionar activity
                                    Intent intent = new Intent(MainActivity.this, SelectActivityActivity.class);
                                    intent.putExtra("package_name", packageName);
                                    intent.putExtra("current_icon", dockApp.getMaterialIconName());
                                    intent.putExtra("index", position);
                                    intent.putExtra("is_editing", true); // Indicar que estamos editando
                                    startActivity(intent);
                                } else {
                                    // Si solo tiene una activity, ir directamente a seleccionar icono
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
                            Toast.makeText(MainActivity.this, "Error: App/Acción no encontrada", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error: Posición inválida", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error al editar app/acción", e);
                    Toast.makeText(MainActivity.this, "Error al editar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        dockAppsRecycler.setLayoutManager(new LinearLayoutManager(this));
        dockAppsRecycler.setAdapter(dockAppAdapter);
        refreshDockAppsList();
    }
    
    private List<DockApp> previousDockApps = null;
    
    private void refreshDockAppsList() {
        List<DockApp> dockApps = DockAppManager.getDockApps(this);
        dockAppAdapter.updateList(dockApps);
        
        // Detectar si hubo cambios en las apps del dock
        if (previousDockApps != null && !isInitializing) {
            if (!dockAppsEqual(previousDockApps, dockApps)) {
                // Hubo cambios, reiniciar servicio
                scheduleServiceRestart();
            }
        }
        previousDockApps = new ArrayList<>(dockApps); // Guardar copia para comparar
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
            
            // Comparar actionType primero
            String actionType1 = app1.getActionType();
            String actionType2 = app2.getActionType();
            if (actionType1 == null) actionType1 = "app";
            if (actionType2 == null) actionType2 = "app";
            if (!actionType1.equals(actionType2)) return false;
            
            // Si son acciones, comparar actionId
            if ("action".equals(actionType1)) {
                String actionId1 = app1.getActionId();
                String actionId2 = app2.getActionId();
                if (actionId1 == null) actionId1 = "";
                if (actionId2 == null) actionId2 = "";
                if (!actionId1.equals(actionId2)) return false;
            } else {
                // Si son apps, comparar package name
                String packageName1 = app1.getPackageName();
                String packageName2 = app2.getPackageName();
                if (packageName1 == null) packageName1 = "";
                if (packageName2 == null) packageName2 = "";
                if (!packageName1.equals(packageName2)) return false;
                
                // Comparar activity name
                String activity1 = app1.getActivityName();
                String activity2 = app2.getActivityName();
                if (activity1 == null) activity1 = "";
                if (activity2 == null) activity2 = "";
                if (!activity1.equals(activity2)) return false;
            }
            
            // Comparar icon name (siempre)
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
        
        startServiceButton.setOnClickListener(v -> {
            startBackgroundService();
        });
    }
    
    private void setupAutoSaveListeners() {
        settingsUpdateHandler = new Handler(Looper.getMainLooper());
        settingsUpdateRunnable = () -> {
            saveAndRestartService();
        };
        
        // Listener para tamaño de icono
        iconSizeInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconSizeInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconSize = Integer.parseInt(value);
                    if (iconSize > 0 && iconSize <= 200) {
                        FloatingButtonConfig.saveIconSize(this, iconSize);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para posición
        positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitializing) {
                    return; // No hacer nada durante la inicialización
                }
                String[] positions = {
                    "top_left", "top_center", "top_right",
                    "center_left", "center_center", "center_right",
                    "bottom_left", "bottom_center", "bottom_right"
                };
                if (position >= 0 && position < positions.length) {
                    FloatingButtonConfig.savePosition(MainActivity.this, positions[position]);
                    scheduleServiceRestart();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
        
        // Listener para border radius
        borderRadiusInput.addTextChangedListener(createTextWatcher(() -> {
            String value = borderRadiusInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int borderRadius = Integer.parseInt(value);
                    if (borderRadius >= 0 && borderRadius <= 100) {
                        FloatingButtonConfig.saveBorderRadius(this, borderRadius);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para color de fondo
        backgroundColorInput.addTextChangedListener(createTextWatcher(() -> {
            String value = backgroundColorInput.getText().toString();
            if (!TextUtils.isEmpty(value) && value.length() == 6) {
                try {
                    int bgColor = Integer.parseInt(value, 16);
                    FloatingButtonConfig.saveBackgroundColor(this, bgColor);
                    scheduleServiceRestart();
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para alpha de fondo
        backgroundAlphaInput.addTextChangedListener(createTextWatcher(() -> {
            String value = backgroundAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int bgAlpha = Integer.parseInt(value);
                    if (bgAlpha >= 0 && bgAlpha <= 255) {
                        FloatingButtonConfig.saveBackgroundAlpha(this, bgAlpha);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para color de iconos
        iconColorInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconColorInput.getText().toString();
            if (!TextUtils.isEmpty(value) && value.length() == 6) {
                try {
                    int iconColor = Integer.parseInt(value, 16);
                    FloatingButtonConfig.saveIconColor(this, iconColor);
                    scheduleServiceRestart();
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para alpha de iconos
        iconAlphaInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconAlpha = Integer.parseInt(value);
                    if (iconAlpha >= 0 && iconAlpha <= 255) {
                        FloatingButtonConfig.saveIconAlpha(this, iconAlpha);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para gap de iconos
        iconGapInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconGapInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconGap = Integer.parseInt(value);
                    if (iconGap >= 0 && iconGap <= 50) {
                        FloatingButtonConfig.saveIconGap(this, iconGap);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para padding de iconos
        iconPaddingInput.addTextChangedListener(createTextWatcher(() -> {
            String value = iconPaddingInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int iconPadding = Integer.parseInt(value);
                    if (iconPadding >= 0 && iconPadding <= 50) {
                        FloatingButtonConfig.saveIconPadding(this, iconPadding);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para margen de posición X
        positionMarginXInput.addTextChangedListener(createTextWatcher(() -> {
            String value = positionMarginXInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int positionMarginX = Integer.parseInt(value);
                    if (positionMarginX >= 0 && positionMarginX <= 200) {
                        FloatingButtonConfig.savePositionMarginX(this, positionMarginX);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
        
        // Listener para margen de posición Y
        positionMarginYInput.addTextChangedListener(createTextWatcher(() -> {
            String value = positionMarginYInput.getText().toString();
            if (!TextUtils.isEmpty(value)) {
                try {
                    int positionMarginY = Integer.parseInt(value);
                    if (positionMarginY >= 0 && positionMarginY <= 200) {
                        FloatingButtonConfig.savePositionMarginY(this, positionMarginY);
                        scheduleServiceRestart();
                    }
                } catch (NumberFormatException e) {
                    // Ignorar valores inválidos mientras se escribe
                }
            }
        }));
    }
    
    private TextWatcher createTextWatcher(Runnable onTextChanged) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No hacer nada
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No hacer nada
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!isInitializing) {
                    onTextChanged.run();
                }
            }
        };
    }
    
    private void scheduleServiceRestart() {
        // Cancelar cualquier reinicio pendiente
        settingsUpdateHandler.removeCallbacks(settingsUpdateRunnable);
        // Programar reinicio después de 500ms de inactividad
        settingsUpdateHandler.postDelayed(settingsUpdateRunnable, 500);
    }
    
    private void saveAndRestartService() {
        if (!Settings.canDrawOverlays(this)) {
            return;
        }
        
        try {
            Intent serviceIntent = new Intent(this, BackgroundService.class);
            stopService(serviceIntent);
            // Esperar un momento antes de reiniciar para asegurar que se detuvo
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
                Toast.makeText(this, "Servicio iniciado", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error al iniciar servicio", e);
                Toast.makeText(this, "Error al iniciar servicio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Necesitas el permiso de overlay primero", Toast.LENGTH_SHORT).show();
            requestOverlayPermission();
        }
    }
    
    private void loadCurrentSettings() {
        // Cargar tamaño de icono
        int iconSize = FloatingButtonConfig.getIconSize(this);
        iconSizeInput.setHint("24");
        iconSizeInput.setText(String.valueOf(iconSize));
        
        // Cargar posición
        String position = FloatingButtonConfig.getPosition(this);
        String[] positions = {
            "top_left", "top_center", "top_right",
            "center_left", "center_center", "center_right",
            "bottom_left", "bottom_center", "bottom_right"
        };
        String[] positionLabels = {
            "Superior Izquierda", "Superior Centro", "Superior Derecha",
            "Centro Izquierda", "Centro Centro", "Centro Derecha",
            "Inferior Izquierda", "Inferior Centro", "Inferior Derecha"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positionLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(adapter);
        
        // Seleccionar posición actual
        for (int i = 0; i < positions.length; i++) {
            if (positions[i].equals(position)) {
                positionSpinner.setSelection(i);
                break;
            }
        }
        
        // Cargar border radius
        int borderRadius = FloatingButtonConfig.getBorderRadius(this);
        borderRadiusInput.setHint("8");
        borderRadiusInput.setText(String.valueOf(borderRadius));
        
        // Cargar color de fondo
        int bgColor = FloatingButtonConfig.getBackgroundColor(this);
        backgroundColorInput.setText(String.format("%06X", bgColor & 0x00FFFFFF));
        
        // Cargar alpha de fondo
        int bgAlpha = FloatingButtonConfig.getBackgroundAlpha(this);
        backgroundAlphaInput.setHint("128");
        backgroundAlphaInput.setText(String.valueOf(bgAlpha));
        
        // Cargar color de iconos
        int iconColor = FloatingButtonConfig.getIconColor(this);
        iconColorInput.setText(String.format("%06X", iconColor & 0x00FFFFFF));
        
        // Cargar alpha de iconos
        int iconAlpha = FloatingButtonConfig.getIconAlpha(this);
        iconAlphaInput.setHint("255");
        iconAlphaInput.setText(String.valueOf(iconAlpha));
        
        // Cargar gap de iconos
        int iconGap = FloatingButtonConfig.getIconGap(this);
        iconGapInput.setHint("8");
        iconGapInput.setText(String.valueOf(iconGap));
        
        // Cargar padding de iconos
        int iconPadding = FloatingButtonConfig.getIconPadding(this);
        iconPaddingInput.setHint("0");
        iconPaddingInput.setText(String.valueOf(iconPadding));
        
        // Cargar margen de posición
        // Migrar valor antiguo si existe
        FloatingButtonConfig.migrateOldPositionMargin(this);
        
        int positionMarginX = FloatingButtonConfig.getPositionMarginX(this);
        positionMarginXInput.setHint("16");
        positionMarginXInput.setText(String.valueOf(positionMarginX));
        
        int positionMarginY = FloatingButtonConfig.getPositionMarginY(this);
        positionMarginYInput.setHint("16");
        positionMarginYInput.setText(String.valueOf(positionMarginY));
    }
    
    private void saveSettings() {
        try {
            // Validar y guardar tamaño de icono
            String iconSizeStr = iconSizeInput.getText().toString();
            if (!TextUtils.isEmpty(iconSizeStr)) {
                int iconSize = Integer.parseInt(iconSizeStr);
                if (iconSize > 0 && iconSize <= 200) {
                    FloatingButtonConfig.saveIconSize(this, iconSize);
                } else {
                    Toast.makeText(this, "Tamaño de icono debe estar entre 1 y 200", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Guardar posición
            String[] positions = {
                "top_left", "top_center", "top_right",
                "center_left", "center_center", "center_right",
                "bottom_left", "bottom_center", "bottom_right"
            };
            int selectedPosition = positionSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < positions.length) {
                FloatingButtonConfig.savePosition(this, positions[selectedPosition]);
            }
            
            // Validar y guardar border radius
            String borderRadiusStr = borderRadiusInput.getText().toString();
            if (!TextUtils.isEmpty(borderRadiusStr)) {
                int borderRadius = Integer.parseInt(borderRadiusStr);
                if (borderRadius >= 0 && borderRadius <= 100) {
                    FloatingButtonConfig.saveBorderRadius(this, borderRadius);
                } else {
                    Toast.makeText(this, "Border radius debe estar entre 0 y 100", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar color de fondo
            String bgColorStr = backgroundColorInput.getText().toString();
            if (!TextUtils.isEmpty(bgColorStr)) {
                try {
                    int bgColor = Integer.parseInt(bgColorStr, 16);
                    FloatingButtonConfig.saveBackgroundColor(this, bgColor);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Color de fondo inválido (use hex)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar alpha de fondo
            String bgAlphaStr = backgroundAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(bgAlphaStr)) {
                int bgAlpha = Integer.parseInt(bgAlphaStr);
                if (bgAlpha >= 0 && bgAlpha <= 255) {
                    FloatingButtonConfig.saveBackgroundAlpha(this, bgAlpha);
                } else {
                    Toast.makeText(this, "Alpha debe estar entre 0 y 255", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar color de iconos
            String iconColorStr = iconColorInput.getText().toString();
            if (!TextUtils.isEmpty(iconColorStr)) {
                try {
                    int iconColor = Integer.parseInt(iconColorStr, 16);
                    FloatingButtonConfig.saveIconColor(this, iconColor);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Color de iconos inválido (use hex)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar alpha de iconos
            String iconAlphaStr = iconAlphaInput.getText().toString();
            if (!TextUtils.isEmpty(iconAlphaStr)) {
                int iconAlpha = Integer.parseInt(iconAlphaStr);
                if (iconAlpha >= 0 && iconAlpha <= 255) {
                    FloatingButtonConfig.saveIconAlpha(this, iconAlpha);
                } else {
                    Toast.makeText(this, "Alpha debe estar entre 0 y 255", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar gap de iconos
            String iconGapStr = iconGapInput.getText().toString();
            if (!TextUtils.isEmpty(iconGapStr)) {
                int iconGap = Integer.parseInt(iconGapStr);
                if (iconGap >= 0 && iconGap <= 50) {
                    FloatingButtonConfig.saveIconGap(this, iconGap);
                } else {
                    Toast.makeText(this, "Gap debe estar entre 0 y 50", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar padding de iconos
            String iconPaddingStr = iconPaddingInput.getText().toString();
            if (!TextUtils.isEmpty(iconPaddingStr)) {
                int iconPadding = Integer.parseInt(iconPaddingStr);
                if (iconPadding >= 0 && iconPadding <= 50) {
                    FloatingButtonConfig.saveIconPadding(this, iconPadding);
                } else {
                    Toast.makeText(this, "Padding debe estar entre 0 y 50", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar margen de posición X
            String positionMarginXStr = positionMarginXInput.getText().toString();
            if (!TextUtils.isEmpty(positionMarginXStr)) {
                int positionMarginX = Integer.parseInt(positionMarginXStr);
                if (positionMarginX >= 0 && positionMarginX <= 200) {
                    FloatingButtonConfig.savePositionMarginX(this, positionMarginX);
                } else {
                    Toast.makeText(this, "Margen X debe estar entre 0 y 200", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validar y guardar margen de posición Y
            String positionMarginYStr = positionMarginYInput.getText().toString();
            if (!TextUtils.isEmpty(positionMarginYStr)) {
                int positionMarginY = Integer.parseInt(positionMarginYStr);
                if (positionMarginY >= 0 && positionMarginY <= 200) {
                    FloatingButtonConfig.savePositionMarginY(this, positionMarginY);
                } else {
                    Toast.makeText(this, "Margen Y debe estar entre 0 y 200", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
            
            // Reiniciar servicio para aplicar cambios
            if (Settings.canDrawOverlays(this)) {
                try {
                    Intent serviceIntent = new Intent(this, BackgroundService.class);
                    stopService(serviceIntent);
                    // Esperar un momento antes de reiniciar para asegurar que se detuvo
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
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar lista y detectar cambios (que reiniciará el servicio si es necesario)
        refreshDockAppsList();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refrescar lista cuando se vuelve de agregar/editar
        // refreshDockAppsList() detectará cambios y reiniciará el servicio si es necesario
        refreshDockAppsList();
    }
}

package com.joshepw.nexusfloatinghelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SelectIconActivity extends AppCompatActivity {
    private RecyclerView iconsRecycler;
    private IconSelectionAdapter adapter;
    private EditText searchInput;
    private List<String> allIconNames;
    private String packageName;
    private String actionId; // Para acciones del sistema
    private String actionName; // Nombre de la acción
    private String currentIcon;
    private int index = -1;
    private boolean isAction = false;
    private Handler searchHandler;
    private Runnable searchRunnable;
    private static final int SEARCH_DEBOUNCE_MS = 300;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_icon);
        
        try {
            packageName = getIntent().getStringExtra("package_name");
            actionId = getIntent().getStringExtra("action_id");
            actionName = getIntent().getStringExtra("action_name");
            String activityName = getIntent().getStringExtra("activity_name");
            currentIcon = getIntent().getStringExtra("current_icon");
            index = getIntent().getIntExtra("index", -1);
            isAction = getIntent().getBooleanExtra("is_action", false);
            
            // Validar que tenemos packageName o actionId
            if ((packageName == null || packageName.isEmpty()) && (actionId == null || actionId.isEmpty())) {
                android.util.Log.e("SelectIconActivity", "Package name y action_id son null o vacíos");
                finish();
                return;
            }
            
            iconsRecycler = findViewById(R.id.icons_recycler);
            searchInput = findViewById(R.id.search_input);
            
            allIconNames = MaterialIconHelper.getAllIconNames();
            if (allIconNames == null) {
                allIconNames = new ArrayList<>();
            }
            
            final String finalActivityName = activityName;
            final String finalPackageName = packageName; // Para usar en el listener
            adapter = new IconSelectionAdapter(this, iconName -> {
                try {
                    // Guardar app o acción en el dock
                    List<DockApp> dockApps = DockAppManager.getDockApps(this);
                    if (index >= 0 && index < dockApps.size()) {
                        // Editar app/acción existente
                        DockApp dockApp = dockApps.get(index);
                        if (dockApp != null) {
                            dockApp.setMaterialIconName(iconName);
                            if (isAction) {
                                dockApp.setActionId(actionId);
                                dockApp.setActionType("action");
                            } else {
                                if (finalActivityName != null && !finalActivityName.isEmpty()) {
                                    dockApp.setActivityName(finalActivityName);
                                }
                                dockApp.setActionType("app");
                            }
                            DockAppManager.updateDockApp(this, index, dockApp);
                        }
                    } else {
                        // Agregar nueva app o acción
                        int newIndex = dockApps != null ? dockApps.size() : 0;
                        DockApp newDockApp;
                        if (isAction) {
                            // Crear acción del sistema
                            newDockApp = new DockApp(actionId, iconName, newIndex, true);
                        } else {
                            // Crear app normal
                            newDockApp = new DockApp(finalPackageName, iconName, finalActivityName, newIndex);
                            newDockApp.setActionType("app");
                            if (finalActivityName != null && !finalActivityName.isEmpty()) {
                                newDockApp.setActivityName(finalActivityName);
                            }
                            newDockApp.setActionId(null);
                        }
                        DockAppManager.addDockApp(this, newDockApp);
                    }
                    
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("SelectIconActivity", "Error al guardar app/acción", e);
                    android.widget.Toast.makeText(this, "Error al guardar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            }, finalPackageName != null ? finalPackageName : "");
            
            iconsRecycler.setLayoutManager(new GridLayoutManager(this, 4));
            iconsRecycler.setAdapter(adapter);
            
            // Inicializar Handler para debounce
            searchHandler = new Handler(Looper.getMainLooper());
            
            // Aplicar filtros después de configurar el adapter
            applyFilters("");
            
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Cancelar búsqueda anterior si existe
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }
                    
                    // Crear nueva búsqueda con debounce
                    final String query = s.toString();
                    searchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            applyFilters(query);
                        }
                    };
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            android.util.Log.e("SelectIconActivity", "Error en onCreate", e);
            android.widget.Toast.makeText(this, "Error al cargar iconos: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void applyFilters(String query) {
        try {
            if (allIconNames == null) {
                allIconNames = new ArrayList<>();
            }
            
            List<String> filteredIcons = new ArrayList<>();
            
            // Agregar icono nativo como primera opción (solo si no hay búsqueda o la búsqueda coincide)
            String lowerQuery = query != null ? query.toLowerCase() : "";
            if (query == null || query.isEmpty() || lowerQuery.contains("native") || lowerQuery.contains("icono") || lowerQuery.contains("nativo")) {
                filteredIcons.add("native");
            }
            
            // Agregar iconos Material filtrados
            for (String iconName : allIconNames) {
                if (iconName == null) continue;
                
                if (query == null || query.isEmpty() || iconName.toLowerCase().contains(lowerQuery)) {
                    filteredIcons.add(iconName);
                }
            }
            
            if (adapter != null) {
                adapter.updateList(filteredIcons);
            }
        } catch (Exception e) {
            android.util.Log.e("SelectIconActivity", "Error en applyFilters", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar Handler para evitar memory leaks
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}


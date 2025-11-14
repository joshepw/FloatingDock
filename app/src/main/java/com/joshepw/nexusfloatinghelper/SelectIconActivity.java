package com.joshepw.nexusfloatinghelper;

import android.content.Intent;
import android.os.Bundle;
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
    private String currentIcon;
    private int index = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_icon);
        
        try {
            packageName = getIntent().getStringExtra("package_name");
            currentIcon = getIntent().getStringExtra("current_icon");
            index = getIntent().getIntExtra("index", -1);
            
            if (packageName == null || packageName.isEmpty()) {
                android.util.Log.e("SelectIconActivity", "Package name es null o vacío");
                finish();
                return;
            }
            
            iconsRecycler = findViewById(R.id.icons_recycler);
            searchInput = findViewById(R.id.search_input);
            
            allIconNames = MaterialIconHelper.getAllIconNames();
            if (allIconNames == null) {
                allIconNames = new ArrayList<>();
            }
            
            adapter = new IconSelectionAdapter(this, iconName -> {
                try {
                    // Guardar app en el dock
                    List<DockApp> dockApps = DockAppManager.getDockApps(this);
                    if (index >= 0 && index < dockApps.size()) {
                        // Editar app existente
                        DockApp dockApp = dockApps.get(index);
                        if (dockApp != null) {
                            dockApp.setMaterialIconName(iconName);
                            DockAppManager.updateDockApp(this, index, dockApp);
                        }
                    } else {
                        // Agregar nueva app
                        int newIndex = dockApps != null ? dockApps.size() : 0;
                        DockApp newDockApp = new DockApp(packageName, iconName, newIndex);
                        DockAppManager.addDockApp(this, newDockApp);
                    }
                    
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("SelectIconActivity", "Error al guardar app", e);
                    android.widget.Toast.makeText(this, "Error al guardar: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
            
            iconsRecycler.setLayoutManager(new GridLayoutManager(this, 4));
            iconsRecycler.setAdapter(adapter);
            
            // Aplicar filtros después de configurar el adapter
            applyFilters("");
            
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters(s.toString());
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
            String lowerQuery = query != null ? query.toLowerCase() : "";
            
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
}


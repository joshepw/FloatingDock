package com.joshepw.nexusfloatinghelper;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SelectActivityActivity extends AppCompatActivity {
    private RecyclerView activitiesRecycler;
    private EditText searchInput;
    private ActivitySelectionAdapter adapter;
    private String packageName;
    private List<ActivityInfo> allOrderedActivities; // Lista completa para filtrar
    private int originalSeparatorPosition = -1;
    private boolean isEditing = false; // Indica si estamos editando una app existente
    private int editIndex = -1; // Índice de la app que se está editando
    private String currentIcon = null; // Icono actual si estamos editando
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_activity);
        
        try {
            // Obtener package name del intent
            packageName = getIntent().getStringExtra("package_name");
            if (packageName == null || packageName.isEmpty()) {
                android.util.Log.e("SelectActivityActivity", "Package name no proporcionado");
                Toast.makeText(this, "Error: Package name no proporcionado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Verificar si estamos editando una app existente
            isEditing = getIntent().getBooleanExtra("is_editing", false);
            editIndex = getIntent().getIntExtra("index", -1);
            currentIcon = getIntent().getStringExtra("current_icon");
            
            activitiesRecycler = findViewById(R.id.activities_recycler);
            searchInput = findViewById(R.id.search_input);
            
            // Obtener todas las activities (lanzables y no lanzables)
            List<ActivityInfo> allActivities = ActivityUtils.getAllActivities(this, packageName);
            
            android.util.Log.d("SelectActivityActivity", "Activities encontradas: " + (allActivities != null ? allActivities.size() : 0));
            
            if (allActivities == null || allActivities.isEmpty()) {
                android.util.Log.w("SelectActivityActivity", "No se encontraron activities para " + packageName);
                // Si no hay activities, ir directamente a seleccionar icono
                Intent intent = new Intent(SelectActivityActivity.this, SelectIconActivity.class);
                intent.putExtra("package_name", packageName);
                startActivity(intent);
                finish();
                return;
            }
            
            // Separar y ordenar activities
            List<ActivityInfo> launchableActivities = new java.util.ArrayList<>();
            List<ActivityInfo> nonLaunchableActivities = new java.util.ArrayList<>();
            ActivityInfo mainLauncherActivity = null;
            
            for (ActivityInfo activity : allActivities) {
                if (activity.isMainLauncher()) {
                    mainLauncherActivity = activity; // Guardar la principal
                } else if (activity.isLaunchable()) {
                    launchableActivities.add(activity);
                } else {
                    nonLaunchableActivities.add(activity);
                }
            }
            
            // Construir lista ordenada: principal primero, luego lanzables, luego no lanzables
            List<ActivityInfo> orderedActivities = new java.util.ArrayList<>();
            if (mainLauncherActivity != null) {
                orderedActivities.add(mainLauncherActivity);
            }
            orderedActivities.addAll(launchableActivities);
            
            // Calcular posición del separador
            int separatorPosition = -1;
            if (!nonLaunchableActivities.isEmpty() && !orderedActivities.isEmpty()) {
                separatorPosition = orderedActivities.size();
                orderedActivities.addAll(nonLaunchableActivities);
            }
            
            // Guardar lista completa y posición del separador para filtrado
            allOrderedActivities = new ArrayList<>(orderedActivities);
            originalSeparatorPosition = separatorPosition;
            
            adapter = new ActivitySelectionAdapter(this, new ActivitySelectionAdapter.OnActivityClickListener() {
                @Override
                public void onActivityClick(ActivityInfo activityInfo) {
                    try {
                        android.util.Log.d("SelectActivityActivity", "Activity seleccionada: " + activityInfo.getActivityName());
                        Intent intent = new Intent(SelectActivityActivity.this, SelectIconActivity.class);
                        intent.putExtra("package_name", activityInfo.getPackageName());
                        intent.putExtra("activity_name", activityInfo.getActivityName());
                        
                        // Si estamos editando, pasar la información de edición
                        if (isEditing) {
                            intent.putExtra("index", editIndex);
                            if (currentIcon != null) {
                                intent.putExtra("current_icon", currentIcon);
                            }
                        }
                        
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        android.util.Log.e("SelectActivityActivity", "Error al seleccionar activity", e);
                        Toast.makeText(SelectActivityActivity.this, "Error al seleccionar activity", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            activitiesRecycler.setLayoutManager(new LinearLayoutManager(this));
            activitiesRecycler.setAdapter(adapter);
            
            // Configurar separador y actualizar lista
            applyFilters("");
            
            // Configurar búsqueda
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
            android.util.Log.e("SelectActivityActivity", "Error en onCreate", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void applyFilters(String query) {
        try {
            if (allOrderedActivities == null) {
                return;
            }
            
            String searchQuery = query != null ? query.toLowerCase().trim() : "";
            
            if (searchQuery.isEmpty()) {
                // Sin filtro, mostrar todas
                adapter.setSeparatorPosition(originalSeparatorPosition);
                adapter.updateList(allOrderedActivities);
            } else {
                // Filtrar activities
                List<ActivityInfo> filteredActivities = new ArrayList<>();
                List<ActivityInfo> filteredLaunchable = new ArrayList<>();
                List<ActivityInfo> filteredNonLaunchable = new ArrayList<>();
                ActivityInfo filteredMainLauncher = null;
                
                for (ActivityInfo activity : allOrderedActivities) {
                    String label = activity.getLabel() != null ? activity.getLabel().toLowerCase() : "";
                    String activityName = activity.getActivityName() != null ? activity.getActivityName().toLowerCase() : "";
                    
                    // Buscar en label y nombre de activity
                    if (label.contains(searchQuery) || activityName.contains(searchQuery)) {
                        if (activity.isMainLauncher()) {
                            filteredMainLauncher = activity;
                        } else if (activity.isLaunchable()) {
                            filteredLaunchable.add(activity);
                        } else {
                            filteredNonLaunchable.add(activity);
                        }
                    }
                }
                
                // Construir lista filtrada ordenada
                List<ActivityInfo> filteredOrdered = new ArrayList<>();
                if (filteredMainLauncher != null) {
                    filteredOrdered.add(filteredMainLauncher);
                }
                filteredOrdered.addAll(filteredLaunchable);
                
                // Calcular posición del separador en la lista filtrada
                int filteredSeparatorPosition = -1;
                if (!filteredNonLaunchable.isEmpty() && !filteredOrdered.isEmpty()) {
                    filteredSeparatorPosition = filteredOrdered.size();
                    filteredOrdered.addAll(filteredNonLaunchable);
                }
                
                adapter.setSeparatorPosition(filteredSeparatorPosition);
                adapter.updateList(filteredOrdered);
            }
        } catch (Exception e) {
            android.util.Log.e("SelectActivityActivity", "Error en applyFilters", e);
        }
    }
}


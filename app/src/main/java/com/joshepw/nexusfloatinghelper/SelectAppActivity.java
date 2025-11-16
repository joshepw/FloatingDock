package com.joshepw.nexusfloatinghelper;

import android.content.Intent;
import android.content.pm.PackageManager;
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

public class SelectAppActivity extends AppCompatActivity {
    private RecyclerView appsRecycler;
    private AppSelectionAdapter adapter;
    private EditText searchInput;
    private List<AppInfo> allApps;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_app);
        
        try {
            appsRecycler = findViewById(R.id.apps_recycler);
            searchInput = findViewById(R.id.search_input);
            
            // Obtener apps con manejo de errores
            allApps = AppUtils.getAllInstalledApps(this);
            if (allApps == null) {
                allApps = new ArrayList<>();
            }
            
            adapter = new AppSelectionAdapter(this, new AppSelectionAdapter.OnAppClickListener() {
                @Override
                public void onAppClick(AppInfo appInfo) {
                    try {
                        String packageName = appInfo.getPackageName();
                        
                        // Verificar si la app tiene múltiples activities
                        if (ActivityUtils.hasMultipleActivities(SelectAppActivity.this, packageName)) {
                            // Abrir Activity para seleccionar activity
                            Intent intent = new Intent(SelectAppActivity.this, SelectActivityActivity.class);
                            intent.putExtra("package_name", packageName);
                            startActivity(intent);
                            finish();
                        } else {
                            // Ir directamente a seleccionar icono
                            Intent intent = new Intent(SelectAppActivity.this, SelectIconActivity.class);
                            intent.putExtra("package_name", packageName);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SelectAppActivity", "Error al abrir SelectIconActivity", e);
                        Toast.makeText(SelectAppActivity.this, "Error al abrir selección de icono", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onManualEntryClick() {
                    showManualEntryDialog();
                }
                
                @Override
                public void onAddActionClick() {
                    // Abrir Activity para seleccionar acción del sistema
                    Intent intent = new Intent(SelectAppActivity.this, SelectActionActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            
            appsRecycler.setLayoutManager(new LinearLayoutManager(this));
            appsRecycler.setAdapter(adapter);
            
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
            android.util.Log.e("SelectAppActivity", "Error en onCreate", e);
            Toast.makeText(this, "Error al cargar la lista de apps: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void applyFilters(String query) {
        try {
            if (allApps == null) {
                allApps = new ArrayList<>();
            }
            
            List<AppInfo> filteredApps = new ArrayList<>();
            List<AppInfo> launchableApps = new ArrayList<>();
            List<AppInfo> nonLaunchableApps = new ArrayList<>();
            
            String lowerQuery = query != null ? query.toLowerCase() : "";
            
            for (AppInfo app : allApps) {
                if (app == null) continue;
                
                try {
                    String appName = app.getAppName() != null ? app.getAppName() : "";
                    String packageName = app.getPackageName() != null ? app.getPackageName() : "";
                    
                    if (query == null || query.isEmpty() || 
                        appName.toLowerCase().contains(lowerQuery) ||
                        packageName.toLowerCase().contains(lowerQuery)) {
                        if (app.isLaunchable()) {
                            launchableApps.add(app);
                        } else {
                            nonLaunchableApps.add(app);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("SelectAppActivity", "Error al procesar app: " + e.getMessage());
                }
            }
            
            filteredApps.addAll(launchableApps);
            filteredApps.addAll(nonLaunchableApps);
            
            if (adapter != null) {
                adapter.updateList(filteredApps);
                
                // Configurar separador
                if (!nonLaunchableApps.isEmpty() && !launchableApps.isEmpty()) {
                    adapter.setSeparatorPosition(launchableApps.size());
                } else {
                    adapter.setSeparatorPosition(-1);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SelectAppActivity", "Error en applyFilters", e);
        }
    }
    
    private void showManualEntryDialog() {
        try {
            // Crear EditText con mejor estilo
            EditText input = new EditText(this);
            input.setHint(getString(R.string.hint_package_name));
            input.setPadding(32, 32, 32, 32);
            input.setSingleLine(true);
            input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI);
            
            // Crear diálogo con estilo Material
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.manual_entry_title))
                .setMessage(getString(R.string.manual_entry_message))
                .setView(input)
                .setPositiveButton(getString(R.string.button_continue), (dialog, which) -> {
                    try {
                        String packageName = input.getText().toString().trim();
                        if (!packageName.isEmpty()) {
                            if (validatePackageName(packageName)) {
                                Intent intent = new Intent(SelectAppActivity.this, SelectIconActivity.class);
                                intent.putExtra("package_name", packageName);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, getString(R.string.error_package_not_found), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.error_enter_package), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("SelectAppActivity", "Error en diálogo manual", e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show();
        } catch (Exception e) {
            android.util.Log.e("SelectAppActivity", "Error al mostrar diálogo manual", e);
            Toast.makeText(this, "Error al mostrar diálogo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validatePackageName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
}


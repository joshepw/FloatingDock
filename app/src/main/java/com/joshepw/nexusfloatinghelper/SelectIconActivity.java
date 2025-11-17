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
    private String actionId; 
    private String actionName; 
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


            int spanCount = calculateSpanCount();

            final String finalActivityName = activityName;
            final String finalPackageName = packageName; 
            adapter = new IconSelectionAdapter(this, iconName -> {
                try {

                    List<DockApp> dockApps = DockAppManager.getDockApps(this);
                    if (index >= 0 && index < dockApps.size()) {

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


                            android.content.Intent updateIntent = new android.content.Intent("UPDATE_ICONS");
                            updateIntent.setPackage(getPackageName());
                            sendBroadcast(updateIntent);
                        }
                    } else {

                        int newIndex = dockApps != null ? dockApps.size() : 0;
                        DockApp newDockApp;
                        if (isAction) {

                            newDockApp = new DockApp(actionId, iconName, newIndex, true);
                        } else {

                            newDockApp = new DockApp(finalPackageName, iconName, finalActivityName, newIndex);
                            newDockApp.setActionType("app");
                            if (finalActivityName != null && !finalActivityName.isEmpty()) {
                                newDockApp.setActivityName(finalActivityName);
                            }
                            newDockApp.setActionId(null);
                        }
                        DockAppManager.addDockApp(this, newDockApp);
                    }


                    android.content.Intent updateIntent = new android.content.Intent("UPDATE_ICONS");
                    updateIntent.setPackage(getPackageName());
                    sendBroadcast(updateIntent);

                    finish();
                } catch (Exception e) {
                    android.util.Log.e("SelectIconActivity", "Error al guardar app/acción", e);
                    android.widget.Toast.makeText(this, getString(R.string.error_saving, e.getMessage()), android.widget.Toast.LENGTH_SHORT).show();
                }
            }, finalPackageName != null ? finalPackageName : "");

            iconsRecycler.setLayoutManager(new GridLayoutManager(this, spanCount));
            iconsRecycler.setAdapter(adapter);


            searchHandler = new Handler(Looper.getMainLooper());


            applyFilters("");

            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }


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
            android.widget.Toast.makeText(this, getString(R.string.error_loading_icons, e.getMessage()), android.widget.Toast.LENGTH_LONG).show();
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
            if (query == null || query.isEmpty() || lowerQuery.contains("native") || lowerQuery.contains("icono") || lowerQuery.contains("nativo")) {
                filteredIcons.add("native");
            }


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

    private int calculateSpanCount() {

        android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidthPx = displayMetrics.widthPixels;


        float density = displayMetrics.density;
        int screenWidthDp = (int) (screenWidthPx / density);


        int minCellSizeDp = 100;


        int spanCount = screenWidthDp / minCellSizeDp;


        if (spanCount < 3) {
            spanCount = 3;
        } else if (spanCount > 8) {
            spanCount = 8;
        }

        return spanCount;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}


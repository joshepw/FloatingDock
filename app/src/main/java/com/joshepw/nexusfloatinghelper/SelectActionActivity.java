package com.joshepw.nexusfloatinghelper;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SelectActionActivity extends AppCompatActivity {
    private RecyclerView actionsRecycler;
    private ActionSelectionAdapter adapter;
    private EditText searchInput;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_action);
        
        try {
            actionsRecycler = findViewById(R.id.actions_recycler);
            searchInput = findViewById(R.id.search_input);
            
            adapter = new ActionSelectionAdapter(this, action -> {
                try {
                    // Ir a seleccionar icono para la acción
                    Intent intent = new Intent(SelectActionActivity.this, SelectIconActivity.class);
                    intent.putExtra("action_id", action.getActionId());
                    intent.putExtra("action_name", action.getActionName());
                    intent.putExtra("is_action", true);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    android.util.Log.e("SelectActionActivity", "Error al abrir SelectIconActivity", e);
                    android.widget.Toast.makeText(this, getString(R.string.error_selecting_action), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
            
            actionsRecycler.setLayoutManager(new LinearLayoutManager(this));
            actionsRecycler.setAdapter(adapter);
            
            // Cargar todas las acciones
            List<SystemAction> allActions = SystemActionHelper.getAllSystemActions(this);
            adapter.updateList(allActions);
            
            // Configurar búsqueda
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString());
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            android.util.Log.e("SelectActionActivity", "Error en onCreate", e);
            android.widget.Toast.makeText(this, getString(R.string.error_loading_actions, e.getMessage()), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }
}


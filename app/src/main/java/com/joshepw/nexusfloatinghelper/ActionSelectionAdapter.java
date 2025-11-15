package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActionSelectionAdapter extends RecyclerView.Adapter<ActionSelectionAdapter.ActionViewHolder> {
    private List<SystemAction> actions;
    private Context context;
    private OnActionClickListener listener;
    
    public interface OnActionClickListener {
        void onActionClick(SystemAction action);
    }
    
    public ActionSelectionAdapter(Context context, OnActionClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.actions = new ArrayList<>();
    }
    
    public void updateList(List<SystemAction> newList) {
        this.actions = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void filter(String query) {
        List<SystemAction> filtered = new ArrayList<>();
        if (TextUtils.isEmpty(query)) {
            filtered.addAll(SystemActionHelper.getAllSystemActions());
        } else {
            String lowerQuery = query.toLowerCase();
            for (SystemAction action : SystemActionHelper.getAllSystemActions()) {
                if (action.getActionName().toLowerCase().contains(lowerQuery) ||
                    action.getDescription().toLowerCase().contains(lowerQuery)) {
                    filtered.add(action);
                }
            }
        }
        updateList(filtered);
    }
    
    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_action_selection, parent, false);
        return new ActionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        SystemAction action = actions.get(position);
        if (action != null) {
            holder.bind(action);
        }
    }
    
    @Override
    public int getItemCount() {
        return actions.size();
    }
    
    class ActionViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView nameText;
        TextView descriptionText;
        
        ActionViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.action_icon);
            nameText = itemView.findViewById(R.id.action_name);
            descriptionText = itemView.findViewById(R.id.action_description);
        }
        
        void bind(SystemAction action) {
            if (action == null) return;
            
            nameText.setText(action.getActionName());
            descriptionText.setText(action.getDescription());
            
            // Mostrar icono Material
            MaterialIconDrawable iconDrawable = new MaterialIconDrawable(context);
            iconDrawable.setIcon(action.getIconName());
            iconDrawable.setSize(48);
            iconDrawable.setColor(0xFF000000); // Negro
            iconDrawable.setBounds(0, 0, 48, 48);
            iconView.setImageDrawable(iconDrawable);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActionClick(action);
                }
            });
        }
    }
}


package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DockAppAdapter extends RecyclerView.Adapter<DockAppAdapter.ViewHolder> {
    private List<DockApp> dockApps;
    private Context context;
    private OnDockAppClickListener listener;
    
    public interface OnDockAppClickListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
    }
    
    public DockAppAdapter(Context context, OnDockAppClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.dockApps = new ArrayList<>();
    }
    
    public void updateList(List<DockApp> newList) {
        this.dockApps = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dock_app, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DockApp dockApp = dockApps.get(position);
        
        holder.packageNameText.setText(dockApp.getPackageName());
        holder.iconNameText.setText("Icono: " + dockApp.getMaterialIconName());
        
        // Mostrar icono Material de la app
        MaterialIconDrawable iconDrawable = new MaterialIconDrawable(context);
        iconDrawable.setIcon(dockApp.getMaterialIconName());
        iconDrawable.setSize(48);
        iconDrawable.setColor(0xFF000000); // Negro para la lista
        iconDrawable.setBounds(0, 0, 48, 48);
        holder.iconView.setImageDrawable(iconDrawable);
        holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        // Icono de editar
        MaterialIconDrawable editIcon = new MaterialIconDrawable(context);
        editIcon.setIcon("edit");
        editIcon.setSize(24);
        editIcon.setColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        editIcon.setBounds(0, 0, 24, 24);
        holder.editButton.setImageDrawable(editIcon);
        holder.editButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(position);
            }
        });
        
        // Icono de eliminar
        MaterialIconDrawable deleteIcon = new MaterialIconDrawable(context);
        deleteIcon.setIcon("delete");
        deleteIcon.setSize(24);
        deleteIcon.setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        deleteIcon.setBounds(0, 0, 24, 24);
        holder.deleteButton.setImageDrawable(deleteIcon);
        holder.deleteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return dockApps.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView packageNameText;
        TextView iconNameText;
        ImageView deleteButton;
        ImageView editButton;
        
        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.dock_app_icon);
            packageNameText = itemView.findViewById(R.id.dock_app_package);
            iconNameText = itemView.findViewById(R.id.dock_app_icon_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_icon_button);
        }
    }
}


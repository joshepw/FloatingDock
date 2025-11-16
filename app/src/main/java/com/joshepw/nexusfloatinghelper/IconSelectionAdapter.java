package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class IconSelectionAdapter extends RecyclerView.Adapter<IconSelectionAdapter.ViewHolder> {
    private static final String NATIVE_ICON = "native";
    
    private List<String> iconNames;
    private Context context;
    private OnIconClickListener listener;
    private String packageName;
    
    public interface OnIconClickListener {
        void onIconClick(String iconName);
    }
    
    public IconSelectionAdapter(Context context, OnIconClickListener listener, String packageName) {
        this.context = context;
        this.listener = listener;
        this.packageName = packageName;
        this.iconNames = new ArrayList<>();
    }
    
    public void updateList(List<String> newList) {
        this.iconNames = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon_selection, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String iconName = iconNames.get(position);
        
        if (NATIVE_ICON.equals(iconName)) {
            // Mostrar icono nativo del app
            holder.iconNameText.setText(context.getString(R.string.icon_native_text));
            try {
                android.content.pm.PackageManager pm = context.getPackageManager();
                android.graphics.drawable.Drawable appIcon = pm.getApplicationIcon(packageName);
                holder.iconView.setImageDrawable(appIcon);
                holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } catch (Exception e) {
                // Si no se puede obtener el icono, mostrar un icono por defecto
                holder.iconView.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon));
            }
        } else {
            // Mostrar icono Material
            holder.iconNameText.setText(iconName);
            MaterialIconDrawable iconDrawable = new MaterialIconDrawable(context);
            iconDrawable.setIcon(iconName);
            iconDrawable.setSize(48);
            iconDrawable.setColor(0xFF000000); // Negro para la lista
            iconDrawable.setBounds(0, 0, 48, 48);
            holder.iconView.setImageDrawable(iconDrawable);
            holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIconClick(iconName);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return iconNames.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView iconNameText;
        
        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.icon_preview);
            iconNameText = itemView.findViewById(R.id.icon_name);
        }
    }
}


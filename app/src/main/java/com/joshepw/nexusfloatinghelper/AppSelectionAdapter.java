package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.content.pm.PackageManager;
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

public class AppSelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MANUAL_ENTRY = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_APP = 2;
    
    private List<AppInfo> apps;
    private Context context;
    private OnAppClickListener listener;
    private int separatorPosition = -1;
    
    public interface OnAppClickListener {
        void onAppClick(AppInfo appInfo);
        void onManualEntryClick();
    }
    
    public AppSelectionAdapter(Context context, OnAppClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.apps = new ArrayList<>();
    }
    
    public void updateList(List<AppInfo> newList) {
        this.apps = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSeparatorPosition(int position) {
        this.separatorPosition = position;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_MANUAL_ENTRY;
        }
        if (separatorPosition > 0 && position == separatorPosition + 1) {
            return TYPE_SEPARATOR;
        }
        return TYPE_APP;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_MANUAL_ENTRY) {
            View view = inflater.inflate(R.layout.item_manual_entry, parent, false);
            return new ManualEntryViewHolder(view);
        } else if (viewType == TYPE_SEPARATOR) {
            View view = inflater.inflate(R.layout.item_separator, parent, false);
            return new SeparatorViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_app_selection, parent, false);
            return new AppViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ManualEntryViewHolder) {
            ((ManualEntryViewHolder) holder).itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onManualEntryClick();
                }
            });
        } else if (holder instanceof SeparatorViewHolder) {
            // Separador, no necesita binding
        } else if (holder instanceof AppViewHolder) {
            int appPosition = position - 1; // Restar manual entry
            if (separatorPosition > 0 && position > separatorPosition + 1) {
                appPosition--; // Restar separator
            }
            
            if (appPosition >= 0 && appPosition < apps.size()) {
                AppInfo appInfo = apps.get(appPosition);
                ((AppViewHolder) holder).bind(appInfo);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        int count = 1; // Manual entry
        if (separatorPosition > 0) {
            count++; // Separator
        }
        count += apps.size(); // Apps
        return count;
    }
    
    static class ManualEntryViewHolder extends RecyclerView.ViewHolder {
        ManualEntryViewHolder(View itemView) {
            super(itemView);
        }
    }
    
    static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        SeparatorViewHolder(View itemView) {
            super(itemView);
        }
    }
    
    class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView nameText;
        TextView packageText;
        ImageView multipleActivitiesIndicator;
        
        AppViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.app_icon);
            nameText = itemView.findViewById(R.id.app_name);
            packageText = itemView.findViewById(R.id.app_package);
            multipleActivitiesIndicator = itemView.findViewById(R.id.multiple_activities_indicator);
        }
        
        void bind(AppInfo appInfo) {
            if (appInfo == null) {
                return;
            }
            
            try {
                String appName = appInfo.getAppName();
                String packageName = appInfo.getPackageName();
                
                if (nameText != null) {
                    nameText.setText(appName != null ? appName : "");
                }
                if (packageText != null) {
                    packageText.setText(packageName != null ? packageName : "");
                }
                if (iconView != null) {
                    android.graphics.drawable.Drawable icon = appInfo.getIcon();
                    if (icon == null) {
                        icon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon);
                    }
                    iconView.setImageDrawable(icon);
                }
                
                // Ocultar indicador de múltiples activities (ya no se muestra)
                if (multipleActivitiesIndicator != null) {
                    multipleActivitiesIndicator.setVisibility(View.GONE);
                }
                
                itemView.setOnClickListener(v -> {
                    if (listener != null && appInfo != null) {
                        listener.onAppClick(appInfo);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AppSelectionAdapter", "Error al bindear app", e);
            }
        }
    }
}


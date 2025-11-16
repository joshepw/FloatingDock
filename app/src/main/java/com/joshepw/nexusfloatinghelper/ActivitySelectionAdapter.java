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

public class ActivitySelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SEPARATOR = 0;
    private static final int TYPE_ACTIVITY = 1;
    
    private List<ActivityInfo> activities;
    private Context context;
    private OnActivityClickListener listener;
    private int separatorPosition = -1;
    
    public interface OnActivityClickListener {
        void onActivityClick(ActivityInfo activityInfo);
    }
    
    public ActivitySelectionAdapter(Context context, OnActivityClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.activities = new ArrayList<>();
    }
    
    public void updateList(List<ActivityInfo> newList) {
        this.activities = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSeparatorPosition(int position) {
        this.separatorPosition = position;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (separatorPosition > 0 && position == separatorPosition) {
            return TYPE_SEPARATOR;
        }
        return TYPE_ACTIVITY;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_SEPARATOR) {
            View view = inflater.inflate(R.layout.item_separator, parent, false);
            return new SeparatorViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_activity_selection, parent, false);
            return new ActivityViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SeparatorViewHolder) {
            // Separador, actualizar texto
            TextView separatorText = ((SeparatorViewHolder) holder).itemView.findViewById(R.id.separator_text);
            if (separatorText != null) {
                separatorText.setText(context.getString(R.string.non_launchable_activities));
            }
        } else if (holder instanceof ActivityViewHolder) {
            int activityPosition = position;
            if (separatorPosition > 0 && position > separatorPosition) {
                activityPosition--; // Restar separador
            }
            
            if (activityPosition >= 0 && activityPosition < activities.size()) {
                ActivityInfo activityInfo = activities.get(activityPosition);
                ((ActivityViewHolder) holder).bind(activityInfo);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        int count = activities != null ? activities.size() : 0;
        if (separatorPosition > 0) {
            count++; // Agregar separador
        }
        return count;
    }
    
    static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        SeparatorViewHolder(View itemView) {
            super(itemView);
        }
    }
    
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView labelText;
        TextView nameText;
        ImageView launchableIcon;
        
        ActivityViewHolder(View itemView) {
            super(itemView);
            labelText = itemView.findViewById(R.id.activity_label);
            nameText = itemView.findViewById(R.id.activity_name);
            launchableIcon = itemView.findViewById(R.id.launchable_icon);
        }
        
        void bind(ActivityInfo activityInfo) {
            if (activityInfo == null) {
                return;
            }
            
            try {
                String label = activityInfo.getLabel();
                String activityName = activityInfo.getActivityName();
                
                // Mostrar solo el nombre de la clase sin el package completo
                String shortName = activityName;
                if (activityName != null && activityName.contains(".")) {
                    shortName = activityName.substring(activityName.lastIndexOf('.') + 1);
                }
                
                if (labelText != null) {
                    labelText.setText(label != null ? label : "");
                }
                if (nameText != null) {
                    nameText.setText(shortName != null ? shortName : "");
                }
                
                // Mostrar icono solo si es la activity principal del launcher
                if (launchableIcon != null) {
                    if (activityInfo.isMainLauncher()) {
                        launchableIcon.setVisibility(View.VISIBLE);
                        // Crear icono Material "launch" o "star" para indicar que es la principal
                        MaterialIconDrawable iconDrawable = new MaterialIconDrawable(context);
                        iconDrawable.setIcon("star");
                        iconDrawable.setSize(24);
                        iconDrawable.setColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                        iconDrawable.setBounds(0, 0, 24, 24);
                        launchableIcon.setImageDrawable(iconDrawable);
                        launchableIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    } else {
                        launchableIcon.setVisibility(View.GONE);
                    }
                }
                
                itemView.setOnClickListener(v -> {
                    if (listener != null && activityInfo != null) {
                        listener.onActivityClick(activityInfo);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ActivitySelectionAdapter", "Error al bindear activity", e);
            }
        }
    }
}


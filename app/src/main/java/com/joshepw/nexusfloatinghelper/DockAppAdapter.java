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

public class DockAppAdapter extends RecyclerView.Adapter<DockAppAdapter.ViewHolder> {
    private List<DockApp> dockApps;
    private Context context;
    private OnDockAppClickListener listener;

    public interface OnDockAppClickListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
        void onAutoStartClick(int position);
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


        String displayText;
        if (dockApp.isAction()) {

            SystemAction action = SystemActionHelper.getActionById(dockApp.getActionId(), context);
            if (action != null) {
                displayText = action.getActionName();
            } else {
                displayText = context.getString(R.string.action_prefix, dockApp.getActionId());
            }
        } else {

            displayText = dockApp.getPackageName();
            if (dockApp.getActivityName() != null && !dockApp.getActivityName().isEmpty()) {
                String activityShortName = dockApp.getActivityName();

                if (activityShortName.contains(".")) {
                    activityShortName = activityShortName.substring(activityShortName.lastIndexOf('.') + 1);
                }
                displayText += " (" + activityShortName + ")";
            }
        }
        holder.packageNameText.setText(displayText);

        String iconName = dockApp.getMaterialIconName();


        if ("native".equals(iconName)) {

            holder.iconNameText.setText(context.getString(R.string.icon_native));
            if (dockApp.isAction()) {

                holder.iconView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon));
                holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                try {
                    PackageManager pm = context.getPackageManager();
                    android.graphics.drawable.Drawable appIcon = pm.getApplicationIcon(dockApp.getPackageName());
                    holder.iconView.setImageDrawable(appIcon);
                    holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } catch (Exception e) {

                    holder.iconView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon));
                    holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        } else {

            holder.iconNameText.setText(context.getString(R.string.icon_format, iconName));
            MaterialIconDrawable iconDrawable = new MaterialIconDrawable(context);
            iconDrawable.setIcon(iconName);
            iconDrawable.setSize(48);
            iconDrawable.setColor(0xFF000000); 
            iconDrawable.setBounds(0, 0, 48, 48);
            holder.iconView.setImageDrawable(iconDrawable);
            holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }


        if (!dockApp.isAction()) {
            boolean isAutoStart = FloatingButtonConfig.isAutoStartApp(context, 
                dockApp.getPackageName(), dockApp.getActivityName());
            MaterialIconDrawable autoStartIcon = new MaterialIconDrawable(context);
            autoStartIcon.setIcon("rocket_launch"); 
            autoStartIcon.setSize(24);

            int autoStartColor = isAutoStart ? 
                ContextCompat.getColor(context, android.R.color.holo_green_dark) :
                ContextCompat.getColor(context, android.R.color.darker_gray);
            autoStartIcon.setColor(autoStartColor);
            autoStartIcon.setBounds(0, 0, 24, 24);
            holder.autoStartButton.setImageDrawable(autoStartIcon);
            holder.autoStartButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.autoStartButton.setVisibility(View.VISIBLE);
            holder.autoStartButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAutoStartClick(position);
                }
            });
        } else {

            holder.autoStartButton.setVisibility(View.GONE);
        }


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
        ImageView autoStartButton;
        ImageView dragHandle;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.dock_app_icon);
            packageNameText = itemView.findViewById(R.id.dock_app_package);
            iconNameText = itemView.findViewById(R.id.dock_app_icon_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_icon_button);
            autoStartButton = itemView.findViewById(R.id.auto_start_button);
            dragHandle = itemView.findViewById(R.id.drag_handle);


            if (dragHandle != null) {
                MaterialIconDrawable dragIcon = new MaterialIconDrawable(context);
                dragIcon.setIcon("drag_handle");
                dragIcon.setSize(24);
                dragIcon.setColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                dragIcon.setBounds(0, 0, 24, 24);
                dragHandle.setImageDrawable(dragIcon);
                dragHandle.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }
    }
}


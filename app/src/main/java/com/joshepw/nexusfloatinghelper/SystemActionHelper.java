package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class SystemActionHelper {
    
    public static List<SystemAction> getAllSystemActions(Context context) {
        List<SystemAction> actions = new ArrayList<>();
        
        // Navegación
        actions.add(new SystemAction("home", context.getString(R.string.action_home), "home", context.getString(R.string.action_home_desc)));
        actions.add(new SystemAction("back", context.getString(R.string.action_back), "arrow_back", context.getString(R.string.action_back_desc)));
        actions.add(new SystemAction("recent", context.getString(R.string.action_recent), "apps", context.getString(R.string.action_recent_desc)));
        
        // Volumen
        actions.add(new SystemAction("volume_up", context.getString(R.string.action_volume_up), "volume_up", context.getString(R.string.action_volume_up_desc)));
        actions.add(new SystemAction("volume_down", context.getString(R.string.action_volume_down), "volume_down", context.getString(R.string.action_volume_down_desc)));
        actions.add(new SystemAction("volume_mute", context.getString(R.string.action_volume_mute), "volume_off", context.getString(R.string.action_volume_mute_desc)));
        
        // Medios
        actions.add(new SystemAction("media_play", context.getString(R.string.action_media_play), "play_arrow", context.getString(R.string.action_media_play_desc)));
        actions.add(new SystemAction("media_pause", context.getString(R.string.action_media_pause), "pause", context.getString(R.string.action_media_pause_desc)));
        actions.add(new SystemAction("media_next", context.getString(R.string.action_media_next), "skip_next", context.getString(R.string.action_media_next_desc)));
        actions.add(new SystemAction("media_previous", context.getString(R.string.action_media_previous), "skip_previous", context.getString(R.string.action_media_previous_desc)));
        
        // Llamadas
        actions.add(new SystemAction("answer_call", context.getString(R.string.action_answer_call), "call", context.getString(R.string.action_answer_call_desc)));
        actions.add(new SystemAction("end_call", context.getString(R.string.action_end_call), "call_end", context.getString(R.string.action_end_call_desc)));
        
        // Asistente de voz
        actions.add(new SystemAction("voice_assistant", context.getString(R.string.action_voice_assistant), "mic", context.getString(R.string.action_voice_assistant_desc)));
        
        // Dock
        actions.add(new SystemAction("hide_dock", context.getString(R.string.action_hide_dock), "visibility_off", context.getString(R.string.action_hide_dock_desc)));
        
        return actions;
    }
    
    public static SystemAction getActionById(String actionId, Context context) {
        List<SystemAction> actions = getAllSystemActions(context);
        for (SystemAction action : actions) {
            if (action.getActionId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }
}


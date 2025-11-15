package com.joshepw.nexusfloatinghelper;

import java.util.ArrayList;
import java.util.List;

public class SystemActionHelper {
    
    public static List<SystemAction> getAllSystemActions() {
        List<SystemAction> actions = new ArrayList<>();
        
        // Navegación
        actions.add(new SystemAction("home", "Home", "home", "Ir a la pantalla principal"));
        actions.add(new SystemAction("back", "Back", "arrow_back", "Volver atrás"));
        actions.add(new SystemAction("recent", "Aplicaciones recientes", "apps", "Ver aplicaciones recientes"));
        
        // Volumen
        actions.add(new SystemAction("volume_up", "Subir volumen", "volume_up", "Aumentar volumen"));
        actions.add(new SystemAction("volume_down", "Bajar volumen", "volume_down", "Disminuir volumen"));
        actions.add(new SystemAction("volume_mute", "Silenciar", "volume_off", "Silenciar volumen"));
        
        // Medios
        actions.add(new SystemAction("media_play", "Reproducir", "play_arrow", "Reproducir medios"));
        actions.add(new SystemAction("media_pause", "Pausar", "pause", "Pausar reproducción"));
        actions.add(new SystemAction("media_next", "Siguiente", "skip_next", "Siguiente canción/pista"));
        actions.add(new SystemAction("media_previous", "Anterior", "skip_previous", "Canción/pista anterior"));
        
        // Llamadas
        actions.add(new SystemAction("answer_call", "Contestar llamada", "call", "Contestar llamada entrante"));
        actions.add(new SystemAction("end_call", "Colgar llamada", "call_end", "Terminar llamada"));
        
        // Asistente de voz
        actions.add(new SystemAction("voice_assistant", "Asistente de voz", "mic", "Activar asistente de voz"));
        
        return actions;
    }
    
    public static SystemAction getActionById(String actionId) {
        List<SystemAction> actions = getAllSystemActions();
        for (SystemAction action : actions) {
            if (action.getActionId().equals(actionId)) {
                return action;
            }
        }
        return null;
    }
}


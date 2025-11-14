package com.joshepw.nexusfloatinghelper;

import java.util.HashMap;
import java.util.Map;

public class MaterialSymbolsMapper {
    private static final Map<String, String> ICON_MAP = new HashMap<>();
    
    static {
        // Mapeo completo de nombres de iconos Material a sus códigos Unicode
        // Los códigos están en el rango E000-F8FF (Private Use Area)
        
        // Navegación básica
        ICON_MAP.put("home", "\ue88a");
        ICON_MAP.put("menu", "\ue5d2");
        ICON_MAP.put("arrow_back", "\ue5c4");
        ICON_MAP.put("arrow_forward", "\ue5c8");
        ICON_MAP.put("arrow_upward", "\ue5d8");
        ICON_MAP.put("arrow_downward", "\ue5db");
        ICON_MAP.put("chevron_left", "\ue5cb");
        ICON_MAP.put("chevron_right", "\ue5cc");
        ICON_MAP.put("close", "\ue5cd");
        ICON_MAP.put("check", "\ue5ca");
        ICON_MAP.put("cancel", "\ue5c9");
        
        // Acciones comunes
        ICON_MAP.put("add", "\ue145");
        ICON_MAP.put("delete", "\ue872");
        ICON_MAP.put("edit", "\ue254");
        ICON_MAP.put("save", "\ue161");
        ICON_MAP.put("search", "\ue8b6");
        ICON_MAP.put("refresh", "\ue5d5");
        ICON_MAP.put("share", "\ue80d");
        ICON_MAP.put("more_vert", "\ue5d4");
        ICON_MAP.put("more_horiz", "\ue5d3");
        
        // Aplicaciones y sistema
        ICON_MAP.put("apps", "\ue5c3");
        ICON_MAP.put("settings", "\ue8b8");
        ICON_MAP.put("info", "\ue88e");
        ICON_MAP.put("help", "\ue887");
        ICON_MAP.put("warning", "\ue002");
        ICON_MAP.put("error", "\ue000");
        
        // Favoritos y calificaciones
        ICON_MAP.put("favorite", "\ue87d");
        ICON_MAP.put("favorite_border", "\ue87e");
        ICON_MAP.put("star", "\ue838");
        ICON_MAP.put("star_border", "\ue83a");
        ICON_MAP.put("star_half", "\ue839");
        ICON_MAP.put("check_circle", "\ue86c");
        ICON_MAP.put("radio_button_checked", "\ue837");
        ICON_MAP.put("radio_button_unchecked", "\ue836");
        
        // Clima y ambiente
        ICON_MAP.put("ac_unit", "\ueb3b");
        ICON_MAP.put("mode_fan", "\uef57");
        ICON_MAP.put("wb_sunny", "\ue430");
        ICON_MAP.put("wb_cloudy", "\ue42d");
        ICON_MAP.put("brightness_high", "\ue1a2");
        ICON_MAP.put("brightness_low", "\ue1a1");
        ICON_MAP.put("brightness_auto", "\ue1ab");
        
        // Comunicación
        ICON_MAP.put("phone", "\ue0cd");
        ICON_MAP.put("email", "\ue0be");
        ICON_MAP.put("message", "\ue0c9");
        ICON_MAP.put("chat", "\ue0b7");
        ICON_MAP.put("forum", "\ue0bf");
        ICON_MAP.put("comment", "\ue0b9");
        
        // Multimedia
        ICON_MAP.put("camera", "\ue3af");
        ICON_MAP.put("photo", "\ue412");
        ICON_MAP.put("video", "\ue04b");
        ICON_MAP.put("music_note", "\ue405");
        ICON_MAP.put("play_arrow", "\ue037");
        ICON_MAP.put("pause", "\ue034");
        ICON_MAP.put("stop", "\ue047");
        ICON_MAP.put("volume_up", "\ue050");
        ICON_MAP.put("volume_down", "\ue04d");
        ICON_MAP.put("volume_off", "\ue04e");
        ICON_MAP.put("movie", "\ue02c");
        ICON_MAP.put("tv", "\ue333");
        
        // Dispositivos
        ICON_MAP.put("computer", "\ue30a");
        ICON_MAP.put("laptop", "\ue31e");
        ICON_MAP.put("tablet", "\ue32f");
        ICON_MAP.put("smartphone", "\ue32c");
        ICON_MAP.put("watch", "\ue334");
        ICON_MAP.put("headphones", "\ue310");
        ICON_MAP.put("speaker", "\ue32d");
        ICON_MAP.put("keyboard", "\ue312");
        ICON_MAP.put("mouse", "\ue323");
        
        // Navegación y ubicación
        ICON_MAP.put("location_on", "\ue567");
        ICON_MAP.put("map", "\ue55b");
        ICON_MAP.put("directions", "\ue52e");
        ICON_MAP.put("navigation", "\ue55f");
        ICON_MAP.put("gps_fixed", "\ue1b3");
        ICON_MAP.put("gps_not_fixed", "\ue1b4");
        
        // Comercio
        ICON_MAP.put("shopping_cart", "\ue8cc");
        ICON_MAP.put("shopping_bag", "\ue8cb");
        ICON_MAP.put("store", "\ue8d1");
        ICON_MAP.put("local_offer", "\ue54e");
        ICON_MAP.put("payment", "\ue8a1");
        ICON_MAP.put("credit_card", "\ue870");
        
        // Lugares
        ICON_MAP.put("restaurant", "\ue56c");
        ICON_MAP.put("hotel", "\ue53a");
        ICON_MAP.put("local_hospital", "\ue548");
        ICON_MAP.put("school", "\ue80c");
        ICON_MAP.put("work", "\ue8f9");
        ICON_MAP.put("home_work", "\uea09");
        
        // Archivos y documentos
        ICON_MAP.put("folder", "\ue2c7");
        ICON_MAP.put("file", "\ue24d");
        ICON_MAP.put("description", "\ue873");
        ICON_MAP.put("insert_drive_file", "\ue24d");
        ICON_MAP.put("cloud", "\ue2bd");
        ICON_MAP.put("cloud_upload", "\ue2c3");
        ICON_MAP.put("cloud_download", "\ue2c0");
        
        // Seguridad
        ICON_MAP.put("lock", "\ue897");
        ICON_MAP.put("lock_open", "\ue898");
        ICON_MAP.put("security", "\ue32a");
        ICON_MAP.put("vpn_key", "\ue0da");
        ICON_MAP.put("fingerprint", "\ue90d");
        
        // Personas
        ICON_MAP.put("person", "\ue7fd");
        ICON_MAP.put("people", "\ue7fb");
        ICON_MAP.put("group", "\ue7ef");
        ICON_MAP.put("account_circle", "\ue853");
        ICON_MAP.put("face", "\ue87c");
        
        // Notificaciones y tiempo
        ICON_MAP.put("notifications", "\ue7f4");
        ICON_MAP.put("notifications_off", "\ue7f6");
        ICON_MAP.put("alarm", "\ue855");
        ICON_MAP.put("schedule", "\ue8b5");
        ICON_MAP.put("event", "\ue878");
        ICON_MAP.put("calendar_today", "\ue935");
        ICON_MAP.put("access_time", "\ue192");
        ICON_MAP.put("timer", "\ue425");
        
        // Herramientas y utilidades
        ICON_MAP.put("build", "\ue869");
        ICON_MAP.put("construction", "\ue869");
        ICON_MAP.put("settings_applications", "\ue8b8");
        ICON_MAP.put("tune", "\ue429");
        ICON_MAP.put("filter_list", "\ue152");
        ICON_MAP.put("sort", "\ue164");
        
        // Red y conectividad
        ICON_MAP.put("wifi", "\ue63e");
        ICON_MAP.put("wifi_off", "\ue648");
        ICON_MAP.put("bluetooth", "\ue1a7");
        ICON_MAP.put("bluetooth_connected", "\ue1a8");
        ICON_MAP.put("signal_wifi_4_bar", "\ue1d8");
        ICON_MAP.put("signal_cellular_4_bar", "\ue1c8");
        
        // Batería y energía
        ICON_MAP.put("battery_full", "\ue1a3");
        ICON_MAP.put("battery_charging_full", "\ue1a3");
        ICON_MAP.put("battery_std", "\ue1a5");
        ICON_MAP.put("power", "\ue8c6");
        
        // Iluminación y visualización
        ICON_MAP.put("brightness_1", "\ue3a6");
        ICON_MAP.put("brightness_2", "\ue3a7");
        ICON_MAP.put("brightness_3", "\ue3a8");
        ICON_MAP.put("brightness_4", "\ue3a9");
        ICON_MAP.put("brightness_5", "\ue3aa");
        ICON_MAP.put("brightness_6", "\ue3ab");
        ICON_MAP.put("brightness_7", "\ue3ac");
        ICON_MAP.put("contrast", "\ue3b1");
        
        // Deportes y entretenimiento
        ICON_MAP.put("sports", "\uea30");
        ICON_MAP.put("fitness_center", "\ueb43");
        ICON_MAP.put("gamepad", "\ue30f");
        ICON_MAP.put("casino", "\ueb40");
        ICON_MAP.put("theater_comedy", "\uea66");
        
        // Transporte
        ICON_MAP.put("directions_car", "\ue531");
        ICON_MAP.put("directions_bus", "\ue530");
        ICON_MAP.put("directions_walk", "\ue536");
        ICON_MAP.put("directions_bike", "\ue52f");
        ICON_MAP.put("flight", "\ue539");
        ICON_MAP.put("train", "\ue570");
        
        // Más iconos comunes
        ICON_MAP.put("bookmark", "\ue866");
        ICON_MAP.put("bookmark_border", "\ue867");
        ICON_MAP.put("flag", "\ue153");
        ICON_MAP.put("tag", "\ue9ef");
        ICON_MAP.put("label", "\ue892");
        ICON_MAP.put("print", "\ue8ad");
        ICON_MAP.put("download", "\ue2c4");
        ICON_MAP.put("upload", "\ue2c6");
        ICON_MAP.put("sync", "\ue627");
        ICON_MAP.put("backup", "\ue2c0");
        ICON_MAP.put("restore", "\ue8b3");
        ICON_MAP.put("history", "\ue889");
        ICON_MAP.put("update", "\ue923");
        ICON_MAP.put("autorenew", "\ue863");
        ICON_MAP.put("loop", "\ue028");
        ICON_MAP.put("repeat", "\ue040");
        ICON_MAP.put("shuffle", "\ue043");
        
        // Agregar más iconos según sea necesario
    }
    
    public static String getUnicode(String iconName) {
        String unicode = ICON_MAP.get(iconName);
        if (unicode == null) {
            // Si no se encuentra, intentar con variaciones comunes
            if (iconName.endsWith("_outline")) {
                String baseName = iconName.replace("_outline", "_border");
                unicode = ICON_MAP.get(baseName);
            }
            if (unicode == null) {
                // Default a "apps" si no se encuentra
                unicode = ICON_MAP.getOrDefault("apps", "\ue5c3");
            }
        }
        return unicode;
    }
    
    public static boolean hasIcon(String iconName) {
        return ICON_MAP.containsKey(iconName);
    }
}

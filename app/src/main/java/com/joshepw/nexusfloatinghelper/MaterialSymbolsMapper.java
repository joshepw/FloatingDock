package com.joshepw.nexusfloatinghelper;

import java.util.HashMap;
import java.util.Map;

public class MaterialSymbolsMapper {
    private static final Map<String, String> ICON_MAP = new HashMap<>();
    
    static {
        // Mapeo completo de nombres de iconos Material Symbols Outlined a sus códigos Unicode
        // Los códigos están en el rango E000-F8FF (Private Use Area)
        // Material Symbols usa los mismos nombres que Material Icons, por lo que este mapeo es compatible
        
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
        ICON_MAP.put("mode_fan", "\uf168");
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
        ICON_MAP.put("navigation", "\ue55d");
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
        
        // Transporte y vehículos
        ICON_MAP.put("directions_car", "\ue531");
        ICON_MAP.put("directions_bus", "\ue530");
        ICON_MAP.put("directions_walk", "\ue536");
        ICON_MAP.put("directions_bike", "\ue52f");
        ICON_MAP.put("flight", "\ue539");
        ICON_MAP.put("train", "\ue570");
        ICON_MAP.put("car_rental", "\ue55a");
        ICON_MAP.put("car_repair", "\ue55a");
        ICON_MAP.put("local_gas_station", "\ue567");
        ICON_MAP.put("tire_repair", "\uebc8");
        ICON_MAP.put("garage", "\ueb02");
        ICON_MAP.put("speed", "\ue9e4");
        ICON_MAP.put("traffic", "\ue565");
        ICON_MAP.put("route", "\ueacd");
        ICON_MAP.put("alt_route", "\ue184");
        
        // Radio y audio de vehículo
        ICON_MAP.put("radio", "\ue1e0");
        ICON_MAP.put("tune", "\ue429");
        ICON_MAP.put("graphic_eq", "\ue1b8");
        ICON_MAP.put("equalizer", "\ue01d");
        ICON_MAP.put("bass", "\ue1b8");
        ICON_MAP.put("treble", "\ue1b8");
        ICON_MAP.put("volume_up", "\ue050");
        ICON_MAP.put("volume_down", "\ue04d");
        ICON_MAP.put("volume_off", "\ue04e");
        ICON_MAP.put("volume_mute", "\ue04e");
        ICON_MAP.put("skip_next", "\ue044");
        ICON_MAP.put("skip_previous", "\ue045");
        ICON_MAP.put("fast_forward", "\ue01f");
        ICON_MAP.put("fast_rewind", "\ue020");
        ICON_MAP.put("repeat", "\ue040");
        ICON_MAP.put("repeat_one", "\ue041");
        ICON_MAP.put("shuffle", "\ue043");
        ICON_MAP.put("queue_music", "\ue03d");
        ICON_MAP.put("playlist_add", "\ue03b");
        ICON_MAP.put("library_music", "\ue02f");
        ICON_MAP.put("radio_button_checked", "\ue837");
        ICON_MAP.put("radio_button_unchecked", "\ue836");
        ICON_MAP.put("fm", "\ue1e0");
        ICON_MAP.put("am", "\ue1e0");
        
        // Apps comunes y servicios
        ICON_MAP.put("phone_android", "\ue324");
        ICON_MAP.put("phone_iphone", "\ue325");
        ICON_MAP.put("tablet_android", "\ue32f");
        ICON_MAP.put("tablet", "\ue32f");
        ICON_MAP.put("laptop", "\ue31e");
        ICON_MAP.put("computer", "\ue30a");
        ICON_MAP.put("desktop_windows", "\ue30c");
        ICON_MAP.put("desktop_mac", "\ue30b");
        ICON_MAP.put("watch", "\ue334");
        ICON_MAP.put("smartphone", "\ue32c");
        
        // Navegación y mapas
        ICON_MAP.put("map", "\ue55b");
        ICON_MAP.put("map_outline", "\ue55b");
        ICON_MAP.put("satellite", "\ue562");
        ICON_MAP.put("terrain", "\ue564");
        ICON_MAP.put("layers", "\ue53b");
        ICON_MAP.put("my_location", "\ue55c");
        ICON_MAP.put("place", "\ue55f");
        ICON_MAP.put("location_searching", "\ue1b7");
        ICON_MAP.put("location_disabled", "\ue1b6");
        ICON_MAP.put("compass_calibration", "\ue57a");
        ICON_MAP.put("explore", "\ue87a");
        ICON_MAP.put("explore_off", "\ue878");
        ICON_MAP.put("near_me", "\ue569");
        ICON_MAP.put("where_to_vote", "\ue177");
        ICON_MAP.put("pin_drop", "\ue55e");
        ICON_MAP.put("add_location", "\ue567");
        ICON_MAP.put("edit_location", "\ue568");
        ICON_MAP.put("location_city", "\ue7f1");
        
        // Símbolos de navegación del OS
        ICON_MAP.put("home", "\ue88a");
        ICON_MAP.put("menu", "\ue5d2");
        ICON_MAP.put("menu_open", "\ue9bd");
        ICON_MAP.put("dashboard", "\ue871");
        ICON_MAP.put("grid_view", "\ue9b0");
        ICON_MAP.put("view_list", "\ue8ef");
        ICON_MAP.put("view_module", "\ue8f0");
        ICON_MAP.put("view_quilt", "\ue8f1");
        ICON_MAP.put("view_comfy", "\ue42a");
        ICON_MAP.put("view_compact", "\ue42b");
        ICON_MAP.put("apps", "\ue5c3");
        ICON_MAP.put("more_vert", "\ue5d4");
        ICON_MAP.put("more_horiz", "\ue5d3");
        ICON_MAP.put("expand_more", "\ue5cf");
        ICON_MAP.put("expand_less", "\ue5ce");
        ICON_MAP.put("chevron_left", "\ue5cb");
        ICON_MAP.put("chevron_right", "\ue5cc");
        ICON_MAP.put("chevron_up", "\ue5c7");
        ICON_MAP.put("chevron_down", "\ue5c5");
        ICON_MAP.put("arrow_back", "\ue5c4");
        ICON_MAP.put("arrow_forward", "\ue5c8");
        ICON_MAP.put("arrow_upward", "\ue5d8");
        ICON_MAP.put("arrow_downward", "\ue5db");
        ICON_MAP.put("arrow_drop_down", "\ue5c5");
        ICON_MAP.put("arrow_drop_up", "\ue5c7");
        ICON_MAP.put("arrow_back_ios", "\ue5e0");
        ICON_MAP.put("arrow_forward_ios", "\ue5e1");
        ICON_MAP.put("keyboard_arrow_left", "\ue314");
        ICON_MAP.put("keyboard_arrow_right", "\ue315");
        ICON_MAP.put("keyboard_arrow_up", "\ue316");
        ICON_MAP.put("keyboard_arrow_down", "\ue317");
        ICON_MAP.put("keyboard_backspace", "\ue317");
        ICON_MAP.put("keyboard_return", "\ue31b");
        ICON_MAP.put("keyboard_tab", "\ue31c");
        ICON_MAP.put("subdirectory_arrow_left", "\ue5d9");
        ICON_MAP.put("subdirectory_arrow_right", "\ue5da");
        
        // Apps y servicios comunes
        ICON_MAP.put("chrome_reader_mode", "\ue86d");
        ICON_MAP.put("book", "\ue865");
        ICON_MAP.put("bookmark", "\ue866");
        ICON_MAP.put("bookmark_border", "\ue867");
        ICON_MAP.put("library_books", "\ue02f");
        ICON_MAP.put("article", "\uef42");
        ICON_MAP.put("newspaper", "\uea81");
        ICON_MAP.put("rss_feed", "\ue0e5");
        ICON_MAP.put("web", "\ue051");
        ICON_MAP.put("language", "\ue894");
        ICON_MAP.put("translate", "\ue8e2");
        ICON_MAP.put("public", "\ue80b");
        ICON_MAP.put("globe", "\ue80b");
        
        // Comunicación y mensajería
        ICON_MAP.put("mail", "\ue158");
        ICON_MAP.put("email", "\ue0be");
        ICON_MAP.put("inbox", "\ue156");
        ICON_MAP.put("send", "\ue163");
        ICON_MAP.put("reply", "\ue15e");
        ICON_MAP.put("reply_all", "\ue15f");
        ICON_MAP.put("forward", "\ue150");
        ICON_MAP.put("attach_file", "\ue226");
        ICON_MAP.put("attach_email", "\uea5e");
        ICON_MAP.put("link", "\ue157");
        ICON_MAP.put("call", "\ue0b0");
        ICON_MAP.put("call_made", "\ue0b2");
        ICON_MAP.put("call_received", "\ue0b5");
        ICON_MAP.put("call_missed", "\ue0b4");
        ICON_MAP.put("voicemail", "\ue0d9");
        ICON_MAP.put("contacts", "\ue0ba");
        ICON_MAP.put("contact_phone", "\ue0cf");
        ICON_MAP.put("contact_mail", "\ue0e0");
        
        // Redes sociales y plataformas
        ICON_MAP.put("share", "\ue80d");
        ICON_MAP.put("share_location", "\ue571");
        ICON_MAP.put("group", "\ue7ef");
        ICON_MAP.put("groups", "\ue233");
        ICON_MAP.put("person_add", "\ue7fe");
        ICON_MAP.put("person_remove", "\ue7ff");
        ICON_MAP.put("people", "\ue7fb");
        ICON_MAP.put("people_outline", "\ue7fc");
        ICON_MAP.put("person", "\ue7fd");
        ICON_MAP.put("account_circle", "\ue853");
        ICON_MAP.put("account_box", "\ue851");
        ICON_MAP.put("face", "\ue87c");
        ICON_MAP.put("tag_faces", "\ue420");
        
        // Herramientas y utilidades del sistema
        ICON_MAP.put("settings", "\ue8b8");
        ICON_MAP.put("settings_applications", "\ue8b8");
        ICON_MAP.put("tune", "\ue429");
        ICON_MAP.put("build", "\ue869");
        ICON_MAP.put("construction", "\ue869");
        ICON_MAP.put("handyman", "\ue10b");
        ICON_MAP.put("engineering", "\uea3d");
        ICON_MAP.put("science", "\uea4b");
        ICON_MAP.put("biotech", "\uea3a");
        ICON_MAP.put("memory", "\ue322");
        ICON_MAP.put("storage", "\ue1db");
        ICON_MAP.put("sd_card", "\ue623");
        ICON_MAP.put("usb", "\ue1e0");
        ICON_MAP.put("sim_card", "\ue32b");
        ICON_MAP.put("battery_full", "\ue1a3");
        ICON_MAP.put("battery_charging_full", "\ue1a3");
        ICON_MAP.put("battery_std", "\ue1a5");
        ICON_MAP.put("battery_alert", "\ue19c");
        ICON_MAP.put("power", "\ue8c6");
        ICON_MAP.put("power_settings_new", "\ue8ac");
        ICON_MAP.put("restart_alt", "\ue863");
        ICON_MAP.put("refresh", "\ue5d5");
        ICON_MAP.put("sync", "\ue627");
        ICON_MAP.put("autorenew", "\ue863");
        
        // Clima y ambiente (ampliado)
        ICON_MAP.put("wb_sunny", "\ue430");
        ICON_MAP.put("wb_cloudy", "\ue42d");
        ICON_MAP.put("wb_twilight", "\ue1c6");
        ICON_MAP.put("brightness_high", "\ue1a2");
        ICON_MAP.put("brightness_low", "\ue1a1");
        ICON_MAP.put("brightness_auto", "\ue1ab");
        ICON_MAP.put("dark_mode", "\ue51c");
        ICON_MAP.put("light_mode", "\ue518");
        ICON_MAP.put("thermostat", "\ue1b3");
        ICON_MAP.put("ac_unit", "\ueb3b");
        ICON_MAP.put("air", "\uefd8");
        ICON_MAP.put("airwave", "\ue29c");
        ICON_MAP.put("mode_fan", "\uf168");
        ICON_MAP.put("mode_fan_off", "\uec17");
        ICON_MAP.put("air_purifier", "\uefd8");
        ICON_MAP.put("air_purifier_gen", "\uefd8");
        
        // Vehículos y mantenimiento
        ICON_MAP.put("car_crash", "\uebf2");
        ICON_MAP.put("car_rental", "\ue55a");
        ICON_MAP.put("car_repair", "\ue55a");
        ICON_MAP.put("local_gas_station", "\ue567");
        ICON_MAP.put("ev_station", "\ue56d");
        ICON_MAP.put("charging_station", "\ue19d");
        ICON_MAP.put("tire_repair", "\uebc8");
        ICON_MAP.put("garage", "\ueb02");
        ICON_MAP.put("car_wash", "\ue16d");
        ICON_MAP.put("local_car_wash", "\ue16d");
        ICON_MAP.put("oil_barrel", "\uec15");
        ICON_MAP.put("local_atm", "\ue53e");
        ICON_MAP.put("atm", "\ue53e");
        ICON_MAP.put("speed", "\ue9e4");
        ICON_MAP.put("speedometer", "\ue9e4");
        ICON_MAP.put("traffic", "\ue565");
        ICON_MAP.put("traffic_jam", "\ue565");
        ICON_MAP.put("route", "\ueacd");
        ICON_MAP.put("alt_route", "\ue184");
        ICON_MAP.put("turn_left", "\ueba6");
        ICON_MAP.put("turn_right", "\ueba7");
        ICON_MAP.put("straight", "\ueb95");
        ICON_MAP.put("u_turn_left", "\ueba1");
        ICON_MAP.put("u_turn_right", "\ueba2");
        ICON_MAP.put("merge", "\ueb98");
        ICON_MAP.put("fork_left", "\ueba0");
        ICON_MAP.put("fork_right", "\ueba3");
        ICON_MAP.put("roundabout_right", "\ueb96");
        ICON_MAP.put("roundabout_left", "\ueb97");
        
        // Más iconos comunes
        ICON_MAP.put("flag", "\ue153");
        ICON_MAP.put("tag", "\ue9ef");
        ICON_MAP.put("label", "\ue892");
        ICON_MAP.put("print", "\ue8ad");
        ICON_MAP.put("download", "\ue2c4");
        ICON_MAP.put("upload", "\ue2c6");
        ICON_MAP.put("backup", "\ue2c0");
        ICON_MAP.put("restore", "\ue8b3");
        ICON_MAP.put("history", "\ue889");
        ICON_MAP.put("update", "\ue923");
        ICON_MAP.put("loop", "\ue028");
        
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
    
    public static java.util.Set<String> getAllIconNames() {
        return ICON_MAP.keySet();
    }
}

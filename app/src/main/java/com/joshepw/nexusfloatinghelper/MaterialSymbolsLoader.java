package com.joshepw.nexusfloatinghelper;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaterialSymbolsLoader {
    private static final String TAG = "MaterialSymbolsLoader";
    private static final String CODEPOINTS_FILE = "codepoints.json";
    private static Map<String, String> loadedIconMap = null;


    public static Map<String, String> loadFromJson(Context context) {
        if (loadedIconMap != null) {
            return loadedIconMap;
        }

        loadedIconMap = new HashMap<>();

        try {
            InputStream inputStream = context.getAssets().open(CODEPOINTS_FILE);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            );

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
            inputStream.close();


            JsonObject jsonObject = JsonParser.parseString(jsonContent.toString()).getAsJsonObject();


            Set<String> keys = jsonObject.keySet();
            for (String iconName : keys) {
                String hexCode = jsonObject.get(iconName).getAsString();
                try {


                    int codePoint = Integer.parseInt(hexCode, 16);

                    char[] chars = Character.toChars(codePoint);
                    String unicode = new String(chars);
                    loadedIconMap.put(iconName, unicode);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Error al parsear código hex para " + iconName + ": " + hexCode);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Código Unicode inválido para " + iconName + ": " + hexCode);
                }
            }

            Log.d(TAG, "Cargados " + loadedIconMap.size() + " iconos desde codepoints.json");
            return loadedIconMap;

        } catch (Exception e) {
            Log.w(TAG, "No se pudo cargar codepoints.json: " + e.getMessage());
            Log.w(TAG, "Usando mapeo manual como fallback");

            loadedIconMap = null;
            return null;
        }
    }


    public static Map<String, String> getIconMap(Context context) {
        Map<String, String> jsonMap = loadFromJson(context);
        if (jsonMap != null && !jsonMap.isEmpty()) {
            return jsonMap;
        }


        return MaterialSymbolsMapper.getManualIconMap();
    }


    public static boolean hasCodepointsFile(Context context) {
        try {
            InputStream inputStream = context.getAssets().open(CODEPOINTS_FILE);
            inputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


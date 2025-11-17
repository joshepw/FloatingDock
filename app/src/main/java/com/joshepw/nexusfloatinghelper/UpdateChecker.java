package com.joshepw.nexusfloatinghelper;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String TAG = "UpdateChecker";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/joshepw/FloatingDock/releases/latest";
    private static final String GITHUB_REPO_URL = "https://github.com/joshepw/FloatingDock";
    
    public static class UpdateInfo {
        private String versionName;
        private int versionCode;
        private String downloadUrl;
        private String releaseNotes;
        private boolean hasUpdate;
        
        public UpdateInfo(String versionName, int versionCode, String downloadUrl, String releaseNotes, boolean hasUpdate) {
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.downloadUrl = downloadUrl;
            this.releaseNotes = releaseNotes;
            this.hasUpdate = hasUpdate;
        }
        
        public String getVersionName() { return versionName; }
        public int getVersionCode() { return versionCode; }
        public String getDownloadUrl() { return downloadUrl; }
        public String getReleaseNotes() { return releaseNotes; }
        public boolean hasUpdate() { return hasUpdate; }
    }
    
    public interface UpdateCheckCallback {
        void onUpdateAvailable(UpdateInfo updateInfo);
        void onNoUpdateAvailable();
        void onError(String error);
    }
    
    public static void checkForUpdates(Context context, UpdateCheckCallback callback) {
        new Thread(() -> {
            try {
                // Obtener versión actual
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                int currentVersionCode = packageInfo.versionCode;
                String currentVersionName = packageInfo.versionName;
                
                // Consultar API de GitHub
                URL url = new URL(GITHUB_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    callback.onError("Error al conectar con GitHub: " + responseCode);
                    return;
                }
                
                // Leer respuesta
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                
                // Parsear JSON
                Gson gson = new Gson();
                JsonObject release = gson.fromJson(response.toString(), JsonObject.class);
                
                String latestVersionName = release.get("tag_name").getAsString().replace("v", "");
                String releaseNotes = release.has("body") ? release.get("body").getAsString() : "";
                
                // Buscar APK en assets
                String downloadUrl = null;
                if (release.has("assets")) {
                    JsonArray assets = release.getAsJsonArray("assets");
                    for (int i = 0; i < assets.size(); i++) {
                        JsonObject asset = assets.get(i).getAsJsonObject();
                        String assetName = asset.get("name").getAsString();
                        if (assetName.endsWith(".apk")) {
                            downloadUrl = asset.get("browser_download_url").getAsString();
                            break;
                        }
                    }
                }
                
                if (downloadUrl == null) {
                    callback.onError("No se encontró APK en el release");
                    return;
                }
                
                // Comparar versiones (asumiendo que versionName sigue formato semántico)
                boolean hasUpdate = isNewerVersion(currentVersionName, latestVersionName);
                
                // Crear UpdateInfo (versionCode se obtendría del APK, pero por simplicidad usamos versionName)
                UpdateInfo updateInfo = new UpdateInfo(latestVersionName, -1, downloadUrl, releaseNotes, hasUpdate);
                
                // Ejecutar callback en el hilo principal
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                if (hasUpdate) {
                    handler.post(() -> callback.onUpdateAvailable(updateInfo));
                } else {
                    handler.post(() -> callback.onNoUpdateAvailable());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error al verificar actualizaciones", e);
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        }).start();
    }
    
    private static boolean isNewerVersion(String currentVersion, String latestVersion) {
        try {
            // Comparar versiones semánticas (ej: "1.0.1" vs "1.0.2")
            String[] currentParts = currentVersion.split("\\.");
            String[] latestParts = latestVersion.split("\\.");
            
            int maxLength = Math.max(currentParts.length, latestParts.length);
            for (int i = 0; i < maxLength; i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                
                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }
            return false; // Son iguales
        } catch (Exception e) {
            Log.e(TAG, "Error al comparar versiones", e);
            // Si hay error, asumir que hay actualización para estar seguros
            return !currentVersion.equals(latestVersion);
        }
    }
    
    public static void downloadUpdate(Context context, String downloadUrl, String versionName) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager == null) {
                Log.e(TAG, "DownloadManager no disponible");
                return;
            }
            
            Uri uri = Uri.parse(downloadUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(context.getString(R.string.downloading_update_title, versionName));
            request.setDescription(context.getString(R.string.downloading_update_desc));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FloatingDock_" + versionName + ".apk");
            
            // Permitir descarga sobre datos móviles
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);
            
            long downloadId = downloadManager.enqueue(request);
            Log.d(TAG, "Descarga iniciada con ID: " + downloadId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar descarga", e);
        }
    }
    
    public static void openGitHubReleases(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL + "/releases"));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir GitHub", e);
        }
    }
}


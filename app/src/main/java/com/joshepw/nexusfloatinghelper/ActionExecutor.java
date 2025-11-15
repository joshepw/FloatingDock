package com.joshepw.nexusfloatinghelper;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.content.ContextCompat;

public class ActionExecutor {
    private static final String TAG = "ActionExecutor";
    
    public static boolean executeAction(Context context, String actionId) {
        if (actionId == null || actionId.isEmpty()) {
            Log.e(TAG, "ActionId es null o vacío");
            return false;
        }
        
        try {
            switch (actionId) {
                // Navegación
                case "home":
                    return executeHome(context);
                case "back":
                    return executeBack(context);
                case "recent":
                    return executeRecent(context);
                
                // Volumen
                case "volume_up":
                    return executeVolumeUp(context);
                case "volume_down":
                    return executeVolumeDown(context);
                case "volume_mute":
                    return executeVolumeMute(context);
                
                // Medios
                case "media_play":
                    return executeMediaPlay(context);
                case "media_pause":
                    return executeMediaPause(context);
                case "media_next":
                    return executeMediaNext(context);
                case "media_previous":
                    return executeMediaPrevious(context);
                
                // Llamadas
                case "answer_call":
                    return executeAnswerCall(context);
                case "end_call":
                    return executeEndCall(context);
                
                // Asistente de voz
                case "voice_assistant":
                    return executeVoiceAssistant(context);
                
                default:
                    Log.w(TAG, "Acción no reconocida: " + actionId);
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar acción: " + actionId, e);
            return false;
        }
    }
    
    // Navegación
    private static boolean executeHome(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Home", e);
            return false;
        }
    }
    
    private static boolean executeBack(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Back", e);
            return false;
        }
    }
    
    private static boolean executeRecent(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_APP_SWITCH);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Recent", e);
            return false;
        }
    }
    
    // Volumen
    private static boolean executeVolumeUp(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Volume Up", e);
            return false;
        }
    }
    
    private static boolean executeVolumeDown(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Volume Down", e);
            return false;
        }
    }
    
    private static boolean executeVolumeMute(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_TOGGLE_MUTE,
                    AudioManager.FLAG_SHOW_UI
                );
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Volume Mute", e);
            return false;
        }
    }
    
    // Medios
    private static boolean executeMediaPlay(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
            context.sendBroadcast(intent);
            
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Play", e);
            return false;
        }
    }
    
    private static boolean executeMediaPause(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
            context.sendBroadcast(intent);
            
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Pause", e);
            return false;
        }
    }
    
    private static boolean executeMediaNext(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            context.sendBroadcast(intent);
            
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Next", e);
            return false;
        }
    }
    
    private static boolean executeMediaPrevious(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            context.sendBroadcast(intent);
            
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Previous", e);
            return false;
        }
    }
    
    // Llamadas
    private static boolean executeAnswerCall(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null && telecomManager.isInCall()) {
                    telecomManager.acceptRingingCall();
                    return true;
                }
            }
            // Fallback: Intent
            Intent answerIntent = new Intent(Intent.ACTION_ANSWER);
            answerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(answerIntent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Answer Call", e);
            return false;
        }
    }
    
    private static boolean executeEndCall(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null && telecomManager.isInCall()) {
                    telecomManager.endCall();
                    return true;
                }
            }
            // Fallback: Simular tecla END_CALL
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENDCALL);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar End Call", e);
            return false;
        }
    }
    
    // Asistente de voz
    private static boolean executeVoiceAssistant(Context context) {
        try {
            // Intent para Google Assistant
            Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                // Intent alternativo
                Intent searchIntent = new Intent(Intent.ACTION_SEARCH);
                searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(searchIntent);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Voice Assistant", e);
            return false;
        }
    }
}


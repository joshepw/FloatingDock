package com.joshepw.nexusfloatinghelper;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.KeyEvent;

public class ActionExecutor {
    private static final String TAG = "ActionExecutor";

    public static boolean executeAction(Context context, String actionId) {
        if (actionId == null || actionId.isEmpty()) {
            Log.e(TAG, "ActionId es null o vacío");
            return false;
        }

        try {
            switch (actionId) {

                case "home":
                    return executeHome(context);
                case "back":
                    return executeBack(context);
                case "recent":
                    return executeRecent(context);


                case "volume_up":
                    return executeVolumeUp(context);
                case "volume_down":
                    return executeVolumeDown(context);
                case "volume_mute":
                    return executeVolumeMute(context);


                case "media_play":
                    return executeMediaPlay(context);
                case "media_pause":
                    return executeMediaPause(context);
                case "media_next":
                    return executeMediaNext(context);
                case "media_previous":
                    return executeMediaPrevious(context);


                case "answer_call":
                    return executeAnswerCall(context);
                case "end_call":
                    return executeEndCall(context);


                case "voice_assistant":
                    return executeVoiceAssistant(context);


                case "hide_dock":
                    return executeHideDock(context);

                default:
                    Log.w(TAG, "Acción no reconocida: " + actionId);
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar acción: " + actionId, e);
            return false;
        }
    }


    private static boolean executeHome(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Home con KeyEvent (permiso denegado), intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Home con Intent", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Home con KeyEvent, intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Home con Intent", e2);
                return false;
            }
        }
    }

    private static boolean executeBack(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Back (permiso denegado)", e);
            return false;
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
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Recent (permiso denegado)", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Recent", e);
            return false;
        }
    }


    private static boolean executeVolumeUp(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Volume Up con KeyEvent (permiso denegado), intentando con AudioManager", e);
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
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Volume Up con AudioManager", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Volume Up con KeyEvent, intentando con AudioManager", e);
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
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Volume Up con AudioManager", e2);
                return false;
            }
        }
    }

    private static boolean executeVolumeDown(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Volume Down con KeyEvent (permiso denegado), intentando con AudioManager", e);
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
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Volume Down con AudioManager", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Volume Down con KeyEvent, intentando con AudioManager", e);
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
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Volume Down con AudioManager", e2);
                return false;
            }
        }
    }

    private static boolean executeVolumeMute(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_MUTE);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Volume Mute con KeyEvent (permiso denegado), intentando con AudioManager", e);
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
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Volume Mute con AudioManager", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Volume Mute con KeyEvent, intentando con AudioManager", e);
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
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Volume Mute con AudioManager", e2);
                return false;
            }
        }
    }


    private static boolean executeMediaPlay(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PLAY);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Media Play con KeyEvent (permiso denegado), intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Play con Intent", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Play con KeyEvent, intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Play con Intent", e2);
                return false;
            }
        }
    }

    private static boolean executeMediaPause(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PAUSE);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Media Pause con KeyEvent (permiso denegado), intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Pause con Intent", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Pause con KeyEvent, intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Pause con Intent", e2);
                return false;
            }
        }
    }

    private static boolean executeMediaNext(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Media Next con KeyEvent (permiso denegado), intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Next con Intent", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Next con KeyEvent, intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Next con Intent", e2);
                return false;
            }
        }
    }

    private static boolean executeMediaPrevious(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Media Previous con KeyEvent (permiso denegado), intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Previous con Intent", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Media Previous con KeyEvent, intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                context.sendBroadcast(intent);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                context.sendBroadcast(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Media Previous con Intent", e2);
                return false;
            }
        }
    }


    private static boolean executeAnswerCall(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_CALL);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Answer Call con KeyEvent (permiso denegado), intentando con TelecomManager", e);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    if (telecomManager != null && telecomManager.isInCall()) {
                        telecomManager.acceptRingingCall();
                        return true;
                    }
                }
                Intent answerIntent = new Intent(Intent.ACTION_ANSWER);
                answerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(answerIntent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Answer Call con métodos alternativos", e2);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Answer Call con KeyEvent, intentando con TelecomManager", e);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                    if (telecomManager != null && telecomManager.isInCall()) {
                        telecomManager.acceptRingingCall();
                        return true;
                    }
                }
                Intent answerIntent = new Intent(Intent.ACTION_ANSWER);
                answerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(answerIntent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Answer Call con métodos alternativos", e2);
                return false;
            }
        }
    }

    private static boolean executeEndCall(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager != null && telecomManager.isInCall()) {
                    telecomManager.endCall();
                    return true;
                }
            }

            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENDCALL);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar End Call con KeyEvent (permiso denegado)", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar End Call", e);
            return false;
        }
    }


    private static boolean executeVoiceAssistant(Context context) {
        try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOICE_ASSIST);
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al ejecutar Voice Assistant con KeyEvent (permiso denegado), intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Voice Assistant con ACTION_VOICE_COMMAND, intentando con ACTION_SEARCH", e2);
                try {
                    Intent searchIntent = new Intent(Intent.ACTION_SEARCH);
                    searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(searchIntent);
                    return true;
                } catch (Exception e3) {
                    Log.e(TAG, "Error al ejecutar Voice Assistant con métodos alternativos", e3);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Voice Assistant con KeyEvent, intentando con Intent", e);
            try {
                Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error al ejecutar Voice Assistant con ACTION_VOICE_COMMAND, intentando con ACTION_SEARCH", e2);
                try {
                    Intent searchIntent = new Intent(Intent.ACTION_SEARCH);
                    searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(searchIntent);
                    return true;
                } catch (Exception e3) {
                    Log.e(TAG, "Error al ejecutar Voice Assistant con métodos alternativos", e3);
                    return false;
                }
            }
        }
    }


    private static boolean executeHideDock(Context context) {
        try {

            Intent intent = new Intent("HIDE_DOCK_ACTION");
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al ejecutar Hide Dock", e);
            return false;
        }
    }
}


package com.fralav.rtminer.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.fralav.rtminer.R;

/**
 * La classe contiene i metodi utili all'implementazione e all'utilizzo della Dark Mode.
 */
public final class ThemeUtils {

    private ThemeUtils() {}

    /**
     * Il metodo restituisce il tema corrente a scelta tra {@code DarkMode} e {@code DayMode}.
     * @return ID del tema corrente.
     */
    public static int defaultTheme() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            return R.style.DarkMode;
        } else {
            return R.style.DayMode;
        }
    }

    /**
     * Restituisce {@code True} se la Dark Mode è attiva, {@code False} altrimenti.
     * @return {@code True} se la Dark Mode è attiva, {@code False} altrimenti.
     */
    public static boolean isDarkMode() {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    }

    /**
     * Quando viene avviato il metodo, il software ascolta i cambiamenti dello switch: se è
     * impostato su {@code ON} viene attivata la dark mode, viene attivata la day mode altrimenti.
     * @param context Activity dove risiede lo switch.
     * @param darkSwitch Switch che attiva o disattiva la dark mode.
     */
    public static void listen(final Context context, SwitchCompat darkSwitch) {
        darkSwitch.setOnCheckedChangeListener((compoundButton, bool) -> {
            if (bool) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            ((AppCompatActivity)context).recreate();
        });
    }

    /**
     * Il metodo serve a costruire un {@link AlertDialog} con il tema corrente.
     * @param context Activity dove risiete l'AlertDialog
     * @return Il builder dell'{@link AlertDialog}.
     */
    public static AlertDialog.Builder getBuilder(Context context) {
        AlertDialog.Builder builder;
        if (ThemeUtils.isDarkMode()) {
            builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder;
    }
}
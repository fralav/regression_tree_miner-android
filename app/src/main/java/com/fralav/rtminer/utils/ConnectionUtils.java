package com.fralav.rtminer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.fralav.rtminer.MainActivity;
import com.fralav.rtminer.R;

/**
 * La classe implementa i metodi utili a verificare lo stato dellaconnessione dello smartphone a Internet.
 */
public final class ConnectionUtils {

    private ConnectionUtils() {}

    /**
     * Verifica se lo smartphone è connesso o meno a internet e ne restituisce l'esito.
     * @param context Activity dove verificare la connessione a internet.
     * @return {@code True} se la connessione è assente, {@code false} altrimenti.
     */
    public static boolean absentConnection(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activateNetwork = connectivityManager.getActiveNetworkInfo();
            return !(activateNetwork != null && activateNetwork.isConnectedOrConnecting());
        } else {
            return true;
        }
    }

    /**
     * Il metodo costruisce nel contesto corrente un {@link AlertDialog} utile a comunicare all'utente
     * la perdita della connessione con il server.
     * @param context Activity o contesto dove costruire l'{@link AlertDialog}.
     */
    public static void lostConnection(Context context) {
        AlertDialog.Builder builder = ThemeUtils.getBuilder(context);
        builder.setTitle(R.string.server_unreachable_title);
        builder.setMessage(R.string.server_lost_connection);
        builder.setNegativeButton(R.string.negative_button, (dialogInterface, i) -> MainActivity.openMainActivity(context));
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Il metodo costruisce nel contesto corrente un {@link AlertDialog} utile a comunicare all'utente
     * che il server non risulta essere raggiungibile.
     * @param context Activity o contesto dove costruire l'{@link AlertDialog}.
     */
    public static void serverUnreachable(Context context) {
        AlertDialog.Builder builder = ThemeUtils.getBuilder(context);
        builder.setTitle(R.string.server_unreachable_title);
        builder.setMessage(R.string.server_unreachable_paragrah);
        builder.setNegativeButton(R.string.negative_button, (dialogInterface, i) -> MainActivity.openMainActivity(context));
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Il metodo costruisce nel contesto corrente un {@link AlertDialog} utile a comunicare all'utente
     * un determinato tipo di errore.
     * @param context Activity o contesto dove costruire l'{@link AlertDialog}.
     * @param message Messaggio di errore.
     */
    public static void errorMessage(Context context, int message) {
        AlertDialog.Builder builder = ThemeUtils.getBuilder(context);
        builder.setTitle(R.string.error_title);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.negative_button, (dialogInterface, i) -> MainActivity.openMainActivity(context));
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}

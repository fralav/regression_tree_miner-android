package com.fralav.rtminer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;

import com.fralav.rtminer.client.Client;
import com.fralav.rtminer.utils.ThemeUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * La classe contiene un solo metodo pubblico utile a costruire un {@link AlertDialog} contenente
 * le impostazioni dell'app (di connessione e ti tema).Viene istanziata quando l'utente clicca sul
 * tasto "Impostazioni" nel menu tree-dots. All'interno dell'{@link AlertDialog} vi sono due
 * {@link EditText} che dovranno contenere rispettivamente l'indirizzo IP del server e la porta.
 * Inoltre è presente uno switch che seleziona o deseleziona la modalità notte.
 */
public class SettingsDialog extends AppCompatDialogFragment {

    /**
     * Deve contenere l'indirizzo IP del server.
     */
    private EditText ip;

    /**
     * Deve contenere la porta alla quale il server è in ascolto.
     */
    private EditText port;

    /**
     * Variabile che diventa {@code True} se l'utente ha inserito in maniera corretta l'IP e la porta
     * del server.
     */
    public static boolean ENABLED = false;

    /**
     * Il metodo costruisce un {@link AlertDialog} contenente le impostazioni dell'app. In primo luogo
     * costruisce due {@link EditText} che devono contenere IP e PORTA del server. Inoltre è presente uno
     * switch che servirà a selezionare o deselezionare la night mode. Se l'utente inserisce nei campi
     * IP e porta valori validi, allora gli {@link EditText} vengono popolati, altrimenti il software
     * invita l'utente, tramite notifiche {@link Toast}, ad inserire validi valori di Ip e porta.
     * @param savedInstanceState
     * @return Dialog creato
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = ThemeUtils.getBuilder(getContext());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setTitle(R.string.settings_dialog_title);
        builder.setPositiveButton(R.string.positive_button, (dialogInterface, i) -> {
            if (!checkIp() && !checkPort()) {
                Toast.makeText(getContext(), R.string.settings_ipAndPort_wrong, Toast.LENGTH_SHORT).show();
                ENABLED = false;
            } else if (checkPort() && !checkIp()) {
                Toast.makeText(getContext(), R.string.settings_ip_wrong, Toast.LENGTH_SHORT).show();
                ENABLED = false;
            } else if (!checkPort() && checkIp()) {
                Toast.makeText(getContext(), R.string.settings_port_wrong, Toast.LENGTH_SHORT).show();
                ENABLED = false;
            } else {
                Client.getInstance().setIp(ip.getText().toString());
                Client.getInstance().setPort(Integer.parseInt(port.getText().toString()));
                ENABLED = true;
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.negative_button, (dialogInterface, i) -> dialogInterface.dismiss());

        ip = view.findViewById(R.id.settings_insertIP);
        port = view.findViewById(R.id.settings_insertPort);

        SwitchCompat darkSwitch = view.findViewById(R.id.settings_switch);
        darkSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        ThemeUtils.listen(getContext(), darkSwitch);

        String instanceIp = Client.getInstance().getIp();
        int instancePort = Client.getInstance().getPort();

        if (instanceIp != null && !instanceIp.equals("")) {
            ip.setText(instanceIp);
        }

        if (instancePort != 0) {
            port.setText(String.format(Locale.getDefault(), "%d", instancePort));
        }

        return builder.create();
    }

    /**
     * Metodo di supporto che serve a calcolare la validità della porta del server
     * @return {@code True} se la porta è in formato valido, {@code False} altrimenti.
     */
    private boolean checkPort() {
       String sPort = port.getText().toString();
        if (sPort.equals("")) {
            return false;
        } else {
            int iPort = Integer.parseInt(sPort);
            return 1024 <= iPort && iPort <= 65535;
        }
    }

    /**
     * Metodo di supporto che serve a calcolare la validità dell'IP del server.
     * @return {@code True} se l'indirizzo IP è in formato valido, {@code False} altrimenti.
     */
    private boolean checkIp() {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.getText().toString().matches(PATTERN);
    }
}
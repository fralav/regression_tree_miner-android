package com.fralav.rtminer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.fralav.rtminer.utils.ThemeUtils;

import java.util.Objects;

/**
 * La classe contiene un solo metodo che viene richiamato quando l'utente chiede di visualizzare la
 * schermata contente le informazioni sull'autore del progetto, il quale ha il compito di creare
 * un {@link AlertDialog} contenente il layout di riferiemento alla schermata <i>Dettagli</i>.
 */
public class DetailsDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = ThemeUtils.getBuilder(getContext());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_details, null);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.negative_button, (dialogInterface, i) -> dialogInterface.dismiss());
        return builder.create();
    }
}

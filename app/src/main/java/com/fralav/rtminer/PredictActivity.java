package com.fralav.rtminer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fralav.rtminer.client.Client;
import com.fralav.rtminer.utils.ConnectionUtils;
import com.fralav.rtminer.utils.ThemeUtils;

/**
 * La classe rappresenta l'activity {@code predict}, ovvero il contesto che viene mostrato quando
 * l'utente sceglie di avviare la fase di predizione
 */
public class PredictActivity extends AppCompatActivity {

    /**
     * Viene caricato il layout corrispondete alla classe {@code predict}, impostando il tema scelto
     * dall'utente. Mostra il tasto indietro sulla action bar, eventualmente clickabile per poter
     * tornare alla {@link TablesActivity}, inizializza la {@link TextView} che deve contenere le
     * istruzioni di predizione, lo {@link Spinner} che deve contenere le possibili scelte e il
     * {@link Button} che permette all'utente di effettuare la scelta. Inoltre viene avviata la fase
     * di predizione, dove avviene un dialogo tra il server e il client.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtils.defaultTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView predictText = findViewById(R.id.predict_paragraph);
        Spinner predictSpinner = findViewById(R.id.predict_spinner);
        Button predictButtonOk = findViewById(R.id.predict_button_ok);

        Client.getInstance().startPredictionMode();
        predictTree(predictText, predictSpinner);

        predictButtonOk.setOnClickListener(view -> {
            Client.getInstance().writeObjectToSocket(Integer.parseInt(predictSpinner.getSelectedItem().toString()));
            predictTree(predictText, predictSpinner);
        });
    }

    /**
     * Il metodo si attiva quando viene premuto il tasto indietro. Disconnette il socket se è connesso
     * e lo riconnette. Inoltre, in caso di assenza di connessione, mostra un {@link AlertDialog} che
     * avvisa l'utente della caduta della connessione. Se c'è connessione, invece ritorna alla schermata
     * precedente.
     */
    @Override
    public void onBackPressed() {
        if (Client.getInstance().isConnected()) {
            Client.getInstance().disconnect();
        }
        if (!Client.getInstance().isConnected()) {
            Client.getInstance().connect();
        }
        if (!ConnectionUtils.absentConnection(this)) {
            Bundle bundle = new Bundle();
            bundle.putInt(TablesActivity.TYPE, TablesActivity.ID);
            TablesActivity.openTablesActivity(PredictActivity.this, bundle);
        } else {
            ConnectionUtils.lostConnection(this);
        }
        finish();
    }

    /**
     * Il metodo si attiva quando si preme il tasto indietro situato nell'action bar, e ha lo stesso
     * compito del tasto back fisico, e quindi richiama il metodo {@code onBackPressed()}.
     * @param item Elemento dell'action bar
     * @return Esito
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    /**
     * Fase di predizione vera e propria. Inizializza le variabili di supporto alla computazione della
     * predizione. Riceve una stringa dal server: se è uguale a {@code QUERY}, allora bisogna selezionare
     * un figlio del nodo corrente tramite lo spinner, che viene aggiornato a ogni iterazione, premendo
     * il tasto {@code OK} a fine scelta. Se invece la stringa ricevuta inzialmente è uguale a
     * {@code OK}, vuol dire che il server ha raggiunto un nodo foglia e quindi viene mostrato un
     * {@link AlertDialog} che mostra il valore di predizione. Il metoodo si ripete finché non viene raggiunto
     * un nodo foglia.
     * @param predictText TextView da aggiornare
     * @param predictSpinner Spinner da aggiornare
     */
    private void predictTree(TextView predictText, Spinner predictSpinner) {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(PredictActivity.this, R.layout.spinner_color_layout);
        String answer = Client.getInstance().readObjectFromSocket().toString();
        if (answer.equals("QUERY")) {
            answer = Client.getInstance().readObjectFromSocket().toString();
            predictText.setText(answer);
            int numberOfChildren = Integer.parseInt(Client.getInstance().readObjectFromSocket().toString());
            adapter.clear();
            for (int i = 0; i< numberOfChildren; i++) {
                adapter.add(i);
            }
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
            predictSpinner.setAdapter(adapter);
        } else if (answer.equals("OK")) {
            answer = Client.getInstance().readObjectFromSocket().toString();
            android.app.AlertDialog.Builder builder = ThemeUtils.getBuilder(PredictActivity.this);
            builder.setTitle(R.string.predict_dialog_title);
            builder.setMessage(answer);
            builder.setNegativeButton(R.string.negative_button, (dialogInterface, i) -> onBackPressed());
            builder.setPositiveButton(R.string.repeat_button, (dialogInterface, i) -> PredictActivity.openPredictActivity(PredictActivity.this));
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    /**
     * Il metodo è di supporto ad altre classi e serve per poter aprire l'oggetto corrente, e quindi
     * mostrare la UI di questa classe.
     * @param context Contesto da dove richiamare il metodo.
     */
    public static void openPredictActivity(Context context) {
        context.startActivity(new Intent(context, PredictActivity.class));
    }
}

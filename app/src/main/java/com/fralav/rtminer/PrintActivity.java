package com.fralav.rtminer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fralav.rtminer.client.Client;
import com.fralav.rtminer.utils.ConnectionUtils;
import com.fralav.rtminer.utils.ThemeUtils;

/**
 * La classe rappresenta l'activity di stampa, ovvero il contesto che viene mostrato quando
 * l'utente sceglie di avviare la stampa dell'albero.
 */
public class PrintActivity extends AppCompatActivity {

    /**
     * Viene creata la UI con il layout corrispondente e con il tema precedentemente scelto dall'utente.
     * Inizializza il {@link TextView} che deve contenere la stampa dell'albero e lo popola con la
     * string richiesta dal server.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtils.defaultTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView paragraph = findViewById(R.id.results_paragraph);
        String text = Client.getInstance().printTree();
        paragraph.setText(text);
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
            TablesActivity.openTablesActivity(PrintActivity.this, bundle);
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
     * Il metodo è di supporto ad altre classi e serve per poter aprire l'oggetto corrente, e quindi
     * mostrare la UI di questa classe.
     * @param context Contesto da dove richiamare il metodo.
     */
    public static void openPrintActivity(Context context) {
        context.startActivity(new Intent(context, PrintActivity.class));
    }
}

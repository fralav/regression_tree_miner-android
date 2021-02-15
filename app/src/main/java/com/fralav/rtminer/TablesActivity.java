package com.fralav.rtminer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fralav.rtminer.client.Client;
import com.fralav.rtminer.utils.ConnectionUtils;
import com.fralav.rtminer.utils.ThemeUtils;

import java.util.LinkedList;

/**
 * La classe rappresenta l'activity {@code tables}, ovvero il contesto che viene mostrato quando
 * l'utente scegliere di voler caricare un training set dal database oppure un albero da file
 * dall'interno della {@link MainActivity}.
 */
public class TablesActivity extends AppCompatActivity {

    public static int ID = 0;
    private Spinner spinner;
    private Button buttonTablePrint;
    private Button buttonTablePredict;
    private ProgressBar progressBar;

    public static final String TYPE = "type";
    public static final int FROM_DB = 1;
    public static final int FROM_FILE = 2;

    /**
     * Crea la UI relativa alla scelta della tabella, se da file o da database, secondo la scelta
     * effettuata dall'utente nella schermata precedente. Imposta il tema scelto dall'utente tra Day
     * Mode o Dark Mode, inizializza gli elementi {@link Button} e {@link Spinner} presenti nell'interfaccia.
     * Il metodo viene chiamato appena viene istanziata la classe e, nel caso in cui ci fossero problemi
     * di connessione, il software mostra un {@link AlertDialog} dove comunica all'utente il problema
     * di connessione riscontrato. Infine preleva dal database i nomi delle tabelle e li mostra
     * all'interno dello spinner, oppure, se l'utente ha scelto di voler prelevare un albero precedentemente
     * serializzato, mostra nello spinner i file relativi.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtils.defaultTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ID = bundle.getInt(TYPE);
        }

        TextView tableTitle = findViewById(R.id.table_title);
        TextView tableParagraph = findViewById(R.id.table_paragraph);

        if (ID == FROM_DB) {
            tableTitle.setText(R.string.table_title_db);
            tableParagraph.setText(R.string.table_paragraph_db);
        } else if (ID == FROM_FILE) {
            tableTitle.setText(R.string.table_title_file);
            tableParagraph.setText(R.string.table_paragraph_file);
        } else {
            finish();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        buttonTablePrint = findViewById(R.id.table_button_print);
        buttonTablePredict = findViewById(R.id.table_button_predict);
        spinner = findViewById(R.id.table_spinner);
        progressBar = findViewById(R.id.tables_progressBar);

        progressBar.setVisibility(View.INVISIBLE);

        setButtonTablePrint();
        setButtonTablePredict();

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, R.layout.spinner_color_layout);

        if (ConnectionUtils.absentConnection(this)) {
            Toast.makeText(this, R.string.connection_not_found, Toast.LENGTH_LONG).show();
            MainActivity.openMainActivity(this);
        }

        if (!Client.getInstance().isConnected()) {
            Client.getInstance().connect();
        }

        if (Client.getInstance().isConnected()) {
            if (ID == FROM_DB) {
                LinkedList<String> tables = Client.getInstance().getTablesFromDb();
                if (tables.contains(Client.NO_TABLES_FOUND)) {
                    ConnectionUtils.errorMessage(this, R.string.error_notables);
                } else {
                    adapter.addAll(tables);
                }
            }
            else if (ID == FROM_FILE) {
                LinkedList<String> files = Client.getInstance().getFilesFromArchive();
                if (files.contains(Client.NO_FILES_FOUND)) {
                    ConnectionUtils.errorMessage(this, R.string.error_nofiles);
                } else {
                    adapter.addAll(files);
                }
            }
        } else {
            ConnectionUtils.serverUnreachable(this);
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spinner.setAdapter(adapter);
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

    /**
     * Il metodo si attiva quando viene premuto il tasto relativo alla stampa dell'albero. Se la
     * connessione è assente, viene mostrato un messaggio a video. Verifica che non ci siano problemi
     * al server nel momento in cui esso preleva l'albero, e se non ci sono problemi, apre la {@link PrintActivity}.
     */
    private void setButtonTablePrint() {
        buttonTablePrint.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            if (ConnectionUtils.absentConnection(this)) {
                ConnectionUtils.lostConnection(TablesActivity.this);
            } else {
                String result;
                switch (ID) {
                    case FROM_DB:
                        result = Client.getInstance().learnTreeFromDb(spinner.getSelectedItem().toString());
                        switch (result) {
                            case Client.DATA_ERROR:
                                ConnectionUtils.errorMessage(TablesActivity.this, R.string.error_dataerror);
                                break;
                            case Client.TABLE_NOT_FOUND:
                                ConnectionUtils.errorMessage(TablesActivity.this, R.string.error_tablenotfound);
                                break;
                            case Client.OK:
                                PrintActivity.openPrintActivity(TablesActivity.this);
                                break;
                            default:
                                break;
                        }
                        break;

                    case FROM_FILE:
                        result = Client.getInstance().getTreeFromFile(spinner.getSelectedItem().toString());
                        if (result.equals(Client.FILE_NOT_FOUND)) {
                            ConnectionUtils.errorMessage(TablesActivity.this, R.string.error_filenotfound);
                        } else if (result.equals(Client.OK)) {
                            PrintActivity.openPrintActivity(TablesActivity.this);
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * Il metodo si attiva quando viene premuto il tasto relativo alla predizione dell'albero. Se la
     * connessione è assente, viene mostrato un messaggio a video. Verifica che non ci siano problemi
     * al server nel momento in cui esso preleva l'albero, e se non ci sono problemi, apre la {@link PredictActivity}.
     */
    private void setButtonTablePredict() {
        buttonTablePredict.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            if (ConnectionUtils.absentConnection(this)) {
                ConnectionUtils.lostConnection(TablesActivity.this);
            } else {
                String result;
                switch (ID) {
                    case FROM_DB:
                        result = Client.getInstance().learnTreeFromDb(spinner.getSelectedItem().toString());
                        switch (result) {
                            case Client.DATA_ERROR:
                                ConnectionUtils.errorMessage(TablesActivity.this, R.string.error_dataerror);
                                break;
                            case Client.TABLE_NOT_FOUND:
                                ConnectionUtils.errorMessage(TablesActivity.this, R.string.error_tablenotfound);
                                break;
                            case Client.OK:
                                PredictActivity.openPredictActivity(TablesActivity.this);
                                break;
                            default:
                                break;
                        }
                        break;

                    case FROM_FILE:
                        result = Client.getInstance().getTreeFromFile(spinner.getSelectedItem().toString());
                        if (result.equals(Client.FILE_NOT_FOUND)) {
                            ConnectionUtils.errorMessage(TablesActivity.this, R.string.error_filenotfound);
                        } else if (result.equals(Client.OK)) {
                            PredictActivity.openPredictActivity(TablesActivity.this);
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * Il metodo è di supporto ad altre classi e serve per poter aprire l'oggetto corrente, e quindi
     * mostrare la UI di questa classe.
     * @param context Contesto da dove richiamare il metodo.
     * @param params Il bundle contiene informazioni circa la scelta dell'utente, ovvero se la classe corrente
     *               deve mostrare i file serializzati o le tabelle del database.
     */
    public static void openTablesActivity(Context context, Bundle params) {
        Intent intent=new Intent(context, TablesActivity.class);
        intent.putExtras(params);
        context.startActivity(intent);
    }

    /**
     * Il metodo si attiva quando viene premuto il tasto indietro. Ritorna alla schermata precedente.
     */
    @Override
    public void onBackPressed() {
        MainActivity.openMainActivity(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}

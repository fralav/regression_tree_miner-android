package com.fralav.rtminer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fralav.rtminer.client.Client;
import com.fralav.rtminer.utils.ConnectionUtils;
import com.fralav.rtminer.utils.ThemeUtils;

/**
 * La classe rappresenta l'activity principale, ovvero la prima activity che l'utente riesce a vedere
 * appena lancia l'app.
 */
public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button buttonDb;
    private Button buttonFile;

    /**
     * Il metodo serve a creare un'istanza della classe MainActivity tramite UI Thread. Il metodo
     * setta il tema scelto dall'utente e seleziona il giusto layout per questa classe. Inizializza
     * gli oggetti {@link ProgressBar} e {@link Button}, rende invisibile la {@code progressBar} e
     * tramite altri metodi, resta in ascolto sui {@link Button}.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtils.defaultTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.main_progressBar);
        buttonDb = findViewById(R.id.main_button_db);
        buttonFile = findViewById(R.id.main_button_file);

        progressBar.setVisibility(View.INVISIBLE);

        setButtonDb();
        setButtonFile();
    }

    /**
     * Il metodo si attiva quando viene ripresa l'activity. Rende la {@code ProgressBar} invisibile
     * e disconnette il socket nel caso sia ancora connesso.
     */
    @Override
    protected void onPostResume() {
        progressBar.setVisibility(View.INVISIBLE);
        if (Client.getInstance().isConnected()) {
            Client.getInstance().disconnect();
        }
        super.onPostResume();
    }

    /**
     * Viene creato il menu {@code tree-dots}.
     * @param menu Oggetto {@link Menu} al quale fare riferimento.
     * @return Valore booleano che indica l'esito del metodo.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Si attiva quando viene premuto il tasto indietro, ha il compito di chiudere l'app.
     */
    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    /**
     * Ascolta quando l'utente clicca su uno degli item all'interno del menu e ne apre il Dialog
     * corrispondente.
     * @param item Elementi del menu.
     * @return Esito.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_author) {
            openAuthorDialog();
            return true;
        } else if (item.getItemId() == R.id.menu_details) {
            openDetailsDialog();
            return true;
        } else if (item.getItemId() == R.id.menu_settings) {
            openSettingsDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Instanzia un oggetto della classe {@link AuthorDialog} e quindi rende visibile il Dialog.
     */
    private void openAuthorDialog() {
        AuthorDialog authorDialog = new AuthorDialog();
        authorDialog.show(getSupportFragmentManager(), "AuthorDialog");
    }

    /**
     * Instanzia un oggetto della classe {@link DetailsDialog} e quindi rende visibile il Dialog.
     */
    private void openDetailsDialog() {
        DetailsDialog detailsDialog = new DetailsDialog();
        detailsDialog.show(getSupportFragmentManager(), "DetailsDialog");
    }

    /**
     * Instanzia un oggetto della classe {@link SettingsDialog} e quindi rende visibile il Dialog.
     */
    private void openSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog();
        settingsDialog.show(getSupportFragmentManager(), "SettingsDialog");
    }

    /**
     * Si attiva appena viene clickato un {@link Button} all'interno dell'Activity. Se la connessione
     * è assente, l'utente viene invitato ad attivare la connessione. Se la connessione è attiva ma
     * l'utente non ha inserito nessun IP e nessuna porta del server, l'utente viene invitato ad inserire
     * IP e porta prima di continuare. Se sono stati inseriti all'interno delle impostazioni IP e
     * porta validi, allora viene aperta una nuova {@link TablesActivity}.
     * @param button {@code Button} premuto a scelta tra "Carica da database" e "Carica da file".
     * @param type Tipo di scelta, può essere per prelevare la tabella dal database oppure l'albero
     *             da file. Serve per comunicare alla {@link TablesActivity} la scelta dell'utente.
     */
    private void setButtonInMainActivity(Button button, int type) {
        button.setOnClickListener(view -> {
            if (ConnectionUtils.absentConnection(this)) {
                Toast.makeText(this, R.string.connection_not_found, Toast.LENGTH_LONG).show();
            } else {
                if (SettingsDialog.ENABLED) {
                    progressBar.setVisibility(View.VISIBLE);
                    Bundle bundle = new Bundle();
                    bundle.putInt(TablesActivity.TYPE, type);
                    TablesActivity.openTablesActivity(MainActivity.this, bundle);
                } else {
                    Toast.makeText(this, R.string.settings_insertip, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Specializza il metodo {@code setButtonInMainActivity(Button button, int type)} per il tasto
     * database.
     */
    private void setButtonDb() {
        setButtonInMainActivity(buttonDb, TablesActivity.FROM_DB);
    }

    /**
     * Specializza il metodo {@code setButtonInMainActivity(Button button, int type)} per il tasto
     * file.
     */
    private void setButtonFile() {
        setButtonInMainActivity(buttonFile, TablesActivity.FROM_FILE);
    }

    /**
     * Il metodo può essere richiamato in altre activity affinché queste ultime possano istanziare
     * una nuova {@link MainActivity}.
     * @param context Activity o altro contesto da dove richiamare il metodo
     */
    public static void openMainActivity(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }
}
package com.fralav.rtminer.client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * La classe implementa tutte le funzioni utili alla comunicazione con il server. Per poter
 * permettere a questa classe di poter istanziare un solo oggetto (utilizzando il design pattern
 * {@code singleton}), il costruttore è privato.
 * @author Francesco Lavecchia
 */
public class Client {

    /**
     * Oggetto che contiene le informazione del socket.
     */
    private SocketContainer socketContainer;

    /**
     * Unica istanza della classe alla quale il programmatore può fare riferimento.
     */
    private static final Client instance = new Client();

    /**
     * Indirizzo IP del server al quale connettersi.
     */
    private String ip;

    /**
     * Porta del server alla quale connettersi.
     */
    private int port;

    /**
     * Identifica lo stato della socket: {@code True} se la connessione è avvenuta, {@code false}
     * altrimenti.
     */
    private boolean connected;

    /**
     * Oggetto generico utile a sincronizzare tra di loro i thread che comunicano col server. In
     * Android, la comunicazione con il server tramite socket deve necessariamente avvenire in un
     * thread diverso dall'{@code UI Thread} e sincorinizzando tutti i thread con questo oggetto,
     * si evita che più thread possano effettuare le stesse operazioni contemporaneamente. Tutti i
     * thread che comunicano tramite socket all'interno di questo progetto sono {@code synchronized}
     * con questo oggetto.
     */
    private static final Object lock = new Object();

    /**
     * Rappresenta l'ID del task che preleva i nomi delle tabelle dal database.
     */
    private static final int TASK_GET_TABLES_FROM_DB = 1;

    /**
     * Rappresenta l'ID del task che preleva i nomi dei file dall'archivio.
     */
    private static final int TASK_GET_FILES_FROM_ARCHIVE = 2;

    /**
     * Rappresenta l'ID del task che apprende un albero da un training set del database.
     */
    private static final int TASK_LEARN_TREE_FROM_DB = 3;

    /**
     * Rappresenta l'ID del task che preleva un albero precedentemente serializzato su file.
     */
    private static final int TASK_GET_TREE_FROM_FILE = 4;

    /**
     * Rappresenta l'ID del task che fornisce al client la rappresentazione in {@link String}
     * dell'albero.
     */
    private static final int TASK_PRINT_TREE = 5;

    /**
     * Rappresenta l'ID del task che fornisce la predizione dell'albero al client.
     */
    private static final int TASK_PREDICT_TREE = 6;

    /**
     * Il server invia questa stringa al client quando un'operazione va a buon fine.
     */
    public static final String OK = "ok";

    /**
     * Il server invia questa stringa al client quando si verifica un errore durante la computazione
     * dell'oggetto di tipo {@code Data}.
     */
    public static final String DATA_ERROR = "dataError";

    /**
     * Il server invia questa stringa al client quando la tabella selezionata non è presente
     * all'interno del database.
     */
    public static final String TABLE_NOT_FOUND = "tableNotFound";

    /**
     * Il server invia questa stringa al client quando il file selezionato non è presente
     * all'interno dell'archivio.
     */
    public static final String FILE_NOT_FOUND = "fileNotFound";

    /**
     * Il server invia questa stringa al client quando nessuna tabella è presente all'interno
     * del database.
     */
    public static final String NO_TABLES_FOUND = "NoTablesFound";

    /**
     * Il server invia questa stringa al client quando nessun file è presente all'interno
     * dell'archivio.
     */
    public static final String NO_FILES_FOUND = "NoFilesFound";

    /**
     * Istanzia un oggetto della classe. Viene definito come privato proprio perché la classe è una
     * classe singoletto.
     */
    private Client() {}

    /**
     * Restituisce l'unica istanza della classe.
     * @return {@code instance}, unica istanza della classe
     */
    public static Client getInstance() {
        return instance;
    }

    /**
     * Imposta l'attributo {@code ip} con quello passato in input.
     * @param ip Indirizzo IP del server.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Restituisce l'IP del server.
     * @return {@code ip}, indirizzo IP del server.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Imposta l'attributo {@code port} con quello passato in input.
     * @param port Porta del server.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Restituisce la porta del server.
     * @return {@code port}
     */
    public int getPort() {
        return port;
    }

    /**
     * Restituisce {@code true} se il client è connesso al server, {@code false} altrimenti.
     * @return {@code connected}
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Il metodo è dichiarato {@code synchronized}, ovvero vincola l'esecuzione del metodo stesso ad
     * un solo thread per volta. Una volta creata la connessione col server, inizializza l'attributo
     * {@code socketContainer}. Imposta l'attributo {@code connected} uguale a {@code true} se la
     * connessione è avvenuta con successo, {@code false} altrimenti.
     */
    public void connect() {
        synchronized (lock) {
            try {
                socketContainer = new CreateSocket(ip, port).execute().get(5, TimeUnit.SECONDS);
                if (socketContainer != null) {
                    connected = true;
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                Log.e("Client", String.valueOf(e));
                connected = false;
            }
        }
    }

    /**
     * Il metodo è dichiarato {@code synchronized}. Si occupa di chiudere il socket, e quindi di
     * chiudere la connessione con il server e imposta l'attributo {@code connected} uguale a
     * {@code false}.
     */
    public void disconnect() {
        new Thread(() -> {
            synchronized (lock) {
                try {
                    if (connected) {
                        socketContainer.getSocket().close();
                        connected = false;
                    }
                } catch (IOException e) {
                    Log.e("Client", String.valueOf(e));
                }
            }
        }).start();
    }

    /**
     * Istanzia un oggetto di tipo {@link GetTables} e restituisce l'output del metodo {@code get()}.
     * @return Lista delle tabelle del database.
     */
    public LinkedList<String> getTablesFromDb() {
        try {
            return new GetTables(TASK_GET_TABLES_FROM_DB).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("Client", String.valueOf(e));
            return null;
        }
    }

    /**
     * Istsanzia un oggetto di tipo {@link GetTables} e restituisce l'output del metodo {@code get()}.
     * @return Lista dei file presenti nell'archivio.
     */
    public LinkedList<String> getFilesFromArchive() {
        try {
            return new GetTables(TASK_GET_FILES_FROM_ARCHIVE).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("Client", String.valueOf(e));
            return null;
        }
    }

    /**
     * Comunica al server di apprendere l'albero dalla tabella del database selezionata in input
     * restituendone l'esito.
     * @param table Nome della tabella del database dove risiede il data set.
     * @return Esito della computazione.
     */
    public String learnTreeFromDb(String table) {
        try {
            new WriteObjectToSocket(TASK_LEARN_TREE_FROM_DB).execute();
            new WriteObjectToSocket(table).execute();
            return new ReadObjectFromSocket().execute().get().toString();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("Client", String.valueOf(e));
            return null;
        }
    }

    /**
     * Comunica al server di voler recuperare l'albero specificato in input, precedentemente appreso
     * e serializzato sul file system del server e ne restituisce l'esito.
     * @param file Nome del file dove è serializzato l'albero.
     * @return Esito della computazione.
     */
    public String getTreeFromFile(String file) {
        try {
            new WriteObjectToSocket(TASK_GET_TREE_FROM_FILE).execute();
            new WriteObjectToSocket(file).execute();
            return new ReadObjectFromSocket().execute().get().toString();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("Client", String.valueOf(e));
            return null;
        }
    }

    /**
     * Richiede al server l'albero appreso sotto forma di stringa per poterla stampare a video.
     * @return Albero appreso sottoforma di stringa
     */
    public String printTree() {
        try {
            new WriteObjectToSocket(TASK_PRINT_TREE).execute();
            return new ReadObjectFromSocket().execute().get().toString();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("Client", String.valueOf(e));
            return null;
        }
    }

    /**
     * Richiede al server di voler iniziare la fase di predizione.
     */
    public void startPredictionMode() {
        new WriteObjectToSocket(TASK_PREDICT_TREE).execute();
    }

    /**
     * Legge l'oggetto ricevuto dal server attraverso lo stream di input e ne restituisce il risultato.
     * @return Oggetto ricevuto dal server.
     */
    public Object readObjectFromSocket() {
        try {
            return new ReadObjectFromSocket().execute().get().toString();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("Client", String.valueOf(e));
            return null;
        }
    }

    /**
     * Invia al server, tramite lo stream di output, l'oggetto {@code obj}.
     * @param obj Oggetto da inviare al server.
     */
    public void writeObjectToSocket(Object obj) {
        new WriteObjectToSocket(obj).execute();
    }

    /**
     * La classe contiene le informazioni del socket.
     */
    private class SocketContainer {

        /**
         * Socket che stabilisce la connessione al server.
         */
        private Socket socket;

        /**
         * Oggetto di output stream.
         */
        private ObjectOutputStream out;

        /**
         * Oggetto di input stream.
         */
        private ObjectInputStream in;

        /**
         * Inizializza gli attributi d'istanza con quelli passati in input.
         * @param socket Socket che stabilisce la connessione al server.
         * @param in Stream di input
         * @param out Stream di output
         */
        private SocketContainer(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
            this.socket = socket;
            this.out = out;
            this.in = in;
        }

        /**
         * Restituisce l'attributo {@code socket}.
         * @return {@code socket}
         */
        private Socket getSocket() {
            return socket;
        }

        /**
         * Restituisce l'attributo {@code out}.
         * @return {@code out}
         */
        private ObjectOutputStream getOut() {
            return out;
        }

        /**
         * Restituisce l'attributo {@code in}.
         * @return {@code in}
         */
        private ObjectInputStream getIn() {
            return in;
        }
    }

    /**
     * La classe estende {@link AsyncTask}, la quale ha lo scopo di rendere user friendly l'iniezione
     * di nuovi thread nell'UI Thread. {@code CreateSocket} crea un nuovo thread all'interno del quale avviene la
     * connessione con il server. Dopo l'esecuzione del thread, viene inizializzato l'attributo
     * {@code socketContainer} con l'output dell'esecuzione del thread.
     */
    private class CreateSocket extends AsyncTask<Void, Void, SocketContainer> {

        private String address;

        private int port;

        private CreateSocket(String address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        protected SocketContainer doInBackground(Void... voids) {
            try {
                Socket socket = new Socket(ip, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                return new SocketContainer(socket, in, out);
            } catch (IOException e) {
                Log.e("Client", String.valueOf(e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(SocketContainer sContainer) {
            super.onPostExecute(sContainer);
            synchronized (lock) {
                socketContainer = sContainer;
            }
        }
    }

    /**
     * La classe estende {@link AsyncTask}, la quale ha lo scopo di rendere user friendly l'iniezione
     * di nuovi thread nell'UI Thread. {@code GetTables} crea un nuovo thread all'interno del quale
     * si chiede al server la lista dei file o delle tabelle disponibili, specificando questa scelta
     * nel costruttore della classe.
     */
    private class GetTables extends AsyncTask<Void, Void, LinkedList<String>> {
        private int type;

        private GetTables(int type) {
            this.type = type;
        }

        @Override
        protected LinkedList<String> doInBackground(Void... voids) {
            synchronized (lock) {
                try {
                    socketContainer.getOut().writeObject(type);
                    return (LinkedList<String>) socketContainer.getIn().readObject();
                } catch (IOException | ClassNotFoundException e){
                    Log.e("Client", String.valueOf(e));
                    return null;
                }
            }
        }
    }

    /**
     * La classe estende {@link AsyncTask}, la quale ha lo scopo di rendere user friendly l'iniezione
     * di nuovi thread nell'UI Thread. {@code ReadObjectFromSocket} crea un nuovo thread all'interno
     * del quale viene letto l'oggetto ricevuto dal server nello stream di input e viene restituito
     * in output.
     */
    private class ReadObjectFromSocket extends AsyncTask<Void, Void, Object> {

        @Override
        protected Object doInBackground(Void... voids) {
            synchronized (lock) {
                try {
                    return socketContainer.getIn().readObject().toString();
                } catch (ClassNotFoundException | IOException e) {
                    Log.e("Client", String.valueOf(e));
                    return null;
                }
            }
        }
    }

    /**
     * La classe estende {@link AsyncTask}, la quale ha lo scopo di rendere user friendly l'iniezione
     * di nuovi thread nell'UI Thread. {@code WriteObjectToSocket} crea un nuovo thread all'interno
     * del quale viene inviato l'oggetto specificato all'interno dell'unico parametro del costruttore
     * della classe al server attraverso lo stream di output.
     */
    private class WriteObjectToSocket extends AsyncTask<Void, Void, Void> {
        private Object obj;

        private WriteObjectToSocket(Object obj) {
            this.obj = obj;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            synchronized (lock) {
                try {
                    socketContainer.getOut().writeObject(obj);
                } catch (IOException e) {
                    Log.e("Client", String.valueOf(e));
                }
                return null;
            }
        }
    }

}
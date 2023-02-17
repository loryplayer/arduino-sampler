package database;

import java.util.Objects;

import file_managers.DataArchiver;
import interfaces.ListHandler;
import interfaces.DatabaseElement;

/**
 * <p>Classe CollectorList utilizzata per gestire l'insieme delle classi {@link Database}.</p>
 * <p>
 * Implementa l'interfaccia {@link ListHandler}.
 * </p>
 * Funziona prevalentemente come un Array:
 * <ul style="margin-top: 0px">
 *     <li>
 *         {@link #add(DatabaseElement)}
 *     </li>
 *     <li>
 *         {@link #addFromRawInformation(String)}
 *     </li>
 *     <li>
 *         {@link #remove(DatabaseElement)}
 *     </li>
 *     <li>
 *         {@link #removeFromJDBC_URL(String)}
 *     </li>
 *     <li>
 *         {@link #setDataArchiver(DataArchiver)}
 *     </li>
 *     <li>
 *         {@link #save()}
 *     </li>
 *     <li>
 *         {@link #isEmpty()}
 *     </li>
 *     <li>
 *         {@link #getDatabases()}
 *     </li>
 *     <li>
 *         {@link #getDatabase(int)}
 *     </li>
 *     <li>
 *         {@link #getDatabasesNames()}
 *     </li>
 *     <li>
 *         {@link #getDatabaseByName(String)}
 *     </li>
 *     <li>
 *         {@link #getDatabasesByJDBC_URL(String)}
 *     </li>
 *     <li>
 *         {@link #getDatabasesFromDataArchiver()}
 *     </li>
 * </ul>
 */
public class DatabaseList implements ListHandler {

    /**
     * Array di oggetti {@link Database}
     */
    private Database[] databases = new Database[0];

    /**
     * Oggetto {@link DataArchiver}, utilizzato per effettuare il salvataggio del {@link Database}
     */
    private DataArchiver database_archiver;

    /**
     * Metodo utilizzato per aggiungere {@link Database} all'array {@link #databases}.
     *
     * @param element {@link Database}
     */
    public void add(DatabaseElement element) {
        Database database = (Database) element;
        int no_databases = this.databases.length;
        Database[] new_drivers = new Database[no_databases + 1];
        System.arraycopy(this.databases, 0, new_drivers, 0, no_databases);
        new_drivers[no_databases] = database;
        this.databases = new_drivers;
    }


    /**
     * Metodo utilizzato per aggiungere un {@link Database} realizzato dalle informazioni salvate.
     *
     * @param rawInformation Stringa contenente le informazioni grezze lette dal file di salvataggio
     * @see #fromStringToMap(String)
     */
    public void addFromRawInformation(String rawInformation) {
        this.add(new Database(this.fromStringToMap(rawInformation)));
    }

    /**
     * Metodo utilizzato per rimuovere {@link Database} dall'array {@link #databases}.
     *
     * @param element {@link Database}
     */
    public void remove(DatabaseElement element) {
        Database database = (Database) element;
        Database[] new_databases = new Database[this.databases.length];
        int counter = 0;
        for (Database current_database : this.databases) {
            if (current_database != database) {
                new_databases[counter] = current_database;
                counter++;
            }
        }
        Database[] new_databases_resized = new Database[counter];
        System.arraycopy(new_databases, 0, new_databases_resized, 0, counter);
        this.databases = new_databases_resized;
    }

    /**
     * Metodo utilizzato per rimuovere {@link Database} dall'array {@link #databases} tramite JDBC_URL.
     *
     * @param JDBC_URL indirizzo principale del {@link Driver}
     * @see Database#getJDBC_URL()
     */
    public void removeFromJDBC_URL(String JDBC_URL) {
        if (this.databases.length == 0)
            return;
        Database[] new_databases = new Database[this.databases.length - this.getDatabasesByJDBC_URL(JDBC_URL).getDatabases().length];
        int counter = 0;
        for (Database current_database : this.databases) {
            if (!Objects.equals(current_database.getJDBC_URL(), JDBC_URL)) {
                new_databases[counter] = current_database;
                counter++;
            }
        }
        Database[] new_databases_resized = new Database[counter];
        System.arraycopy(new_databases, 0, new_databases_resized, 0, counter);
        this.databases = new_databases_resized;
    }


    /**
     * Metodo utilizzato per impostare il {@link DataArchiver} al {@link Database}.
     *
     * @param data_archiver {@link DataArchiver} da utilizzare
     */
    public void setDataArchiver(DataArchiver data_archiver) {
        this.database_archiver = data_archiver;
    }

    /**
     * Metodo utilizzato per salvare ogni {@link Database} contenuto in {@link #databases}.
     * <p>
     * <b>Nota</b><br>
     * Ad ogni richiamo di questa funzione viene sovrascritto il file di salvataggio con le informazioni dei database.<br>
     * Ogni {@link Database} viene salvato su una riga.
     * </p>
     *
     * @see DataArchiver#overrideFile()
     * @see DataArchiver#save(DatabaseElement)
     */

    public void save() {
        this.database_archiver.overrideFile();
        for (Database database : this.databases) {
            this.database_archiver.save(database, true);
        }
    }

    /**
     * Metodo utilizzato per verificare se {@link #databases} risulta vuoto.
     *
     * @return {@code true} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se non sono presenti {@link Database}
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se sono presenti {@link Database}
     *     </li>
     * </ul>
     */
    public boolean isEmpty() {
        return this.databases.length == 0;
    }

    /**
     * <p>Metodo utilizzato per verificare se {@link #databases} contiene già un {@link Database} che presenta le stesse informazioni di quello passato come parametro.</p>
     * <p>
     * Il confronto tra Database utilizza i seguenti metodi:<br>
     * <ul style="margin-top:0px">
     *     <li>
     *         {@link Database#getName()}
     *     </li>
     *     <li>
     *         {@link Database#getJDBC_URL()}
     *     </li>
     * </ul>
     *
     * @param database {@link Database} da confrontare
     * @return <ul style="margin-top: 0px">
     *     <li>
     *         se è già presente un {@link Database} con le stesse informazioni
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se non è presente un {@link Database} con le stesse informazioni
     *     </li>
     * </ul>
     */
    public boolean has(Database database) {
        for (Database current_database : this.databases)
            if (Objects.equals(current_database.getName(), database.getName()) && Objects.equals(current_database.getJDBC_URL(), database.getJDBC_URL()))
                return true;
        return false;
    }

    /**
     * Metodo utilizzato per ottenere i vari {@link Database} contenuti in {@link #databases}.
     *
     * @return {@link Database} ({@code list})
     */
    public Database[] getDatabases() {
        return this.databases;
    }

    /**
     * Metodo utilizzato per ottenere tutti i {@link Database} raggiungibili contenuti in {@link #databases}.
     *
     * @return {@link Database} ({@code list})
     */

    public Database[] getReachableDatabases() {
        DatabaseList db_list = new DatabaseList();
        for (Database database : this.databases)
            if (database.testConnection())
                db_list.add(database);
        return db_list.getDatabases();
    }

    /**
     * Metodo utilizzato per ottenere il {@link Database}, situato nella posizione corrispondente del parametro passato, contenuto in {@link #databases}.
     *
     * @param index posizione del {@link Database}
     * @return {@link Database}
     */
    public Database getDatabase(int index) {
        return this.databases[index];
    }

    /**
     * Metodo utilizzato per ottenere il nome di tutti i {@link Database} contenuti in {@link #databases}.
     *
     * @return Nomi dei {@link Database} ({@code list})
     */
    public String[] getDatabasesNames() {
        String[] names = new String[this.databases.length];
        for (int i = 0; i < this.databases.length; i++)
            names[i] = this.getDatabase(i).getName();
        return names;
    }

    /**
     * Metodo utilizzato per ottenere il {@link Database} in base al nome passato come parametro
     *
     * @param name Stringa contenente il nome del {@link Database}
     * @return {@link Database}
     * @see Database#getName()
     */
    public Database getDatabaseByName(String name) {
        for (Database database : this.databases)
            if (Objects.equals(database.getName(), name))
                return database;
        return null;
    }

    /**
     * Metodo utilizzato per ottenere i {@link Database} in base al sistema di gestione passato come parametro.
     *
     * @param RDBMS Stringa contenente il nome del sistema di gestione
     * @return Variabile {@link DatabaseList} contenente i database filtrati
     * @see Driver#getRDBMS_NAME()
     */
    public DatabaseList getDatabasesByRDBMS(String RDBMS) {
        DatabaseList db_list = new DatabaseList();
        for (Database database : this.databases)
            if (Objects.equals(database.getDriver().getRDBMS_NAME(), RDBMS))
                db_list.add(database);
        return db_list;
    }

    /**
     * Metodo utilizzato per ottenere i {@link Database} in base all'indirizzo principale del {@link Driver}.
     *
     * @param driverJDBC_URL Stringa contenente l'indirizzo principale del {@link Driver}
     * @return Variabile {@link DatabaseList} contenente i database filtrati
     * @see Driver#getJDBC_URL()
     */
    public DatabaseList getDatabasesByJDBC_URL(String driverJDBC_URL) {
        DatabaseList db_list = new DatabaseList();
        for (Database database : this.databases)
            if (Objects.equals(database.getDriver().getJDBC_URL(), driverJDBC_URL))
                db_list.add(database);
        return db_list;
    }

    /**
     * Metodo utilizzato per inizializzare la variabile {@link #databases} dalle informazioni dei {@link Database} salvati.
     *
     * @see DataArchiver#getElementsSaved()
     */
    public void getDatabasesFromDataArchiver() {
        this.database_archiver.loadData();
        this.databases = ((DatabaseList) this.database_archiver.getElementsSaved()).getDatabases();
    }

    /**
     * Metodo utilizzato per stampare i nomi dei vari {@link Database} contenuti in {@link #databases}.
     * <p>
     * <b>NOTA</b><br>
     * Utilizzato solo nei test
     * </p>
     */
    public void print_names() {
        StringBuilder output = new StringBuilder();
        for (Database database : this.databases) {
            output.append(database.getName()).append(" ,");
        }
        output.append(";");
        System.out.println(output);
    }

}

package database;

import file_managers.DataArchiver;

import java.util.Objects;

import interfaces.ListHandler;
import interfaces.DatabaseElement;

/**
 * <p>Classe DriverList, utilizzata per gestire l'insieme delle classi {@link Driver}.</p>
 * <p>
 *     Implementa l'interfaccia {@link ListHandler}.
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
 *         {@link #setDataArchiver(DataArchiver)}
 *     </li>
 *     <li>
 *         {@link #save()}
 *     </li>
 *     <li>
 *         {@link #getDriverConnections()}
 *     </li>
 *     <li>
 *         {@link #getDriverByJDBC_URL(String)}
 *     </li>
 *     <li>
 *         {@link #getDriversFromDataArchiver()}
 *     </li>
 * </ul>
 */
public class DriverList implements ListHandler {
    /**
     * Array di oggetti {@link Driver}
     */
    private Driver[] drivers = new Driver[0];


    /**
     * Oggetto {@link DataArchiver}, utilizzato per effettuare il salvataggio del {@link Driver}
     */
    private DataArchiver dataArchiver;


    /**
     * Metodo utilizzato per aggiungere {@link Driver} all'array {@link #drivers}.
     *
     * @param element {@link Driver}
     */
    public void add(DatabaseElement element) {
        Driver driver = (Driver) element;
        int no_drivers = drivers.length;
        Driver[] new_drivers = new Driver[no_drivers + 1];
        System.arraycopy(drivers, 0, new_drivers, 0, no_drivers);
        new_drivers[no_drivers] = driver;
        this.drivers = new_drivers;
    }

    /**
     * Metodo utilizzato per aggiungere un {@link Driver} realizzato dalle informazioni salvate.
     *
     * @param rawInformation Stringa contenente le informazioni grezze lette dal file di salvataggio
     * @see #fromStringToMap(String)
     */

    public void addFromRawInformation(String rawInformation) {
        this.add(new Driver(this.fromStringToMap(rawInformation)));
    }

    /**
     * Metodo utilizzato per rimuovere {@link Driver} dall'array {@link #drivers}.
     *
     * @param element {@link Driver}
     */
    public void remove(DatabaseElement element) {
        Driver driver = (Driver) element;
        if (this.drivers.length == 0)
            return;
        Driver[] new_drivers = new Driver[this.drivers.length];
        int counter = 0;
        for (Driver current_driver : this.drivers) {
            if (!Objects.equals(current_driver.getJDBC_URL(), driver.getJDBC_URL())) {
                new_drivers[counter] = current_driver;
                counter++;
            }
        }
        Driver[] new_collectors_resized = new Driver[counter];
        System.arraycopy(new_drivers, 0, new_collectors_resized, 0, counter);
        this.drivers = new_collectors_resized;
    }

    /**
     * Metodo utilizzato per impostare il {@link DataArchiver} al {@link Driver}.
     *
     * @param driver_archiver {@link DataArchiver} da utilizzare
     */
    public void setDataArchiver(DataArchiver driver_archiver) {
        this.dataArchiver = driver_archiver;
    }

    /**
     * Metodo utilizzato per salvare ogni {@link Driver} contenuto in {@link #drivers}.
     * <p>
     * <b>Nota</b><br>
     * Ad ogni richiamo di questa funzione viene sovrascritto il file di salvataggio con le informazioni dei driver.<br>
     * Ogni {@link Driver} viene salvato su una riga.
     * </p>
     *
     * @see DataArchiver#overrideFile()
     * @see DataArchiver#save(DatabaseElement)
     */

    public void save() {
        this.dataArchiver.overrideFile();
        for (Driver driver : this.drivers) {
            this.dataArchiver.save(driver, true);
        }
    }

    /**
     * Metodo utilizzato per interrompere tutte le comunicazioni di ogni {@link Driver} con il relativo sistema di gestione dei database relazionali.
     */
    public void close_all_connections() {
        for (Driver driver : this.drivers) {
            driver.closeConnection();
        }
    }


    /**
     * Metodo utilizzato per ottenere i vari {@link Driver} contenuti in {@link #drivers}.
     *
     * @return {@link Driver} ({@code list})
     */
    public Driver[] getDriverConnections() {
        return this.drivers;
    }

    /**
     * Metodo utilizzato per ottenere il {@link Driver}, situato nella posizione corrispondente del parametro passato, contenuto in {@link #drivers}.
     *
     * @param index posizione del {@link Driver}
     * @return {@link Driver}
     */
    public Driver getDriver(int index) {
        return this.drivers[index];
    }

    /**
     * Metodo utilizzato per ottenere i {@link Driver} in base al sistema di gestione passato come parametro.
     *
     * @param RDBMS Stringa contenente il nome del sistema di gestione
     * @return {@link Driver} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se è stato trovato un {@link Driver} avente come sistema di gestione quello passato come parametro
     *     </li>
     * </ul>
     * {@code null} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se non è presente nessun {@link Driver}
     *     </li>
     * </ul>
     * @see Driver#getRDBMS_NAME()
     */
    public Driver getDriverByRDBMS(String RDBMS) {
        for (Driver driver : this.drivers) {
            if (Objects.equals(driver.getRDBMS_NAME(), RDBMS))
                return driver;
        }
        return null;
    }

    /**
     * Metodo utilizzato per ottenere i {@link Driver} in base al sistema di gestione passato come parametro.
     *
     * @param JDBC_URL Stringa contenente l'intero indirizzo utilizzabile per connettersi al RDBMS
     * @return {@link Driver} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se è stato trovato un {@link Driver} avente come sistema di gestione quello passato come parametro
     *     </li>
     * </ul>
     * {@code null} <br>
     * <ul style="margin-top: 0px">
     *     <li>
     *         se non è presente nessun {@link Driver}
     *     </li>
     * </ul>
     * @see Driver#getRDBMS_NAME()
     */

    public Driver getDriverByJDBC_URL(String JDBC_URL) {
        for (Driver driver : this.drivers) {
            if (Objects.equals(driver.getJDBC_URL(), JDBC_URL))
                return driver;
        }
        return null;
    }

    /**
     * Metodo utilizzato per inizializzare la variabile {@link #drivers} dalle informazioni dei {@link Driver} salvati.
     *
     * @see DataArchiver#getElementsSaved()
     */
    public void getDriversFromDataArchiver() {
        this.dataArchiver.loadData();
        this.drivers = ((DriverList) this.dataArchiver.getElementsSaved()).getDriverConnections();
    }


    /**
     * Metodo utilizzato per ottenere il sistema di salvataggio del {@link Driver}
     *
     * @return {@link #dataArchiver}
     */
    public DataArchiver getDataArchiver() {
        return this.dataArchiver;
    }
}

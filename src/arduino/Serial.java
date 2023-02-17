package arduino;

import com.fazecast.jSerialComm.SerialPort;
import interfaccia.IndexController;

import java.io.IOException;
import java.util.Objects;

import static interfaccia.IndexController.primaryController;

/**
 * <p>Classe Serial, utilizzata per usufruire dei metodi contenuti all'interno della libreria {@link com.fazecast.jSerialComm}.</p>
 * Metodi principali:
 * <ul style="margin-top: 0px">
 *     <li>
 *         {@link #open()}
 *     </li>
 *     <li>
 *         {@link #open(boolean)}
 *     </li>
 *     <li>
 *         {@link #read_and_collect()}
 *     </li>
 *     <li>
 *         {@link #close()}
 *     </li>
 *     <li>
 *         {@link #getPortLocation()}
 *     </li>
 *     <li>
 *         {@link #getDeviceName()}
 *     </li>
 *     <li>
 *         {@link #getDataCollectorList()}
 *     </li>
 *     <li>
 *         {@link #getSamplingSettings()}
 *     </li>
 * </ul>
 */

public class Serial {


    /**
     * <p>Variabile di tipo carattere, viene confrontata con il primo byte che viene letto dalla seriale.</p>
     * Il valore 'R' indica 'Ready', utile per capire se l'interfaccia hardware a cui siamo connessi è pronta ad eseguire e a rispondere alle istruzioni che le invieremo.
     */
    private final char ReadyConnection = 'R';


    /**
     * <p>Variabile di tipo stringa, viene utilizzata per l'auto inizializzazione.</p>
     * Indica l'interfaccia preferenziale a cui collegarsi.
     */
    private final String default_name_device = "arduino uno";

    /**
     * Variabile {@link CollectorList}, utilizzata per memorizzare i dati grezzi ricevuti dalla seriale.
     */
    private CollectorList collector_used;

    /**
     * Variabile {@link DataCollectorList}, utilizzata per memorizzare i dati elaborati ricevuti dalla seriale.
     */
    private DataCollectorList dataCollectorUsed;

    /**
     * Variabile {@link SamplingSettings} contenente le informazioni sul campionamento.
     */
    private SamplingSettings samplingSettingsUsed;

    /**
     * Variabile di tipo long, utilizzata per il periodo di non-risposta di default in millisecondi.
     */
    private final long default_timeout_ms = 8000; // 15000ms

    /**
     * Variabile di tipo long inizializzata con il periodo di non-risposta di default in millisecondi.
     */
    private long timeout = default_timeout_ms;

    /**
     * Variabile di tipo stringa, utilizzata per memorizzare il nome della porta a cui è connessa la seriale.
     */
    private String portName;

    /**
     * Variabile di tipo stringa, utilizzata per memorizzare il nome del dispositivo connesso.
     */
    private String deviceName;

    /**
     * Variabile {@link SerialPort} della libreria {@link com.fazecast.jSerialComm}, utilizzata per tutte le operazioni di lettura/scrittura dalla/sulla seriale.
     */
    private SerialPort serialPort;


    /**
     * Costruttore vuoto, utilizzato per evitare di richiamare il costruttore {@link #Serial(String, String)}, utile quando non si conosce subito il nome della porta e del dispositivo.
     */
    public Serial() {
    }

    /**
     * Costruttore della Classe {@link Serial}.
     *
     * @param portName   stringa contenente il nome della porta a cui connettersi
     * @param deviceName stringa contenente il nome del dispositivo a cui connettersi
     */

    public Serial(String portName, String deviceName) {
        this.portName = portName;
        this.deviceName = deviceName;
        this.initialize();
    }

    /**
     * Metodo utilizzato per:
     * <ul style="margin-top: 0px">
     *     <li>
     *         ottenere e inizializzare la variabile {@link #serialPort}
     *     </li>
     *     <li>
     *         impostare i vari oggetti per la raccolta dei dati
     *     </li>
     * </ul>
     */
    private void initialize() {
        this.serialPort = SerialPort.getCommPort(this.portName);
        this.setDataCollector();
    }


    /**
     * Metodo utilizzato per:
     * <ul style="margin-top: 0px">
     *     <li>
     *         auto-inizializzare la variabile {@link #serialPort} tramite l'utilizzo di una stringa contenente il nome del dispositivo a cui collegarsi
     *     </li>
     *     <li>
     *         impostare i vari oggetti per la raccolta dei dati
     *     </li>
     * </ul>
     *
     * @param device_name se:
     *                    <ul style="margin-top: 0px">
     *                        <li>
     *                            {@code null} -> viene cercato il dispositivo con il nome corrispondente a quello contenuto nella variabile {@link #default_name_device}
     *                        </li>
     *                        <li>
     *                            {@code String} -> viene cercato il dispositivo con il nome corrispondente a tale stringa
     *                        </li>
     *                    </ul>
     * @throws NullPointerException se non viene trovato alcun dispositivo
     */
    public void autoInitialize(String device_name) {
        String target = Objects.requireNonNullElse(device_name, default_name_device);
        for (SerialPort port : SerialPort.getCommPorts()) {
            if (port.getPortDescription().toLowerCase().contains(target)) {
                this.serialPort = port;
                this.setDeviceName(port.getPortDescription().substring(0, port.getPortDescription().indexOf(" (COM")));
                this.setPortName(port.getSystemPortName());
                break;
            }
        }
        if (this.serialPort == null) {
            throw new NullPointerException();
        }
        this.setDataCollector();
    }


    /**
     * Metodo utilizzato per inizializzare gli oggetti per la raccolta dati:
     * <ul style="margin-top: 0px">
     *     <li>
     *         {@link #dataCollectorUsed} (vedi: {@link DataCollectorList})
     *     </li>
     *     <li>
     *         {@link #collector_used} (vedi: {@link CollectorList})
     *     </li>
     *     <li>
     *         {@link #samplingSettingsUsed} (vedi: {@link SamplingSettings})
     *     </li>
     *     <li>
     *         {@link Collector} (per la temperature)
     *     </li>
     *     <li>
     *         {@link Collector} (per l'umidità)
     *     </li>
     * </ul>
     */
    private void setDataCollector() {
        this.dataCollectorUsed = new DataCollectorList();
        this.collector_used = new CollectorList();
        this.samplingSettingsUsed = new SamplingSettings();
        Collector temperature_collector = new Collector('T');
        Collector humidity_collector = new Collector('H');
        this.collector_used.add(temperature_collector);
        this.collector_used.add(humidity_collector);
    }

    /**
     * Metodo utilizzato per aprire il canale di comunicazione e verificare che l'interfaccia hardware connessa sia pronta.
     *
     * @return {@code true} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         nel caso in cui il dispositivo risponda, nei tempi previsti da {@link #timeout}, restituendo un valore uguale a quello contenuto nella variabile {@link #ReadyConnection}
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         non c'è stata alcuna risposta da parte del dispositivo
     *     </li>
     * </ul>
     */
    public boolean open() {
        this.serialPort.openPort();
        //ricezione da parte di arduino per quando è pronto
        return this.read_command('R');
    }

    /**
     * Metodo utilizzato per aprire il canale di comunicazione, nel quale è facoltativo il controllo di risposta da parte del dispositivo.
     *
     * @param check <br>
     *              {@code true} <br>
     *              <ul style="margin-top:0">
     *                  <li>
     *                      viene verificata la risposta da parte del dispositivo (vedi: {@link #open()})
     *                  </li>
     *              </ul>
     *              {@code false} <br>
     *              <ul style="margin-top:0">
     *                  <li>
     *                      non viene effettuato nessun tipo di controllo, viene solo aperta la comunicazione
     *                  </li>
     *              </ul>
     */
    public void open(boolean check) {
        this.serialPort.openPort();
        if (check)
            //ricezione da parte di arduino per quando è pronto
            this.read_command('R');
    }

    /**
     * Metodo utilizzato per la scrittura sulla seriale.
     * <p>
     * Se l'esito della scrittura produce un errore, allora viene interrotto il campionamento e rimosso l'oggetto {@link Serial} dalla selezione delle interfacce nel pannello di controllo della classe {@link IndexController}
     * </p>
     *
     * @param b byte da scrivere
     */
    private void write(byte b) {
        try {
            this.serialPort.getOutputStream().write(b);
            this.serialPort.getOutputStream().flush();
        } catch (IOException e) {
            primaryController.stopSampling();
            primaryController.removeSerialSelected();
            primaryController.getLogger().write("Errore durante la scrittura sulla seriale");
//            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo utilizzato per la lettura di un unico byte in ricezione dalla seriale.
     *
     * @return byte (int)
     */
    private int read() {
        try {
            return this.serialPort.getInputStream().read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Metodo utilizzato per inviare comandi alla seriale, esistono tre principali comandi:
     * <h2 style="margin-bottom:0">
     * {@link #ReadyConnection} [R]
     * </h2>
     * <p>
     * Viene scritto sulla console (vedi: {@link interfaccia.Logger}) il nome del dispositivo seguito dall'esito della connessione, il quale può essere:
     *     <ul style="margin-top:0">
     *         <li>
     *             <b>[Ok...]</b> -> se il dispositivo ha risposto correttamente nel momento dell'apertura della comunicazione
     *         </li>
     *         <li>
     *             <b>[Timeout...]</b> -> se il dispositivo non ha risposto entro il tempo di {@link #timeout} o ha risposto in maniera errata nel momento dell'apertura della comunicazione
     *         </li>
     *     </ul>
     * </p>
     * <h2 style="margin-bottom:0">
     *     Temperature [T] / Humidity [H]
     * </h2>
     * <p>
     *     Viene letto il dato corrispondente o alla Temperatura o all'Umidità e salvato all'interno dei raccoglitori.
     * </p>
     * L'esito di quest'operazione si divide in:
     * <ul style="margin-top:0">
     *     <li>
     *         <b>{@code true}</b> se il dato è stato immagazzinato correttamente
     *     </li>
     *     <li>
     *         <b>{@code false}</b> se il dato non è stato immagazzinato correttamente, in questo caso si attende una risposta da parte del dispositivo che se supera un tempo pari a {@link #timeout} comporta la rimozione di tale seriale.
     *     </li>
     * </ul>
     *
     * @param Command carattere corrispondente al comando da eseguire (R, T, H)
     * @return {@code true} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         nel caso in cui il dispositivo ha risposto correttamente all'avvio della comunicazione
     *     </li>
     *     <li>
     *         nel caso in cui il dato da leggere è stato correttamente salvato
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         se il tempo destinato alla risposta da parte del dispositivo ha superato {@link #timeout}
     *     </li>
     * </ul>
     */
    private boolean read_command(char Command) {
        long start_time = System.currentTimeMillis();
        if (Command == this.ReadyConnection) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            /*
                Pausa per effettuare un effetto più gradevole

                [Nomi_dispositivo] [stato]
                ......... (pausa) ........
                [Nomi_dispositivo] [stato]

             */
            primaryController.getLogger().writeInline(this.deviceName + " (" + this.portName + ") ");
        }
        while (System.currentTimeMillis() - start_time < this.timeout) {
            try {
                if ((this.serialPort.getInputStream().available() > 0)) {
                    int data = this.read();
                    if (Command == 'R') {
                        if (data == this.ReadyConnection) {
                            primaryController.getLogger().write("[OK...]");
                            this.close();
                            return true;
                        }
                    } else if (Command == 'T' || Command == 'H') {
                        boolean status = this.collector_used.addDataToCollector(data, Command);
                        if (!status) // se si verificano errori durante la raccolta dei dati si lascia proseguire, se passa un tempo superiore a quello di timeout esce dalla funzione con valore [false]
                            continue;
                        // se sono sorti problemi durante la fase di raccolta dei dati, viene restituito false
                        if (!this.collector_used.getCollectorsFromIdentifier(Command).isCollecting())
                            return true;
                        // se ha finito di raccogliere dati allora esce restituendo true
                    }
                }
            } catch (IOException e) {
                //primary_controller.getLogger().write("Errore durante la lettura dalla seriale");
//                this.close();
//                System.out.println();
//                 throw new RuntimeException(e);
            }
        }
        if (Command != this.ReadyConnection)
            primaryController.getLogger().writeInline("Errore durante la lettura dalla seriale ");
        primaryController.getLogger().write("[Timeout...]");

        this.close();
        return false;
    }

    /**
     * Metodo utilizzato per la scrittura sulla seriale e successivamente la lettura del dato da salvare.
     *
     * @param cmd carattere corrispondente alla tipologia di dato da salvare (Temperature <b>[T]</b>, Humidity <b>[H]</b>)
     * @return {@link Float} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         se il dato è stato correttamente letto (vedi: {@link #read_command(char)})
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         se il tempo destinato alla risposta da parte del dispositivo ha superato {@link #timeout}
     *     </li>
     * </ul>
     */
    // la classe Float può assumere valore null
    private Float write_and_get(char cmd) {
        this.write((byte) cmd);
        boolean success = this.read_command(cmd);
        if (!success)
            return null;
        return this.collector_used.getCollectorsFromIdentifier(cmd).getLastDataAsFloat();
    }

    /**
     * Metodo utilizzato per ottenere il valore corrispondente alla temperatura da parte del dispositivo connesso.
     *
     * @return {@link Float}
     * @see #write_and_get(char)
     * @see #read_command(char)
     */
    private Float read_temperature() {
        return this.write_and_get('T');
    }

    /**
     * Metodo utilizzato per ottenere il valore corrispondente all'umidità da parte del dispositivo connesso.
     *
     * @return {@link Float}
     * @see #write_and_get(char)
     * @see #read_command(char)
     */
    private Float read_humidity() {
        return this.write_and_get('H');
    }

    /**
     * Metodo utilizzato per la raccolta dei vari dati da leggere.
     *
     * @see #read_temperature()
     * @see #read_humidity()
     * @see DataCollector
     */
    public void read_and_collect() {
        Float temperature = this.read_temperature();
//        System.out.println(temperature);
        Float humidity = this.read_humidity();
        if (temperature == null || humidity == null)
            return;
        DataCollector collector = new DataCollector();
        collector.setCollectionTime(System.currentTimeMillis());
        collector.add(primaryController.getDatabaseSelected().getDataStructure().getTemperatureColumnName(), temperature);
        collector.add(primaryController.getDatabaseSelected().getDataStructure().getHumidityColumnName(), humidity);
        this.dataCollectorUsed.add(collector);
    }


    /**
     * Metodo utilizzato per la chiusura della comunicazione seriale col dispositivo
     *
     * @return {@code true} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         se la comunicazione si è chiusa correttamente
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         se il tentativo di chiusura della comunicazione è fallito
     *     </li>
     * </ul>
     */
    public boolean close() {
        return this.serialPort.closePort();
    }

    /**
     * Metodo utilizzato per impostare il nome della porta seriale
     *
     * @param portName Stringa contenente il nome della porta seriale
     * @see #portName
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }

    /**
     * Metodo utilizzato per impostare il nome del dispositivo
     *
     * @param deviceName Stringa contenente il nome del dispositivo
     * @see #deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Metodo utilizzato per impostare i parametri di comunicazione.
     *
     * @param BaudRate velocità in baud desiderata per questa porta seriale.
     * @param DataBits numero di bit di dati da utilizzare per parola.
     * @param StopBits numero di bit di stop da utilizzare.
     * @param Parity   il tipo di controllo degli errori di parità desiderato.
     */
    public void setComParameters(int BaudRate, int DataBits, int StopBits, int Parity) {
        this.serialPort.setComPortParameters(BaudRate, DataBits, StopBits, Parity);
    }

    /**
     * Metodo utilizzato per impostare i parametri di Timeout.
     *
     * @param ResponseTimeout valore di timeout in millisecondi
     * @param TimeoutMode     modalità di Timeout ({@link SerialPort#TIMEOUT_NONBLOCKING}, {@link SerialPort#TIMEOUT_WRITE_BLOCKING}, {@link SerialPort#TIMEOUT_READ_SEMI_BLOCKING}, {@link SerialPort#TIMEOUT_READ_BLOCKING}, {@link SerialPort#TIMEOUT_SCANNER})
     * @param ReadTimeout     Numero di millisecondi d'inattività da tollerare prima di poter effettuare un altra chiamata del metodo {@link #read()}
     * @param WriteTimeout    Numero di millisecondi d'inattività da tollerare prima di poter effettuare un altra chiamata del metodo {@link #write(byte)}}
     */
    public void setComTimeouts(int ResponseTimeout, int TimeoutMode, int ReadTimeout, int WriteTimeout) {
        this.timeout = ResponseTimeout;
        this.serialPort.setComPortTimeouts(TimeoutMode, ReadTimeout, WriteTimeout);
    }

    /**
     * Metodo utilizzato per impostare i parametri di Timeout.
     *
     * @param TimeoutMode  modalità di Timeout ({@link SerialPort#TIMEOUT_NONBLOCKING}, {@link SerialPort#TIMEOUT_WRITE_BLOCKING}, {@link SerialPort#TIMEOUT_READ_SEMI_BLOCKING}, {@link SerialPort#TIMEOUT_READ_BLOCKING}, {@link SerialPort#TIMEOUT_SCANNER})
     * @param ReadTimeout  Numero di millisecondi d'inattività da tollerare prima di poter effettuare un altra chiamata del metodo {@link #read()}
     * @param WriteTimeout Numero di millisecondi d'inattività da tollerare prima di poter effettuare un altra chiamata del metodo {@link #write(byte)}}
     */
    public void setComTimeouts(int TimeoutMode, int ReadTimeout, int WriteTimeout) {
        this.serialPort.setComPortTimeouts(TimeoutMode, ReadTimeout, WriteTimeout);
    }

    /**
     * Metodo utilizzato per ottenere il nome della porta a cui è connesso il dispositivo.
     *
     * @return {@link #portName}
     */
    public String getPortName() {
        return this.portName;
    }

    /**
     * Metodo utilizzato per ottenere il nome della locazione a cui è connesso il dispositivo.
     *
     * @return Stringa contenente la posizione della porta
     */
    public String getPortLocation() {
        return this.serialPort.getPortLocation();
    }

    /**
     * Metodo utilizzato per ottenere il nome del dispositivo.
     *
     * @return {@link #deviceName}
     */
    public String getDeviceName() {
        return this.deviceName;
    }

    /**
     * Metodo utilizzato per ottenere tutti i vari dati letti.
     *
     * @return {@link #dataCollectorUsed}
     * @see DataCollector
     * @see DataCollectorList
     */
    public DataCollectorList getDataCollectorList() {
        return this.dataCollectorUsed;
    }

    /**
     * Metodo utilizzato per ottenere il numero di campionamenti effettuati
     *
     * @return valore intero
     */
    public int getDataCollectorCount() {
        return this.dataCollectorUsed.getDataCollectors().length;
    }

    /**
     * Metodo utilizzato per ottenere l'oggetto contenente le informazioni di campionamento
     *
     * @return {@link #samplingSettingsUsed}
     */
    public SamplingSettings getSamplingSettings() {
        return this.samplingSettingsUsed;
    }
}
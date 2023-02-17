package database;

import java.sql.*;
import java.util.*;


/**
 * <p>Classe DataStructure, utilizzata per ottenere le informazioni riguardanti la struttura SQL del Database.</p>
 * Metodi principali:
 * <ul style="margin-top: 0px">
 *     <li>
 *         {@link #structureInitialize()}
 *     </li>
 *     <li>
 *         {@link #isUsable()}
 *     </li>
 *     <li>
 *         {@link #getTableName()}
 *     </li>
 *     <li>
 *         {@link #getTemperatureColumnName()}
 *     </li>
 *     <li>
 *         {@link #getTuple_ValuesName()}
 *     </li>
 * </ul>
 */

public class DatabaseStructure {

    /**
     * Variabile di tipo stringa che assumerà il nome della colonna relativa al tempo.
     */
    private String timeColumnName;

    /**
     * Variabile di tipo stringa che assumerà il nome della colonna relativa alla temperatura.
     */
    private String temperatureColumnName;

    /**
     * Array di variabili di tipo stringa, contenente i nomi generici relativi alla temperatura <i>(a partire dal nome completo fino alla sua abbreviazione)</i>.
     */
    private final String[] defaultTypeTemperatureNames = new String[]{"temperature", "temp", "t"};


    /**
     * Variabile di tipo stringa che assumerà il nome della tabella in cui verranno salvati i dati.
     */
    private String tableName;

    /**
     * {@link Database} a cui fa riferimento questa struttura dati.
     */
    private final Database database;

    /**
     * Valore di tipo booleano che identifica se questa struttura dati è utilizzabile.
     */
    private boolean usable = false;


    /**
     * Costruttore della Classe {@link DatabaseStructure}, si occupa d'inizializzare la struttura del {@link Database}.
     *
     * @param database {@link Database} a cui fa riferimento
     * @see #structureInitialize()
     */
    public DatabaseStructure(Database database) {
        this.database = database;
        this.structureInitialize();
    }

    /**
     * Metodo utilizzato per ottenere tutte le informazioni riguardanti la struttura SQL del {@link Database}.
     * <p>Il funzionamento si divide in:</p>
     * <br>
     * <h2 style="margin:0">
     * Ricezione Tabelle
     * </h2>
     * <p>
     * Tramite il metodo {@link Database#getStatement()} viene ottenuto lo {@link java.sql.Statement} relativo al {@link Database} in questione, grazie al quale viene interrogato il sistema di gestione con la query:<br>
     * {@code SHOW TABLES;}
     * </p>
     * <h2 style="margin:0">
     * Elaborazione Tabelle
     * </h2>
     * <p>
     * La precedente query farà in modo che il sistema di gestione risponda con una tabella contenente tutti i nomi delle varie tabelle contenute all'interno del {@link Database}.<br>
     * Per ogni riga della tabella di risposta, viene preparata un altra query:<br>
     * <code>DESCRIBE {@link Database#getName()}.[nome_tabella];></code>
     * </p>
     * <h2 style="margin:0">
     * Controllo Parametri
     * </h2>
     *
     * <p>Grazie alla seconda query, invece è invece possibile ottenere:</p>
     *     <ul style="margin-top:0px">
     *         <li>
     *             Nome della variabile
     *         </li>
     *         <li>
     *             Tipologia della variabile
     *         </li>
     *     </ul>
     *     Dalle informazioni ottenute è possible verificare se la tabella selezionata presenta le caratteristiche per poter ospitare i dati che si intendono salvare (tempo, temperatura).
     *
     * @see #isSimilar(String, String)
     */
    public void structureInitialize() {
        try (PreparedStatement prestmt_tables = this.database.getStatement().getConnection().prepareStatement("SHOW TABLES;")) {
            ResultSet tables = prestmt_tables.executeQuery();
            while (tables.next()) {
                String table_name = tables.getString(1);
                PreparedStatement prestmt_describe = this.database.getStatement().getConnection().prepareStatement("DESCRIBE " + this.database.getName() + "." + table_name + " ;");
                ResultSet describe = prestmt_describe.executeQuery();
                while (describe.next()) {
                    String field = describe.getString(1);
                    String type = describe.getString(2);
                    if (Objects.equals(type, "float") || Objects.equals(type, "double")) {
                        for (String default_field : this.defaultTypeTemperatureNames) {
                            if (this.isSimilar(field, default_field)) {
                                this.temperatureColumnName = field;
                                this.tableName = table_name;
                                break;
                            }
                        }
                    } else if (Objects.equals(type, "datetime")) {
                        this.timeColumnName = field;
                        this.tableName = table_name;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.usable = this.timeColumnName != null && this.temperatureColumnName != null;
    }

    /**
     * Metodo realizzato per generare una {@link Map} contenente:
     *     <table>
     *         <tr>
     *             <th>
     *                 Chiave
     *             </th>
     *             <th>
     *                 Valore
     *             </th>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code carattere}]:
     *             </td>
     *             <td>
     *                  [{@code numero_ripetizioni}]
     *             </td>
     *         </tr>
     *     </table><br>
     * es. <br>
     * Stringa presa in esame: <br>{@code Hello world!}
     *     <table>
     *         <tr>
     *             <th>
     *                 Chiave
     *             </th>
     *             <th>
     *                 Valore
     *             </th>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code h}]:
     *             </td>
     *             <td>
     *                  [{@code 1}]
     *             </td>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code e}]:
     *             </td>
     *             <td>
     *                  [{@code 1}]
     *             </td>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code l}]:
     *             </td>
     *             <td>
     *                  [{@code 3}]
     *             </td>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code o}]:
     *             </td>
     *             <td>
     *                  [{@code 2}]
     *             </td>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code w}]:
     *             </td>
     *             <td>
     *                  [{@code 1}]
     *             </td>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code r}]:
     *             </td>
     *             <td>
     *                  [{@code 1}]
     *             </td>
     *         </tr>
     *         <tr>
     *             <td>
     *                  [{@code d}]:
     *             </td>
     *             <td>
     *                  [{@code 1}]
     *             </td>
     *         </tr>
     *     </table>
     *
     * @param str Stringa con la quale generare la {@link Map} (verrà convertita tutta in minuscolo)
     * @return {@link Map}
     */
    private Map<Character, Integer> generateCharMap(String str) {
        Map<Character, Integer> map = new HashMap<>();
        Integer currentChar;
        for (char c : str.toCharArray()) {
            currentChar = map.get(c);
            if (currentChar == null) {
                map.put(c, 1);
            } else {
                map.put(c, currentChar + 1);
            }
        }
        return map;
    }

    /**
     * Metodo realizzato per comparare due stringhe prese in esame.
     *
     * @param str        prima stringa da prendere in esame
     * @param compareStr seconda stringa da prendere in esame
     * @return {@code true} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         nel caso in cui il numero di caratteri simili supera il parametro <b>{@code THRESHOLD}</b> espresso in percentuale riferito alla stringa più lunga
     *     </li>
     * </ul>
     * {@code false} <br>
     * <ul style="margin-top:0">
     *     <li>
     *         nel caso in cui il numero di caratteri simili non supera il parametro <b>{@code THRESHOLD}</b>
     *     </li>
     * </ul>
     */
    private boolean isSimilar(String str, String compareStr) {
        float THRESHOLD = 0.60f; //soglia che identifica il numero di caratteri per determinare se la stringa è simile o meno (60%)
        Map<Character, Integer> strMap = this.generateCharMap(str.toLowerCase());
        Map<Character, Integer> compareStrMap = this.generateCharMap(compareStr.toLowerCase());
        Set<Character> charSet = compareStrMap.keySet();
        int similar_chars = 0;
        int total_strChars = str.length();
        if (total_strChars < compareStrMap.size())
            total_strChars = compareStr.length();
        float thisThreshold;
        Iterator<Character> it = charSet.iterator();
        char currentChar;
        Integer currentCountStrMap;
        Integer currentCountCompareStrMap;
        while (it.hasNext()) {
            currentChar = it.next();
            currentCountStrMap = strMap.get(currentChar);
            if (currentCountStrMap != null) {
                currentCountCompareStrMap = compareStrMap.get(currentChar);
                if (currentCountCompareStrMap >= currentCountStrMap) {
                    similar_chars += currentCountStrMap;
                } else {
                    similar_chars += currentCountCompareStrMap;
                }
            }
        }
        thisThreshold = ((float) similar_chars) / ((float) total_strChars);
        return thisThreshold > THRESHOLD;
    }

    /**
     * Metodo utilizzato per indicare che il Database è utilizzabile.
     *
     * @return {@code true} | {@code false}
     */
    public boolean isUsable() {
        return this.usable;
    }

    /**
     * Metodo utilizzato per ottenere il nome della tabella in cui salvare i dati.
     *
     * @return {@link #tableName}
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Metodo utilizzato per ottenere il nome della colonna riferita alla temperatura
     *
     * @return {@link #temperatureColumnName}
     */
    public String getTemperatureColumnName() {
        return this.temperatureColumnName;
    }


    /**
     * Metodo utilizzato per ottenere il nome della colonna riferita al tempo
     *
     * @return {@link #timeColumnName}
     */

    public String getTimeColumnName() {
        return this.timeColumnName;
    }

    /**
     * Metodo utilizzato per ottenere i nomi relativi alle tabelle.
     * <p>viene utilizzato nel metodo {@link Database#insert()}.</p>
     * @return Stringa, struttura:
     * <code>({@link #timeColumnName}, {@link #temperatureColumnName}</code>
     */
    public String getTuple_ValuesName() {
        return " (" + this.timeColumnName + ", " + this.temperatureColumnName +") ";
    }
}

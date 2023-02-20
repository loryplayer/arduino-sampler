package test;

import arduino.Serial;
import com.fazecast.jSerialComm.SerialPort;
import database.Database;
import database.Driver;
import database.DriverList;
import file_managers.DataArchiver;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class main_test {
    public static void main(String[] args) throws IOException, InterruptedException {
//        test1();
//        test2_a();
//        test2_a_b();
//        test2_b();
//        test2_c();
        //test3();
        test4();
    }

    static void test1() {
        Driver driver = new Driver("localhost:3306", "root", "root");
        Database db = driver.getAllDatabases().getDatabaseByName("test");
        db.createStatement();
        Statement st = db.getStatement();
        ResultSet rs;
        System.out.println(db.getName());
        try {
            rs = st.executeQuery("select * from prova");
            while (rs.next()) {
                System.out.println(rs.getString(1));
                System.out.println(rs.getString(2) == null);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void test2_a() {
        System.out.println("Arduino Uno (COM8)".substring(0, "Arduino Uno (COM8)".indexOf("(COM") - 1));
        for (SerialPort port : SerialPort.getCommPorts()) {
            System.out.println(port.getPortDescription());
            System.out.println(port.getSystemPortPath());
            System.out.println(port.getPortLocation());
            System.out.println(port.getSystemPortName());
        }
//        System.out.println(Arrays.toString(Arrays.stream(SerialPort.getCommPorts()).toArray()));
    }

    static void test2_a_b() {
        Serial ard_serial = new Serial("COM8", "Arduino Uno");
        ard_serial.setComParameters(9600, 8, 1, 0);
        ard_serial.setComTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        System.out.println(ard_serial.open());
    }

    static void test2_b() {
        Serial ard_serial = new Serial();
        ard_serial.autoInitialize(null);
        System.out.println(ard_serial.getDeviceName() + " " + ard_serial.getPortName());
        ard_serial.setComParameters(9600, 8, 1, 0);
        ard_serial.setComTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        if (ard_serial.open())
            System.out.println("Port is open");
        else {
            System.out.println("Failed to open port");
            return;
        }

        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                //ard_serial.write((byte) 1);
                System.out.println("LED ON");
            } else {
                //ard_serial.write((byte) 0);
                System.out.println("LED OFF");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (ard_serial.close())
            System.out.println("Port is closed");
        else
            System.out.println("Failed to close port");
    }

    static void test2_c() throws IOException, InterruptedException {
        Serial ard_serial = new Serial();
        ard_serial.autoInitialize(null);
        System.out.println(ard_serial.getDeviceName() + " " + ard_serial.getPortName());
        ard_serial.setComParameters(9600, 8, 1, 0);
        ard_serial.setComTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        if (ard_serial.open())
            System.out.println("Port is open");
        else {
            System.out.println("Failed to open port");
            return;
        }
        ard_serial.readAndCollect();
        System.out.println(ard_serial.getDataCollectorList().getLastDataCollector().getData());

        if (ard_serial.close())
            System.out.println("Port is closed");
        else
            System.out.println("Failed to close port");
    }

    private static Map<Character, Integer> generateCharMap(String str) {
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

    private static boolean isSimilar(String str, String compareStr) {
        Map<Character, Integer> strMap = generateCharMap(str);
        Map<Character, Integer> compareStrMap = generateCharMap(compareStr);
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
        return thisThreshold > 0.60;
    }

    static void test3() {
        System.out.println(isSimilar("temperature", "temperat"));
    }


    static void test4() {
        DataArchiver a = new DataArchiver("src/test", "test.txt", new DriverList());
        Driver driver = new Driver("localhost:3306", "root", "root");
        Database db = new Database(driver, "db");
        a.save(driver);

        System.out.println(a.getElementsSaved());

    }
}

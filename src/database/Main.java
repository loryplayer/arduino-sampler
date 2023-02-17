package database;

public class Main {
    public static void main(String[] args) {
        Driver driver = new Driver("localhost:3306","root", "root");
        System.out.println(driver.testConnection());
    }

}
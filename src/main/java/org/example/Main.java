package org.example;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;


public class Main {
    public static Properties properties = new Properties();

    static {
        try{
            properties.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
        }  catch (Exception ox) {
            throw new RuntimeException(ox);
        }
    }
    public static void main(String[] args){
        try (Connection connection = createConnection()){
            if (connection != null){
                flywayMigrate (connection);
                performCrudOperations (connection);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static Connection createConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
    private static void flywayMigrate(Connection connection) throws Exception {
        Flyway.configure()
                .dataSource(properties.getProperty("db.url"), properties.getProperty("db.user"), properties.getProperty("db.password"))
                .load()
                .migrate();
    }
    private static void performCrudOperations(Connection connection) throws Exception{
        try {
            connection.setAutoCommit(false);
            insertNewProductAndCastomer(connection);
            creatOrdreForCustomer(connection);
            readListFiveOrders(connection);
            updatePriceAndStocks(connection);
            deleteTestRecords(connection);
            readListCategory(connection);

            connection.commit();// фиксируем изменения

        }catch (Exception y){
            connection.rollback();// откатываем изменения
            throw y;
        }
    }
    private static void insertNewProductAndCastomer(Connection connection) throws Exception{
        PreparedStatement ps = connection.prepareStatement("INSERT INTO product(description, price, quantity, category) VALUES (?, ?, ?, ?)");
        ps.setString(1, "Плеер");
        ps.setDouble(2, 3000.00);
        ps.setInt(3, 25);
        ps.setString(4,"Электроника");
        ps.executeUpdate();

        ps = connection.prepareStatement("INSERT INTO customer(first_name, last_name, phone, email) VALUES (?, ?, ?, ?)");
        ps.setString(1, "Наталия");
        ps.setString(2, "Бравая");
        ps.setString(3, "922 5478965");
        ps.setString(4,"hgf@ya.ru");
        ps.executeUpdate();

    }
    private static void creatOrdreForCustomer(Connection connection) throws Exception {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO orders(product_id, customer_id, quantity, status_id) VALUES (?, ?, ?, ?)");
        ps.setInt(1, 9);
        ps.setInt(2, 2);
        ps.setInt(3, 1);
        ps.setInt(4, 2);
        ps.executeUpdate();
    }
    private static void readListFiveOrders(Connection connection) throws Exception {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM orders ORDER by id DESC LIMIT 5");
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()){
            System.out.println("Заказ №" + resultSet.getInt("id") + ": Продукт = " + resultSet.getInt("product_id") + ", Клиент = " +  resultSet.getInt("customer_id"));
        }
    }
    private static void readListCategory(Connection connection) throws Exception {
        PreparedStatement ps = connection.prepareStatement("select * from product where category = 'Electronics'");
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()){
            System.out.println("Категория 'Electronics': Продукт: " + resultSet.getString("description") + ", цена = " + resultSet.getDouble("price") + ", количество = " +  resultSet.getInt("quantity"));
        }
        ps = connection.prepareStatement("select * from product where category = 'Clothing'");
        ResultSet resultSet1 = ps.executeQuery();
        while (resultSet1.next()){
            System.out.println("Категория 'Clothing': Продукт: " + resultSet1.getString("description") + ", цена = " + resultSet1.getDouble("price") + ", количество = " +  resultSet1.getInt("quantity"));
        }
    }

    private static void updatePriceAndStocks(Connection connection) throws Exception {
        PreparedStatement ps = connection.prepareStatement("UPDATE product SET price = price* 1.1, quantity = quantity-1 WHERE id = ?");
        ps.setInt(1,6);
        int rowsUpdated = ps.executeUpdate();
        System.out.println(rowsUpdated + " строки обновлены.");
    }
    private static void deleteTestRecords(Connection connection) throws Exception {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM product WHERE id = ?");
        ps.setInt(1, 1);
        int deletedRows = ps.executeUpdate();
        System.out.println(deletedRows + " записи удалены");
    }
}


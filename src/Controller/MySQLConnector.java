package Controller;

import Model.Appointment;
import Model.Client;
import Model.Customer;
import Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.*;

public class MySQLConnector {

    static volatile Connection connection = null;
    private static Statement statement = null;


    static void connect() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://wgudb.ucertify.com:3306/U06P7R", "U06P7R", "53688828396");
            statement = connection.createStatement();
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

        } catch (Exception e) {
            System.out.println("Error connecting to DB in MySQLConnector: " + e);
        }

    }


    public static ResultSet getResultSet(String sql) throws Exception {
        try {
            return statement.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("Error connecting to DB in MySQLConnector: " + e);
            return null;
        }

    }


    // Lambda expressions are used in the methods which call this code, allowing this
    // block of code to return multiple objects with custom classes.
    @SuppressWarnings("unchecked")
    public static <T> ObservableList<T> getObjectsFromDB(String _query, _DBtoObject<T> _convertible) {
        ObservableList<T> resultList = FXCollections.observableArrayList();

        try {
            Statement statement = connection.createStatement();
            for (ResultSet resultSet = statement.executeQuery(_query); resultSet.next(); ) {
                resultList.add((T) _convertible.createFromDB(resultSet));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return resultList;
    }


    public static Customer fetchCustomerFromDB(ResultSet _rs) {
        Customer newCustomer = null;

        try {
            int newID = _rs.getInt("customerId");
            String newName = _rs.getString("customerName");
            int newAddress = _rs.getInt("addressId");

            newCustomer = new Customer(newID, newName, newAddress);
            ObservableList<Appointment> appointments = fetchAllAppointmentsForCustomer(newCustomer);
            newCustomer.setAppointments(appointments);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return newCustomer;
    }


    public static Appointment fetchAppointmentFromDB(ResultSet _rs) {

        Appointment newAppointment = null;

        try {
            int appointmentId = _rs.getInt("appointmentId");
            int customerId = _rs.getInt("customerId");
            int userId = _rs.getInt("userId");
            String title = _rs.getString("title");
            String description = _rs.getString("description");
            String location = _rs.getString("location");
            String contact = _rs.getString("contact");
            String appointmentType = _rs.getString("type");
            String url = _rs.getString("url");
            Timestamp start = _rs.getObject("start", Timestamp.class);
            Timestamp end = _rs.getObject("end", Timestamp.class);


            ZonedDateTime _start = start.toLocalDateTime().atZone(ZoneId.of("UTC"));
            ZonedDateTime _end = end.toLocalDateTime().atZone(ZoneId.of("UTC"));

            newAppointment = new Appointment(appointmentId, customerId, userId, title, description,
                    location, contact, appointmentType, url, _start, _end);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return newAppointment;
    }


    public static Client fetchClientFromDB(ResultSet _rs) {

        Client newClient = null;

        try {
            int id = _rs.getInt("userId");
            String name = _rs.getString("userName");

            newClient = new Client(id, name);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newClient;
    }


    public static ObservableList<Client> fetchAllClients() {
        String query = "SELECT * FROM user;";

        // Using lambda here allows multiple types of objects to be returned using the same code
        return getObjectsFromDB(query, _rs -> fetchClientFromDB(_rs));
    }


    public static ObservableList<Customer> fetchAllCustomers() throws SQLException {
        String query = "SELECT * FROM customer;";

        // Using lambda here allows multiple types of objects to be returned using the same code
        return getObjectsFromDB(query, _rs -> fetchCustomerFromDB(_rs));
    }


    public static ObservableList<Appointment> fetchAllAppointments() throws SQLException {
        String query = String.format("Select * FROM appointment WHERE userId=%d;", User.getUserId());

        // Appointment objects are returned here via the same code as the function above
        return getObjectsFromDB(query, _rs -> fetchAppointmentFromDB(_rs));
    }


    public static ObservableList<Appointment> fetchAllAppointmentsForCustomer(Customer customer) throws SQLException {
        String query = String.format("Select * FROM appointment WHERE customerId = '%d';", customer.getCustomerId());

        return getObjectsFromDB(query, _rs -> fetchAppointmentFromDB(_rs));
    }


    public static ObservableList<Appointment> fetchAllAppointmentsForClient(Client client) {
        String query = String.format("Select * FROM appointment WHERE userId = '%d';", client.getUserId());

        return getObjectsFromDB(query, _rs -> fetchAppointmentFromDB(_rs));    }


    public static void deleteCustomerFromDB(int customerId) throws SQLException {
        String sql = String.format("DELETE FROM customer WHERE customerId = %d;", customerId);

        statement.execute(sql);
    }


    public static void deleteAppointmentFromDB(int appointmentId) throws SQLException {
        String sql = String.format("DELETE FROM appointment WHERE appointmentId = %d;", appointmentId);

        statement.execute(sql);
    }


    public static Statement getStatement() throws Exception {
        return statement;
    }

}

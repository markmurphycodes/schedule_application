package Controller;


import Model.Appointment;
import Model.Customer;
import Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SessionController {


    private static ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private static ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

    private static Locale locale;
    private static ZoneId zone;

    private static boolean updateAppointment = false;
    private static Appointment appointment;

    private SessionController() {
    }


    // Handle all login functions including logging
    public static void login() throws IOException {
        addLogEntry("User with ID #" + User.getUserId() + " has logged in at " + LocalDateTime.now());

    }


    public static void setLocale(Locale l) {
        SessionController.locale = l;
    }

    public static Locale getLocale() {
        return SessionController.locale;
    }

    public static void setZone(ZoneId o) {
        SessionController.zone = o;
    }

    public static ZoneId getZone() {
        return SessionController.zone;
    }


    public static void addLogEntry(String output) throws IOException {
        FileWriter out = null;
        File file = new File("userLog.txt");
        try {
            out = new FileWriter(file, true); // Open file in append mode
            out.write(output);
            out.write("\n");
        } finally {
            if (out != null)
                out.close();
        }
    }


    public static void addCustomerToList(int customerID, String customerName, int addressID) {
        Customer newCustomer = new Customer(customerID, customerName, addressID);
        customerList.add(newCustomer);
    }

    public static boolean deleteCustomer(Customer selectedCustomer) throws Exception {

        int customerId = selectedCustomer.getCustomerId();

        if (customerList.contains(selectedCustomer)) {
            customerList.remove(selectedCustomer);

            Statement statement = MySQLConnector.getStatement();

            String sql = String.format("DELETE FROM customer WHERE customerId = '%d';", customerId);
            statement.execute(sql);

            return true;
        }


        return false;
    }


    public static ObservableList<Customer> lookupCustomer(String customerName) {

        ObservableList<Customer> returnList = FXCollections.observableArrayList();

        for (int i = 0; i < customerList.size(); i++) {
            if (customerList.get(i).getCustomerName().toLowerCase().contains(customerName.toLowerCase()))
                returnList.add(customerList.get(i));
        }

        return returnList;
    }


    public static boolean deleteAppointment(Appointment selectedAppointment) {

        if (appointmentList.contains(selectedAppointment)) {
            appointmentList.remove(selectedAppointment);
            return true;
        }

        return false;
    }


    public static Appointment getAppointment(int apptId) {
        for (Appointment appointment : appointmentList) {
            if (appointment.getAppointmentId() == apptId)
                return appointment;
        }
        return null;
    }


    public static ObservableList<Customer> getCustomerList() {
        return SessionController.customerList;
    }


    public static Customer getCustomer(int custId) {
        for (Customer customer : customerList) {
            if (customer.getCustomerId() == custId)
                return customer;
        }
        return null;
    }


    public static ObservableList<Appointment> lookupAppointment(LocalDate appointmentDate, int userId) {

        ObservableList<Appointment> returnList = FXCollections.observableArrayList();

        if (appointmentList == null)
            return null;


        // Return a list of appointments which occur on the query date and which belong to the user
        for (int i = 0; i < Objects.requireNonNull(appointmentList).size(); i++) {
            if (appointmentDate.compareTo(appointmentList.get(i).getStart().toLocalDate()) == 0 && appointmentList.get(i).getUserId() == userId)
                returnList.add(appointmentList.get(i));
        }

        return returnList;
    }


    public static ObservableList<Appointment> lookupAppointment(Customer customer, int userId) {

        ObservableList<Appointment> returnList = FXCollections.observableArrayList();

        if (appointmentList == null)
            return null;


        // Return a list of appointments which which belong to the user and customer
        for (int i = 0; i < Objects.requireNonNull(appointmentList).size(); i++) {
            if (appointmentList.get(i).getCustomer() == customer.getCustomerId())
                returnList.add(appointmentList.get(i));
        }

        return returnList;
    }


    public static void addAppointment(Appointment newAppointment) {
        appointmentList.add(newAppointment);
    }


    public static void loadData() throws Exception {
        customerList = MySQLConnector.fetchAllCustomers();
        appointmentList = MySQLConnector.fetchAllAppointments();
    }


    public static void setUpdateAppointment(boolean b) {
        SessionController.updateAppointment = b;
    }


    public static boolean getUpdateAppointment() {
        return SessionController.updateAppointment;
    }


    public static void setAppointment(Appointment appointment) {
        SessionController.appointment = appointment;
    }


    public static Appointment getAppointment() {
        return SessionController.appointment;
    }

    public static List<Appointment> getAppointmentWithin15() {
        ZonedDateTime time = ZonedDateTime.now(zone);
        List<Appointment> apptList = new ArrayList<>();

        if (!appointmentList.isEmpty()) {
            for (Appointment appt : appointmentList) {
                long minutes = ChronoUnit.MINUTES.between(time, appt.getStart());
                if (minutes > 0 && minutes <= 15)
                    apptList.add(appt);
            }
        }
        return apptList;
    }


    public static ObservableList<Appointment> getAppointmentList() {
        return SessionController.appointmentList;
    }


}




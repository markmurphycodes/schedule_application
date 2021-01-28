package Controller;

import Model.Appointment;
import Model.Customer;
import Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.TextStyle;
import java.util.*;

public class Controller {

    @FXML
    public Button addCustomerButton, deleteCustomerButton, newAppointment, modifyCustomerButton;
    @FXML
    public Button getReport;
    @FXML
    public Label timeZoneLabel;

    @FXML
    public ToggleGroup weekOrMonth;
    @FXML
    public ToggleButton weekViewToggle, monthViewToggle;
    @FXML
    public Pane calendarPane;

    @FXML
    public ComboBox<String> selectReport;

    @FXML
    public javafx.scene.control.TextField customerSearchBar;

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<Customer, String> customerNameCol;
    @FXML
    private TableColumn<Appointment, String> appointmentName, appointmentTime, appointmentDate;

    private ObservableList<String> reportList;

    public void selectView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();

        if (weekViewToggle.isSelected()) {
            fxmlLoader.setLocation(getClass().getResource("/View/WeekView.fxml"));
            monthViewToggle.setSelected(false);
            weekViewToggle.setSelected(true);
        } else {
            fxmlLoader.setLocation(getClass().getResource("/View/MonthView.fxml"));
            monthViewToggle.setSelected(true);
            weekViewToggle.setSelected(false);
        }

        calendarPane.getChildren().setAll((Node) fxmlLoader.load());
    }


    public void populateAppointmentTable() {
        Customer customer = customerTable.getSelectionModel().getSelectedItem();
        ObservableList<Appointment> appointments = SessionController.lookupAppointment(customer, User.getUserId());

        appointmentTime.setCellValueFactory(new PropertyValueFactory<>("startTimeString"));
        appointmentName.setCellValueFactory(new PropertyValueFactory<>("title"));
        appointmentDate.setCellValueFactory(new PropertyValueFactory<>("startDateString"));


        appointmentTable.setItems(appointments);

        appointmentTable.setVisible(!appointments.isEmpty());

        appointmentTable.getColumns().setAll(appointmentDate, appointmentTime, appointmentName);
    }


    public void openAddCustomerWindow(Event event) throws Exception {

        Button button = (Button) event.getSource();
        Customer customer = customerTable.getSelectionModel().getSelectedItem();


        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/View/AddCustomer.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        if (button.getId().equals("modifyCustomerButton")) {
            AddCustomerController controller = fxmlLoader.getController();
            controller.initCustomer(customer);
        }

        Stage stage = new Stage();
        stage.setTitle("Add Customer");
        stage.setScene(scene);
        stage.show();
    }


    public void getReportClicked() throws SQLException {
        if (selectReport.getValue() != null) {
            switch (selectReport.getValue()) {
                case "All Appointments":
                    Reports.showSchedule();
                    break;
                case "List Types":
                    Reports.showNumberOfTypes();
                    break;
                case "List Customers":
                    Reports.showNumberOfCustomers();
                    break;
            }
        }
    }


    public void deleteCustomer() throws Exception {
        Customer customer = customerTable.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        if (customer.getAppointments().isEmpty()) {
            alert.setTitle("Delete customer?");
            alert.setHeaderText("Delete customer " + customer.getCustomerName() + "?");
            alert.setContentText("Is this ok?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                MySQLConnector.deleteCustomerFromDB(customer.getCustomerId());
                SessionController.deleteCustomer(customer);
            }
        } else {
            alert.setTitle("Illegal operation");
            alert.setHeaderText("Cannot delete customer.");
            alert.setContentText("This customer has appointments and cannot be deleted.");

            alert.showAndWait();
        }


    }


    @SuppressWarnings("unchecked")
    public void populateCustomerTable() throws SQLException {
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        customerTable.setItems(SessionController.getCustomerList());
        customerTable.getColumns().setAll(customerNameCol);
    }


    @SuppressWarnings("unchecked")
    public void searchCustomerTable(Event event) {
        ObservableList<Customer> tempProdList = SessionController.lookupCustomer(customerSearchBar.getText());

        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        customerTable.setItems(tempProdList);
        customerTable.getColumns().setAll(customerNameCol);
    }


    public void openNewAppointmentWindow() throws Exception {
        SessionController.setUpdateAppointment(false);

        if (CalendarUtility.getSelectedDate() != null) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/View/AddAppointment.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Add Appointment");
            stage.setScene(scene);
            stage.show();
        }
    }


    public void initialize() {
        try {
            SessionController.loadData();

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/View/MonthView.fxml"));
            calendarPane.getChildren().setAll((Node) fxmlLoader.load());

            timeZoneLabel.setText("All times in " + SessionController.getZone().getDisplayName(TextStyle.FULL_STANDALONE, SessionController.getLocale()));
            populateCustomerTable();

            reportList = FXCollections.observableArrayList();
            reportList.add("All Appointments");
            reportList.add("List Types");
            reportList.add("List Customers");
            selectReport.setItems(reportList);

            List<Appointment> appointmentList = SessionController.getAppointmentWithin15();
            if (!appointmentList.isEmpty()) {
                Alert appointmentSoon = new Alert(Alert.AlertType.INFORMATION);
                appointmentSoon.setHeaderText("Appointment within 15 minutes!");

                String bodyText = "";

                for (Appointment appt : appointmentList) {
                    bodyText += String.format("Time: %s\n\tLocation: %s\n", appt.getStartTimeString(), appt.getLocation());
                }
                appointmentSoon.setContentText(bodyText);

                appointmentSoon.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

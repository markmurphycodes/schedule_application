package Controller;

import Model.Appointment;
import Model.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.*;

public class Reports {

    private static ObservableList<Appointment> apptList = FXCollections.observableArrayList();

    private static void getClient() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Select User");
        GridPane pane = new GridPane();
        ComboBox<Client> clientList = new ComboBox<>();

        clientList.setItems(MySQLConnector.fetchAllClients());

        StringConverter<Client> sc = new StringConverter<Client>() {
            @Override
            public String toString(Client object) {
                if (object != null) { return object.getUserName(); }
                else { return null; }
            }

            @Override
            public Client fromString(String string) { return null; }
        };

        clientList.setConverter(sc);

        Label paneLabel = new Label("Which user would you like to view this report for?");

        pane.add(paneLabel, 0, 0);
        pane.add(clientList, 0, 1);

        Button view = new Button("View Report");
        view.setOnMouseClicked(event ->
        {
            Client newClient = clientList.getSelectionModel().getSelectedItem();
            apptList = MySQLConnector.fetchAllAppointmentsForClient(newClient);
            alert.close();
        });

        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        pane.add(view, 1, 1);

        alert.getDialogPane().setContent(pane);
        alert.getButtonTypes().setAll(cancel);
        alert.showAndWait();
    }


    public static void showSchedule() {

        getClient();

        TextArea textArea = new TextArea();
        ScrollPane scrollPane = new ScrollPane();
        Alert schedule = new Alert(Alert.AlertType.INFORMATION);
        String bodyText = "";

        schedule.setHeaderText("SCHEDULE");
        schedule.setTitle("Full Schedule");

        apptList.sort(Comparator.comparing(Appointment::getStart));

        for (Appointment appt : apptList) {
            bodyText += "Customer: " + SessionController.getCustomer(appt.getCustomer()).getCustomerName() + "\n";
            bodyText += "\tDate:       " + appt.getStartDateString() + "\n";
            bodyText += "\tStart Time: " + appt.getStartTimeString() + "\n";
            bodyText += "\tEnd Time:   " + appt.getEndTimeString() + "\n";
            bodyText += "\tLocation:   " + appt.getLocation() + "\n\n\n";
        }

        textArea.setText(bodyText);
        scrollPane.setContent(textArea);
        schedule.getDialogPane().setContent(scrollPane);
        schedule.showAndWait();

    }

    public static void showNumberOfTypes() {

        getClient();

        ScrollPane pane = new ScrollPane();
        DialogPane dialogPane = new DialogPane();
        Label label = new Label();
        Alert apptTypes = new Alert(Alert.AlertType.INFORMATION);
        String bodyText = "";

        apptTypes.setHeaderText("TYPES");
        apptTypes.setTitle("Appointments by Type");

        apptList.sort(Comparator.comparing(Appointment::getAppointmentType));
        Map<String, List<Appointment>> types = new HashMap<>();

        for (Appointment appt : apptList) {
            if (!types.containsKey(appt.getAppointmentType()))
                types.put(appt.getAppointmentType(), new ArrayList<>());


            if (types.containsKey(appt.getAppointmentType()))
                types.get(appt.getAppointmentType()).add(appt);

        }

        for (String type : types.keySet()) {
            bodyText += "Type: " + type + "\n";
            bodyText += "\tNumber of Appointments: " + types.get(type).size() + "\n";
        }

        label.setText(bodyText);
        pane.setContent(label);
        dialogPane.setContent(pane);
        apptTypes.getDialogPane().setContent(dialogPane);
        apptTypes.showAndWait();
    }


    public static void showNumberOfCustomers() throws SQLException {
        ScrollPane pane = new ScrollPane();
        DialogPane dialogPane = new DialogPane();
        Label label = new Label();
        Alert customerTypes = new Alert(Alert.AlertType.INFORMATION);
        String bodyText = "";

        customerTypes.setHeaderText("CUSTOMERS");
        customerTypes.setTitle("Appointments by Customer");

        ObservableList<Appointment> apptList = MySQLConnector.fetchAllAppointments();

        apptList.sort(Comparator.comparing(Appointment::getCustomer));
        Map<String, List<Appointment>> customers = new HashMap<>();

        for (Appointment appt : apptList) {
            if (!customers.containsKey(SessionController.getCustomer(appt.getCustomer()).getCustomerName()))
                customers.put(SessionController.getCustomer(appt.getCustomer()).getCustomerName(), new ArrayList<>());


            if (customers.containsKey(SessionController.getCustomer(appt.getCustomer()).getCustomerName()))
                customers.get(SessionController.getCustomer(appt.getCustomer()).getCustomerName()).add(appt);

        }

        for (String type : customers.keySet()) {
            bodyText += "Customer: " + type + "\n";
            bodyText += "\tNumber of Appointments: " + customers.get(type).size() + "\n";
        }

        label.setText(bodyText);
        pane.setContent(label);
        dialogPane.setContent(pane);
        customerTypes.getDialogPane().setContent(dialogPane);
        customerTypes.showAndWait();
    }

}

package Controller;

import Model.Appointment;
import Model.Customer;
import Model.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;

public class CalendarUtility {

    private static ZonedDateTime selectedDate = ZonedDateTime.now();
    private static Label currentLabel = null;
    private static ObservableList<Appointment> appointments;
    private static TitledPane selectedPane;
    private static Customer selectedCustomer;


    public static void populateAppointments(ObservableList<Appointment> appts, TitledPane box) {
        appointments = appts;

        ScrollPane pane = new ScrollPane();
        GridPane grid = new GridPane();
        int n = 0;
        for (Appointment appointment : appointments) {
            Label apptLabel = new Label();
            apptLabel.setText(appointment.getStartTimeString() + " " +
                    SessionController.getCustomer(appointment.getCustomer()).getCustomerName());
            apptLabel.setId(Integer.toString(appointment.getAppointmentId()));
            apptLabel.setTextFill(Paint.valueOf("Black"));

            /*
                Using lambda here allows for each appointment label to be associated with
                an onMouseClicked function in a readable and efficient manner.
             */
            apptLabel.setOnMouseClicked(event ->
            {
                try {
                    selectAppointment(apptLabel);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });

            grid.add(apptLabel, 0, n);
            n++;
        }
        pane.setContent(grid);
        box.setContent(pane);
    }


    public static void setSelectedPane(TitledPane pane) {
        selectedPane = pane;
    }

    public static TitledPane getSelectedPane() {
        return selectedPane;
    }


    // Set selected appointment and update label colors
    public static void selectAppointment(Label selectedLabel) throws SQLException {
        if (currentLabel != null)
            currentLabel.setTextFill(Paint.valueOf("Black"));

        selectedLabel.setTextFill(Paint.valueOf("Red"));
        Appointment appointment = SessionController.getAppointment(Integer.parseInt(selectedLabel.getId()));
        SessionController.setAppointment(appointment);
        setSelectedCustomer(SessionController.getCustomer(appointment.getCustomer()));
        setSelectedDate(appointment.getStart());

        Parent pane = selectedLabel.getParent();
        while (!(pane instanceof javafx.scene.control.TitledPane)) {
            pane = pane.getParent();
        }
        setSelectedPane((TitledPane) pane);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Appointment for " + selectedCustomer.getCustomerName());
        alert.setContentText("What would you like to do?");

        ButtonType view = new ButtonType("View");
        ButtonType modify = new ButtonType("Modify");
        ButtonType delete = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(view, modify, delete, cancel);
        Optional<ButtonType> result = alert.showAndWait();


        if (result.get() == view) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(CalendarUtility.class.getResource("/View/ViewAppointment.fxml"));

            try {
                Scene scene = new Scene(fxmlLoader.load());
                Stage stage = new Stage();

                stage.setTitle("View Appointment");
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result.get() == modify) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(CalendarUtility.class.getResource("/View/AddAppointment.fxml"));

            try {
                SessionController.setUpdateAppointment(true);

                Scene scene = new Scene(fxmlLoader.load());
                Stage stage = new Stage();

                stage.setTitle("Modify Appointment");
                stage.setScene(scene);
                stage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result.get() == delete) {
            MySQLConnector.deleteAppointmentFromDB(appointment.getAppointmentId());
            SessionController.deleteAppointment(appointment);
            appointments.remove(appointment);
            selectedCustomer.getAppointments().remove(appointment);
        }


        appointments = SessionController.lookupAppointment(getSelectedDate().toLocalDate(), User.getUserId());
        populateAppointments(appointments, (TitledPane) pane);

        currentLabel = selectedLabel;
    }


    public static void setSelectedDate(ZonedDateTime date) {
        selectedDate = date;
    }

    public static ZonedDateTime getSelectedDate() {
        return selectedDate;
    }

    public static void setSelectedCustomer(Customer customer) {
        selectedCustomer = customer;
    }


}

package Controller;

import Model.Appointment;
import Model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ViewAppointmentController {

    @FXML
    public Label customerName, appointmentTitle, appointmentDescription, appointmentLocation;
    @FXML
    public Label appointmentContact, appointmentType, appointmentUrl, appointmentStart, appointmentEnd;
    @FXML
    public Label addAppointmentLabel;
    @FXML
    public Button exitButton;

    public void exitButtonClicked() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }


    public void initialize() {
        Appointment appointment = SessionController.getAppointment();
        Customer customer = SessionController.getCustomer(appointment.getCustomer());

        addAppointmentLabel.setText(appointment.getStartDateString());
        customerName.setText(customer.getCustomerName());
        appointmentTitle.setText(appointment.getTitle());
        appointmentDescription.setText(appointment.getDescription());
        appointmentLocation.setText(appointment.getLocation());
        appointmentContact.setText(appointment.getContact());
        appointmentType.setText(appointment.getAppointmentType());
        appointmentUrl.setText(appointment.getUrl());
        appointmentStart.setText(appointment.getStartTimeString());
        appointmentEnd.setText(appointment.getEndTimeString());

    }

}

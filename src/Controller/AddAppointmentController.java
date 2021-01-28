package Controller;

import Model.Appointment;
import Model.Customer;
import Model.User;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import javax.swing.text.NumberFormatter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static Controller.CalendarUtility.populateAppointments;

public class AddAppointmentController {

    @FXML
    public ComboBox<Customer> selectCustomer = new ComboBox<>();
    @FXML
    public Button exitButton, saveChanges;
    @FXML
    public TextField titleField, descriptionField, locationField, contactField, typeField, urlField;
    @FXML
    public Spinner<Integer> startHour, startMinute, endHour, endMinute;
    @FXML
    public RadioButton startAM, startPM, endAM, endPM;
    @FXML
    public Label addAppointmentLabel;

    private ZonedDateTime start;
    private ZonedDateTime end;

    Customer selectedCustomer;
    String title;
    String description;
    String location;
    String contact;
    String appointmentType;
    String url;
    int customerId;


    public void populateComboBox() throws SQLException {

        selectCustomer.setItems(SessionController.getCustomerList());

        // Custom StringConverter to display customer names in ComboBox
        StringConverter<Customer> converter = new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                if (customer != null)
                    return customer.getCustomerName();
                else
                    return "";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        };
        selectCustomer.setConverter(converter);

    }


    public void exitButtonAction(Event event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }


    public ZonedDateTime getTime(Spinner<Integer> inputHour, Spinner<Integer> inputMinute, boolean isAM, ZonedDateTime time) {
        int hour, minute;

        hour = inputHour.getValue();
        minute = inputMinute.getValue();

        hour += 12;

        time = time.withMinute(minute);
        if (isAM)
            hour %= 12;

        if (hour == 24)
            hour = 12;

        time = time.withHour(hour);

        return time.withZoneSameLocal(SessionController.getZone());
    }

    /*
    There should be 3 cases which result in a true return value
        - the start time lies between the start and end of an appointment
        - the end time lies between the start and end of an appointment
        - the start time is before and the end time is after those of an appointment
     */
    public Appointment checkAppointmentOverlap(ZonedDateTime start, ZonedDateTime end, boolean existing) {
        ObservableList<Appointment> appointmentList = SessionController.getAppointmentList();
        for (Appointment appt : appointmentList) {
            if (!existing || appt.getAppointmentId() != SessionController.getAppointment().getAppointmentId()) {
                if (( start.isAfter(appt.getStart()) || start.isEqual(appt.getStart()) ) && ( start.isBefore(appt.getEnd()) || start.isEqual(appt.getEnd() )))
                    return appt;
                if (( end.isAfter(appt.getStart()) || end.isEqual(appt.getStart()) ) && ( end.isBefore(appt.getEnd()) || end.isEqual(appt.getEnd()) ))
                    return appt;
                if (( start.isBefore(appt.getStart()) || start.isEqual(appt.getStart()) ) && ( end.isAfter(appt.getEnd()) || end.isEqual(appt.getEnd()) ))
                    return appt;
            }
        }

        return null;
    }


    public void saveChanges(Event event) throws Exception {

        boolean validAppointment = true;

        selectedCustomer = selectCustomer.getValue();
        title = titleField.getText();
        description = descriptionField.getText();
        location = locationField.getText();
        contact = contactField.getText();
        appointmentType = typeField.getText();
        url = urlField.getText();

        start = getTime(startHour, startMinute, startAM.isSelected(), start);
        end = getTime(endHour, endMinute, endAM.isSelected(), end);
        LocalDateTime currentDate = LocalDateTime.now(); // Used for SQL timestamp

        LocalDateTime _start = start.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        LocalDateTime _end = end.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

        Statement statement = MySQLConnector.getStatement();
        Appointment appt = SessionController.getAppointment();
        Appointment overlap = checkAppointmentOverlap(start, end, SessionController.getUpdateAppointment());

        Alert alert = new Alert(Alert.AlertType.WARNING);

        if (start.isAfter(end)) {
            alert.setTitle("Invalid time");
            alert.setHeaderText("Start must be before end time");
            alert.setContentText("Please enter a valid appointment time.");
            validAppointment = false;
        } else if (start.isBefore(start.withHour(8).withMinute(0)) || end.isAfter(end.withHour(17).withMinute(0))) {
            alert.setTitle("Invalid time");
            alert.setHeaderText("Appointment must be during normal business hours (8 - 5 Local Time).");
            alert.setContentText("Please enter a valid appointment time.");
            validAppointment = false;
        } else if (overlap != null) {
            alert.setTitle("Invalid time");
            alert.setHeaderText("Appointment time not available");
            alert.setContentText("Conflict with appointment at " + overlap.getStartTimeString());
            validAppointment = false;
        }

        if (selectedCustomer == null) {
            alert.setTitle("No customer selected");
            alert.setHeaderText("No customer has been selected");
            alert.setContentText("Please select a customer.");
            validAppointment = false;
        } else {
            customerId = selectedCustomer.getCustomerId();
        }

        if (title == null || title.equals("")) {
            alert.setTitle("No title entered");
            alert.setHeaderText("No title has been entered");
            alert.setContentText("Please enter a title.");
            validAppointment = false;
        }
        if (location == null || location.equals("")) {
            alert.setTitle("No location entered");
            alert.setHeaderText("No location has been entered");
            alert.setContentText("Please enter a location.");
            validAppointment = false;
        }
        if (contact == null || contact.equals("")) {
            alert.setTitle("No contact entered");
            alert.setHeaderText("No contact has been entered");
            alert.setContentText("Please enter a contact.");
            validAppointment = false;
        }
        if (appointmentType == null || appointmentType.equals("")) {
            alert.setTitle("No type entered");
            alert.setHeaderText("No appointment type has been entered");
            alert.setContentText("Please enter an appointment type.");
            validAppointment = false;
        }

        if (validAppointment) {
            int userID = User.getUserId();
            String sql;

            if (SessionController.getUpdateAppointment()) {
                sql = String.format("UPDATE appointment SET customerId=%d, title='%s', description='%s', location='%s', contact='%s', " +
                                "type='%s', url='%s', start='%s', end='%s', lastUpdateBy='%s' WHERE appointmentId=%d;",
                        customerId, title, description, location, contact, appointmentType, url, _start, _end, userID, appt.getAppointmentId());

                statement.execute(sql);

                appt.setCustomer(customerId);
                appt.setTitle(title);
                appt.setDescription(description);
                appt.setLocation(location);
                appt.setContact(contact);
                appt.setAppointmentType(appointmentType);
                appt.setUrl(url);
                appt.setStart(start);
                appt.setEnd(end);
                appt.setDuration(Duration.between(start, end));
            } else {
                sql = String.format("INSERT INTO appointment (customerId, userId, title," +
                                "description, location, contact, type, url, start, end, createDate, createdBy, lastUpdateBy)" +
                                "VAlUES ('%d', '%d', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", customerId,
                        userID, title, description, location, contact, appointmentType, url, _start, _end, currentDate, userID, userID);

                statement.execute(sql);

                // Select appointment and get appointmentID, create Appointment object
                sql = String.format("SELECT * FROM appointment WHERE STRCMP(description, '%s') = 0;", description);
                ResultSet resultSet = statement.executeQuery(sql);
                resultSet.next();
                int appointmentId = resultSet.getInt("userId");
                Appointment tempAppointment = new Appointment(appointmentId, customerId, userID, title, description, location, contact,
                        appointmentType, url, start, end);

                SessionController.addAppointment(tempAppointment);

                selectedCustomer.addAppointment(tempAppointment);
            }

            ObservableList<Appointment> appointments = SessionController.lookupAppointment(start.toLocalDate(), User.getUserId());
            populateAppointments(appointments, CalendarUtility.getSelectedPane());


            exitButtonAction(event);
        } else {
            alert.showAndWait();
        }
    }


    // Forces the spinner to update and check values when typing
    public void updateSpinner(Event event) {
        Spinner<Integer> target = (Spinner<Integer>) event.getSource();
        
        target.increment(0);
        event.consume();
        target.getEditor().requestFocus();
        target.getEditor().end();
    }


    public void initAppointment(Appointment appt) throws Exception {

        selectedCustomer = SessionController.getCustomer(appt.getCustomer());
        title = appt.getTitle();
        description = appt.getDescription();
        location = appt.getLocation();
        contact = appt.getContact();
        appointmentType = appt.getAppointmentType();
        url = appt.getUrl();

        selectCustomer.setValue(selectedCustomer);

        titleField.setText(title);
        descriptionField.setText(description);
        locationField.setText(location);
        contactField.setText(contact);
        typeField.setText(appointmentType);
        urlField.setText(url);


    }


    public void initialize() throws Exception {

        populateComboBox();

        SpinnerValueFactory<Integer> startHourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
        SpinnerValueFactory<Integer> startMinuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        SpinnerValueFactory<Integer> endHourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
        SpinnerValueFactory<Integer> endMinuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        startHourFactory.setWrapAround(true);
        startMinuteFactory.setWrapAround(true);
        endHourFactory.setWrapAround(true);
        endMinuteFactory.setWrapAround(true);

        startHour.setValueFactory(startHourFactory);
        startMinute.setValueFactory(startMinuteFactory);

        endHour.setValueFactory(endHourFactory);
        endMinute.setValueFactory(endMinuteFactory);

        if (SessionController.getUpdateAppointment()) {
            Appointment appt = SessionController.getAppointment();
            ZonedDateTime zStart = appt.getStart().withZoneSameInstant(SessionController.getZone());
            ZonedDateTime zEnd = zStart.plus(appt.getDuration());

            if (zStart.getHour() % 12 == 0)
                startHour.getEditor().setText("12");
            else
                startHour.getEditor().setText(String.valueOf(zStart.getHour() % 12));

            startMinute.getEditor().setText(String.valueOf(zStart.getMinute()));
            startAM.setSelected(zStart.getHour() < 12);
            startPM.setSelected(!startAM.isSelected());

            start = appt.getStart();
            end = start.plus(appt.getDuration());

            startHour.increment(0);
            startMinute.increment(0);

            if (zEnd.getHour() % 12 == 0)
                endHour.getEditor().setText("12");
            else
                endHour.getEditor().setText(String.valueOf(zEnd.getHour() % 12));

            endMinute.getEditor().setText(String.valueOf(zEnd.getMinute()));
            endAM.setSelected(zEnd.getHour() < 12);
            endPM.setSelected(!endAM.isSelected());

            endHour.increment(0);
            endMinute.increment(0);

            initAppointment(appt);

            addAppointmentLabel.setText("Modify appointment for " + SessionController.getCustomer(appt.getCustomer()).getCustomerName());
        } else {
            start = end = CalendarUtility.getSelectedDate();
            addAppointmentLabel.setText("Add appointment on " + DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(start));
        }

    }

}

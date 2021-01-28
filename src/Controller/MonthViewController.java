package Controller;

import Model.Appointment;
import Model.User;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.ColorAdjust;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;

public class MonthViewController {
    @FXML
    public TitledPane box0, box1, box2, box3, box4, box5, box6, box7, box8, box9, box10,
            box11, box12, box13, box14, box15, box16, box17, box18, box19, box20, box21, box22,
            box23, box24, box25, box26, box27, box28, box29, box30, box31, box32, box33, box34;

    @FXML
    public Button previousMonth, nextMonth;

    TitledPane[] boxes;

    @FXML
    public Label monthLabel;

    @FXML
    public ColorAdjust selectedEffect, unselectedEffect;

    ZonedDateTime defaultDate = ZonedDateTime.now();
    ZonedDateTime currentDate;
    public ZonedDateTime populateAppointmentDate;


    public void populateCalendar(ZonedDateTime selectedDate) throws Exception {

        CalendarUtility.setSelectedDate(null);

        this.currentDate = selectedDate;
        Month month = selectedDate.getMonth();
        int year = selectedDate.getYear();

        ObservableList<Appointment> appointments;

        // Update label on calendar
        monthLabel.setText(month.toString() + " " + year);

        // Get the day of the week of the first day of the month
        DayOfWeek firstDayOfMonth = selectedDate.withDayOfMonth(1).getDayOfWeek();
        int firstBox = (firstDayOfMonth.getValue() % 7) + 1;

        // Clear previous labels on all boxes
        for (TitledPane box : boxes) {
            box.setText(" ");
            box.setEffect(unselectedEffect);
        }

        // Add numeric labels to calendar tiles, populate tiles with appointment entries
        for (int i = 0; i < month.length(selectedDate.toLocalDate().isLeapYear()); i++) {
            int day = i + 1;
            TitledPane box;
            if ((i + firstBox - 1) > 34) {
                box = boxes[i + firstBox - 8];
                box.setText(box.getText() + " / " + (day));
            } else {
                box = boxes[i + firstBox - 1];
                box.setText(Integer.toString(day));
            }

            populateAppointmentDate = currentDate.withDayOfMonth(day);

            appointments = SessionController.lookupAppointment(populateAppointmentDate.toLocalDate(), User.getUserId());

            if (appointments != null)
                CalendarUtility.populateAppointments(appointments, box);
        }
    }


    public void previousMonth() throws Exception {
        this.currentDate = currentDate.minusMonths(1);
        populateCalendar(currentDate);
    }


    public void nextMonth() throws Exception {
        this.currentDate = currentDate.plusMonths(1);
        populateCalendar(currentDate);
    }


    // Select calendar box with date indicated
    public void selectBox(Event event) {
        // Clear previous selection on all boxes
        for (TitledPane box : boxes) {
            box.setEffect(unselectedEffect);
        }

        CalendarUtility.setSelectedDate(null);

        // Set effect on selected box
        TitledPane selectedPane = (TitledPane) event.getSource();

        if (!selectedPane.getText().equals(" ")) {
            selectedPane.setEffect(selectedEffect);
            CalendarUtility.setSelectedDate(currentDate.withDayOfMonth(Integer.parseInt(selectedPane.getText())));
            CalendarUtility.setSelectedPane(selectedPane);
        }

    }


    public void initialize() throws Exception {
        boxes = new TitledPane[]{box0, box1, box2, box3, box4, box5, box6, box7,
                box8, box9, box10, box11, box12, box13, box14, box15, box16, box17, box18,
                box19, box20, box21, box22, box23, box24, box25, box26, box27, box28, box29,
                box30, box31, box32, box33, box34};


        populateCalendar(defaultDate);

        selectedEffect = new ColorAdjust(.2, .15, .1, .1);
        unselectedEffect = new ColorAdjust(0, 0, 0, 0);
    }

}

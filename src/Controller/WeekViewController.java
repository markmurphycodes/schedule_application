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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.Hashtable;

public class WeekViewController {

    ZonedDateTime defaultDate = ZonedDateTime.now();
    ZonedDateTime currentDate;

    private Dictionary<TitledPane, ZonedDateTime> dateDictionary;

    @FXML
    public Label weekLabel;
    @FXML
    public ColorAdjust selectedEffect, unselectedEffect;
    @FXML
    public Button previousWeek, nextWeek;
    @FXML
    public TitledPane sunday, monday, tuesday, wednesday, thursday, friday, saturday;
    public TitledPane[] boxes;


    /*
        Gets the correct days/days of the week for a given week
        Finds the appointments which belong to each day
        Populates these appointments within the GUI on the appropriate day
     */
    public void populateCalendar(ZonedDateTime selectedDate) {
        CalendarUtility.setSelectedDate(null);
        // Clear previous selection on all boxes
        for (TitledPane box : boxes) {
            box.setEffect(unselectedEffect);

            if (!dateDictionary.isEmpty())
                dateDictionary.remove(box);
        }


        int dayOfWeek = (selectedDate.getDayOfWeek().getValue() - 1) % 6;

        selectedDate = selectedDate.minusDays(dayOfWeek);
        this.currentDate = selectedDate;

        weekLabel.setText("Week of " + currentDate.getMonth().getValue() + "/"
                + currentDate.getDayOfMonth() + "/" + currentDate.getYear());

        for (int i = 0; i < 7; i++) {
            String day = selectedDate.getDayOfWeek().name();
            String display = String.format(day + "%" + (50 - day.length()) + "s" + selectedDate.format(DateTimeFormatter.ofPattern("MM/dd/yy")), "");
            boxes[i].setText(display);
            dateDictionary.put(boxes[i], selectedDate);


            ObservableList<Appointment> appointments = SessionController.lookupAppointment(selectedDate.toLocalDate(), User.getUserId());

            if (appointments != null)
                CalendarUtility.populateAppointments(appointments, boxes[i]);


            selectedDate = selectedDate.plusDays(1);
        }
    }


    public void selectBox(Event event) {
        CalendarUtility.setSelectedDate(null);
        // Clear previous selection on all boxes
        for (TitledPane box : boxes) {
            box.setEffect(unselectedEffect);
        }

        // Set effect on selected box
        TitledPane selectedPane = (TitledPane) event.getSource();
        selectedPane.setEffect(selectedEffect);

        // Update selected date and pane in CalendarUtility
        CalendarUtility.setSelectedDate(dateDictionary.get(selectedPane));
        CalendarUtility.setSelectedPane(selectedPane);

    }


    // Move the calendar view to the previous week
    public void previousWeek() {
        this.currentDate = currentDate.minusWeeks(1);
        populateCalendar(currentDate);
    }


    // Move the calendar view to the next week
    public void nextWeek() {
        this.currentDate = currentDate.plusWeeks(1);
        populateCalendar(currentDate);
    }


    public void initialize() {
        boxes = new TitledPane[]{sunday, monday, tuesday, wednesday, thursday, friday, saturday};

        dateDictionary = new Hashtable<>();

        populateCalendar(defaultDate);

        selectedEffect = new ColorAdjust(.2, .15, .1, .1);
        unselectedEffect = new ColorAdjust(0, 0, 0, 0);
    }
}

package Model;

import Controller.SessionController;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment
{

    private final int appointmentId;
    private int customer;
    private int userId;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String appointmentType;
    private String url;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private Duration duration;


    public Appointment(int appointmentId, int customer, int userId, String title, String location, ZonedDateTime start, ZonedDateTime end)
    {
        this.appointmentId = appointmentId;
        this.customer = customer;
        this.userId = userId;
        this.title = title;
        this.location = location;
        this.start = start;
        this.end = end;
        this.duration = Duration.between(this.start, this.end);

    }

    public Appointment(int appointmentId, int customerId, int userId, String title, String description,
                       String location, String contact, String appointmentType,
                       String url, ZonedDateTime start, ZonedDateTime end)
    {
        this.appointmentId = appointmentId;
        this.customer = customerId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.appointmentType = appointmentType;
        this.url = url;
        this.start = start;
        this.end = end;
        this.duration = Duration.between(this.start, this.end);

    }

    public Appointment(int appointmentId, int customerId, int userId, String title, String description,
                       String location, String contact, String appointmentType,
                       String url, ZonedDateTime start, Duration duration)
    {
        this.appointmentId = appointmentId;
        this.customer = customerId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.appointmentType = appointmentType;
        this.url = url;
        this.start = start;
        this.duration = duration;
        this.end = this.start.plus(duration);

    }

    public void setDuration(Duration d) { this.duration = d; }

    public Duration getDuration() { return this.duration; }

    public int getAppointmentId(){ return this.appointmentId; }

    public int getCustomer() {
        return customer;
    }

    public void setCustomer(int customer) {
        this.customer = customer;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public String getStartTimeString() { return this.start.withZoneSameInstant(SessionController.getZone()).format(DateTimeFormatter.ofPattern("hh:mm a")); }

    public String getStartDateString() { return this.start.withZoneSameInstant(SessionController.getZone()).format(DateTimeFormatter.ofPattern("MM/dd/yy")); }

    public String getEndTimeString() { return this.end.withZoneSameInstant(SessionController.getZone()).format(DateTimeFormatter.ofPattern("hh:mm a")); }
}

package Model;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;


public class Customer
{

    private final int customerId;
    private String customerName;
    private int addressId;

    private ObservableList<Appointment> appointments;


    public Customer(int newID, String newName, int addressId)
    {
        this.customerId = newID;
        this.customerName = newName;
        this.addressId = addressId;
        this.appointments = FXCollections.observableArrayList();
    }

    // Getters
    public String getCustomerName(){ return this.customerName; }

    public int getCustomerId(){ return this.customerId; }

    public int getAddressId() { return this.addressId; }

    public ObservableList<Appointment> getAppointments(){ return this.appointments; }


    // Setters
    public void setCustomerName(String newName){ this.customerName = newName; }

    public void setAddressId(int newAddress){ this.addressId = newAddress; }

    public void addAppointment(Appointment newAppt) { this.appointments.add(newAppt); }

    public void setAppointments(ObservableList<Appointment> appointments) { this.appointments = appointments; }

}

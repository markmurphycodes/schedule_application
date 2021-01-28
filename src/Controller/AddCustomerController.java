package Controller;

import Model.Customer;
import Model.User;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;


public class AddCustomerController {

    @FXML
    public TextField addCustomerName, addCustomerAddress, addCustomerAddress2;
    @FXML
    public TextField addCustomerCity, addCustomerZip, addCustomerCountry, addCustomerPhone;

    @FXML
    public Button saveChanges, exitButton;
    @FXML
    public Label headerLabel;

    private String customerName, customerAddress, customerAddress2, customerZip, customerCity;
    private String customerPhone, customerCountry;

    private boolean update = false;
    private Customer customer;


    public void exitButtonAction(Event event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }


    public void saveChanges(Event event) {
        boolean validCustomer = true;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Entry");
        alert.setContentText("Invalid entry, please enter valid information.");
        String alertText = "";

        // Read form values
        customerName = addCustomerName.getText();
        customerAddress = addCustomerAddress.getText();
        customerAddress2 = addCustomerAddress2.getText();
        customerCity = addCustomerCity.getText();
        customerZip = addCustomerZip.getText();
        customerCountry = addCustomerCountry.getText();
        customerPhone = addCustomerPhone.getText();

        if (customerZip.length() != 5 || !customerZip.matches("[0-9]+")) {
            alertText += "Zip code must be 5 digits.\n";
            validCustomer = false;
        } else { validCustomer = true; }

        if (customerName.isEmpty()) {
            alertText += "Must enter a customer name.\n";
            validCustomer = false;
        } else { validCustomer = true; }

        if (customerCity.isEmpty()) {
            alertText += "Must enter a city.\n";
            validCustomer = false;
        } else { validCustomer = true; }

        if (customerAddress.isEmpty()) {
            alertText += "Must enter an address value.\n";
            validCustomer = false;
        } else { validCustomer = true; }

        if (customerCountry.isEmpty()) {
            alertText += "Must enter a county.\n";
            validCustomer = false;
        } else { validCustomer = true; }

        if (customerPhone.isEmpty()) {
            alertText += "Must enter a valid phone number.\n";
            validCustomer = false;
        } else { validCustomer = true; }

        if (validCustomer) {
            try {

                int cityID = 0;
                Statement statement = MySQLConnector.getStatement();

                int userID = User.getUserId();

                int countryID = 1;
                LocalDateTime timeStamp = LocalDateTime.now();


                // Check whether country exists already and get countryId
                String sql = String.format("SELECT * FROM country WHERE STRCMP(country, '%s') = 0;", customerCountry);
                ResultSet countryResultSet = statement.executeQuery(sql);

                if (!countryResultSet.first()) // Country doesn't exist and needs to be added
                {
                    sql = String.format("INSERT INTO country (country, createDate, createdBy, lastUpdateBy)" +
                            "VALUES ('%s', '%s', '%s', '%s')", customerCountry, timeStamp, userID, userID);
                    statement.execute(sql);
                }

                sql = String.format("SELECT * FROM country WHERE STRCMP(country, '%s') = 0;", customerCountry);
                countryResultSet = statement.executeQuery(sql);
                countryResultSet.next();
                countryID = countryResultSet.getInt("countryId");


                // Check for the city in the database
                sql = String.format("SELECT * FROM city WHERE STRCMP(city, '%s') = 0;", customerCity);
                ResultSet resultSet = statement.executeQuery(sql);

                if (!resultSet.first())  // City is not present in database
                {
                    sql = String.format("INSERT INTO city (city, countryId, createDate, createdBy, lastUpdateBy) VALUES ('%s', '%s', '%s', '%s', '%s');",
                            customerCity, countryID, timeStamp, userID, userID);
                    statement.execute(sql);
                }


                // Find city in database and get cityID
                sql = String.format("SELECT cityId from city where STRCMP(city, '%s') = 0;", customerCity);
                resultSet = statement.executeQuery(sql);
                resultSet.next();
                cityID = resultSet.getInt("cityId");

                int addressID, customerID;

                // Add address to table or update address
                if (update) {
                    addressID = customer.getAddressId();
                    sql = String.format("UPDATE address SET address='%s', address2='%s', cityId='%d', postalCode='%s', phone='%s', createDate='%s', createdBy='%s', lastUpdateBy='%s' " +
                            "WHERE addressId='%d';", customerAddress, customerAddress2, cityID, customerZip, customerPhone, timeStamp, userID, userID, addressID);
                    statement.execute(sql);

                    customerID = customer.getCustomerId();
                    // Update customer in table
                    sql = String.format("UPDATE customer SET customerName='%s', addressId='%s', active='%d', createDate='%s', createdBy='%s', lastUpdateBy='%s' " +
                            "WHERE customerId='%d';", customerName, addressID, 0, timeStamp, userID, userID, customerID);
                    statement.execute(sql);

                    // Update customer object
                    customer.setCustomerName(customerName);

                } else {
                    sql = String.format("INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdateBy) " +
                                    "VALUES ('%s', '%s', '%d', '%s', '%s', '%s', '%s', '%s');", customerAddress, customerAddress2, cityID, customerZip,
                            customerPhone, timeStamp, userID, userID);

                    statement.execute(sql);

                    // Select the last address in the table and get addressID from it
                    resultSet = statement.executeQuery("SELECT * FROM address");
                    resultSet.last();
                    addressID = resultSet.getInt("addressId");

                    // Add customer to table
                    sql = String.format("INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdateBy) " +
                            "VALUES ('%s', '%s', '%d', '%s', '%s', '%s')", customerName, addressID, 0, timeStamp, userID, userID);
                    statement.execute(sql);

                    // Get customerID
                    sql = String.format("SELECT * FROM customer WHERE STRCMP(customerName, '%s') = 0;", customerName);
                    resultSet = statement.executeQuery(sql);
                    resultSet.last();
                    customerID = resultSet.getInt("customerId");

                    // Add customer to list
                    SessionController.addCustomerToList(customerID, customerName, addressID);

                }


                Stage stage = (Stage) saveChanges.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                e.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText(e.toString());
                a.showAndWait();
            }
        } else {
            alert.setContentText(alertText);
            alert.showAndWait();
        }

    }


    // Get selected customer from main scene if the update button is clicked
    public void initCustomer(Customer cust) throws Exception {
        customer = cust;
        headerLabel.setText("Modify Customer");
        update = true;

        Statement statement = MySQLConnector.getStatement();

        String sql = String.format("SELECT * FROM address WHERE addressId = %d;", cust.getAddressId());

        ResultSet resultSet = statement.executeQuery(sql);

        resultSet.absolute(1);

        customerName = cust.getCustomerName();
        customerAddress = resultSet.getString("address");
        customerAddress2 = resultSet.getString("address2");
        int cityId = resultSet.getInt("cityId");
        customerZip = resultSet.getString("postalCode");
        customerPhone = resultSet.getString("phone");

        sql = String.format("SELECT * FROM city WHERE cityId = %d", cityId);
        resultSet = statement.executeQuery(sql);
        resultSet.absolute(1);

        customerCity = resultSet.getString("city");
        int countryId = resultSet.getInt("countryId");


        sql = String.format("SELECT * FROM country WHERE countryId = %d", countryId);
        resultSet = statement.executeQuery(sql);
        resultSet.absolute(1);


        customerCountry = resultSet.getString("country");

        addCustomerName.setText(customerName);
        addCustomerAddress.setText(customerAddress);
        addCustomerAddress2.setText(customerAddress2);
        addCustomerCity.setText(customerCity);
        addCustomerZip.setText(customerZip);
        addCustomerCountry.setText(customerCountry);
        addCustomerPhone.setText(customerPhone);


    }


}
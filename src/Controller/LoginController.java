package Controller;

import Model.User;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;


public class LoginController implements Initializable {

    @FXML
    private javafx.scene.control.Button cancelButton, loginButton;

    @FXML
    private javafx.scene.control.Label errorText, timeZoneLabel;

    @FXML
    private javafx.scene.control.TextField usernameField;
    @FXML
    private javafx.scene.control.PasswordField passwordField;


    @FXML
    private Label welcomeLabel, loginLabel, usernameLabel, passwordLabel;
    String loginCancel, errorTextString, exit, exitApp, isOk;


    public void loginButtonClicked(Event event) throws Exception {
        String _username = usernameField.getText();
        String _password = passwordField.getText();

        errorText.setText("");

        try {
            validateLogin(_username, _password);
        } catch (Exception e) {
            errorText.setTextFill(Paint.valueOf("Red"));
            errorText.setText(errorTextString);
            e.printStackTrace();
        }

    }


    public void cancelButtonClicked(Event event) {
        errorText.setTextFill(Paint.valueOf("Yellow"));
        errorText.setText(loginCancel);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(exit);
        alert.setHeaderText(exitApp);
        alert.setContentText(isOk);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        } else
            errorText.setText("");

    }


    private void openWindow() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/View/MainWindow.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Home");
        stage.setScene(scene);
        stage.show();

        stage = (Stage) errorText.getScene().getWindow();
        stage.close();
    }


    private void validateLogin(String user, String pass) throws Exception {
        String sql = String.format("SELECT * FROM user WHERE (STRCMP(username, '%s') = 0 AND STRCMP(password, '%s') = 0);", user, pass);
        Statement statement = MySQLConnector.getStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        if (resultSet.first())  // Login is successful
        {
            errorText.setText("Login Successful! Welcome.");
            errorText.setTextFill(Paint.valueOf("Green"));

            User.setUserId(resultSet.getInt("userId"));

            SessionController.login();

            resultSet.absolute(1);
            resultSet.updateInt("active", 1);
            resultSet.updateRow();

            sql = String.format("UPDATE user SET active=0 WHERE userId<>%d;", User.getUserId());
            statement.execute(sql);

            openWindow();

        } else // Login has failed
        {
            throw new Exception("Invalid login");
        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Locale.setDefault(Locale.FRANCE);
        Locale locale = Locale.getDefault();
        ZoneId zone = ZoneId.systemDefault();

        // zone = ZoneId.of("Europe/Paris");

        SessionController.setZone(zone);
        SessionController.setLocale(locale);

        resources = ResourceBundle.getBundle("languages/login", locale);

        timeZoneLabel.setText("All times in " + zone.getDisplayName(TextStyle.FULL_STANDALONE, locale));


        String welcome = resources.getString("welcome");
        String login = resources.getString("login");
        String username = resources.getString("username");
        String password = resources.getString("password");
        String cancel = resources.getString("cancel");
        loginCancel = resources.getString("loginCancel");
        errorTextString = resources.getString("errorText");
        exit = resources.getString("exit");
        exitApp = resources.getString("exitApp");
        isOk = resources.getString("isOK");


        welcomeLabel.setText(welcome);
        loginLabel.setText(login);
        usernameLabel.setText(username);
        passwordLabel.setText(password);
        loginButton.setText(login);
        cancelButton.setText(cancel);
        Main.setStageTitle(welcome);

    }
}

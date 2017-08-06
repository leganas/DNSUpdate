package com.leganas.dnsupdate.WindowController;

import com.leganas.dnsupdate.Assets.Account;
import com.leganas.dnsupdate.Setting;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;



/**
 * Created by AndreyLS on 04.08.2017.
 */
public class LoginController {
    private Stage mainStage;

    @FXML
    TextField login;

    @FXML
    PasswordField password;

    public void click_login(ActionEvent actionEvent) {
        if (Setting.account == null) Setting.account = new Account();
        Setting.account.setLogin(login.getText());
        Setting.account.setPassword(password.getText());
        Setting.saveAccount();
        Setting.set_ip.fire();
        ((Stage)((Node)actionEvent.getSource()).getScene().getWindow()).close();
    }

    public void click_cancel(ActionEvent actionEvent) {
        Setting.loadAccount();
        ((Stage)((Node)actionEvent.getSource()).getScene().getWindow()).close();
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        if (Setting.account != null) login.setText(Setting.account.getLogin());
        if (Setting.account != null) password.setText(Setting.account.getPassword());
    }
}

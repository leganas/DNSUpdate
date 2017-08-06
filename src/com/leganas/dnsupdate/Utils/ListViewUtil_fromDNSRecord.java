package com.leganas.dnsupdate.Utils;

import com.leganas.dnsupdate.Assets.DNSRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by AndreyLS on 05.08.2017.
 */
public class ListViewUtil_fromDNSRecord {

    /**Преобразует ArrayList в ObservableList */
    public static ObservableList<DNSRecord> ArrayListDNSRecord_TO_ObservaleList(ArrayList<DNSRecord> arrayList){
        if (arrayList == null) return null;
        if (arrayList.size() == 0) return null;

        ObservableList<DNSRecord> result = FXCollections.observableArrayList();
        result.addAll(arrayList);
        return result;
    }

    public static void initListViewDNSRecord(ListView<DNSRecord> listView, ObservableList<DNSRecord> list){
        if (list == null) return;
        listView.setItems(list);
        listView.setTooltip(new Tooltip("Список активных DNSRecord записей"));
        listView.setOrientation(Orientation.VERTICAL);

        listView.setCellFactory(new Callback<ListView<DNSRecord>, ListCell<DNSRecord>>() {
            public ListCell<DNSRecord> call(ListView<DNSRecord> param) {
                try {
                    Parent parent = FXMLLoader.load(getClass().getResource("../resources/listcellDNSRecord.fxml"));
                    TextField id = (TextField) parent.lookup("#id");
                    TextField value = (TextField) parent.lookup("#value");
                    TextField set_value = (TextField) parent.lookup("#set_value");
                    CheckBox chbox = (CheckBox) parent.lookup("#chbox");
                    ObservableList<String> options =
                            FXCollections.observableArrayList(
                                    "Назначить конкретное значение",
                                    "Назначить текущий IP"
                            );
                    ComboBox<String> comboBox = (ComboBox<String>) parent.lookup("#comboBox");
                    comboBox.setItems(options);

                    final ListCell<DNSRecord> cell = new ListCell<DNSRecord>(){
                        @Override
                        protected void updateItem(DNSRecord item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                id.setText(item.getId());
                                value.setText(item.getValue());
                                chbox.setSelected(item.isFlag_to_update());
                                comboBox.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {

                                        if (comboBox.getValue().equals("Назначить текущий IP")) {
                                            item.setValue("@real_ip");
                                            value.setText("@real_ip");
                                            set_value.setVisible(false);
                                        } else {
                                            set_value.setVisible(true);
                                        }

                                    }
                                });
                                if (item.getValue().equals("@real_ip")){
                                    comboBox.setValue("Назначить текущий IP");
                                    set_value.setText("@real_ip");
                                    set_value.setVisible(false);
                                } else  {
                                    comboBox.setValue("Назначить конкретное значение");
                                    set_value.setText(item.getValue());
                                    set_value.setVisible(true);
                                }
                                set_value.setOnKeyReleased(new EventHandler<KeyEvent>() {
                                    @Override
                                    public void handle(KeyEvent event) {
                                        value.setText(set_value.getText());
                                        item.setValue(set_value.getText());
                                    }
                                });
                                this.setGraphic(parent);
                            }
                        }

                    };
                    return cell;
                } catch (IOException e) {
                    return null;
                }
            }
        });
    }
}

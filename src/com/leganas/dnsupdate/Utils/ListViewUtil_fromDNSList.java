package com.leganas.dnsupdate.Utils;

import com.leganas.dnsupdate.Assets.DNS;
import com.leganas.dnsupdate.WindowController.DNS_RecordController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by AndreyLS on 04.08.2017.
 */
public class ListViewUtil_fromDNSList{

    /**Преобразует ArrayList в ObservableList */
    public static ObservableList<DNS> ArrayListDNS_TO_ObservaleList(ArrayList<DNS> arrayList){
        if (arrayList == null) return null;
        if (arrayList.size() == 0) return null;

        ObservableList<DNS> result = FXCollections.observableArrayList();
        result.addAll(arrayList);
     return result;
    }

    public static void initListViewDNS(ListView<DNS> listView, ObservableList<DNS> list, DNS_RecordController callback){
        if (list == null) return;
        listView.setItems(list);
        listView.setLayoutX(10);
        listView.setLayoutY(10);
        listView.setCursor(Cursor.OPEN_HAND);
        final DropShadow effect=new DropShadow();
        effect.setOffsetX(10);
        effect.setOffsetY(10);
        listView.setEffect(effect);
        listView.setStyle("-fx-border-width:3pt; -fx-font:bold 10pt Georgia;");
        // -fx-border-color:navy
        listView.setPrefSize(200, 512);
        listView.setTooltip(new Tooltip("Список активных DNS записей"));
        listView.setOrientation(Orientation.VERTICAL);

        listView.setCellFactory(new Callback<ListView<DNS>, ListCell<DNS>>() {
            public ListCell<DNS> call(ListView<DNS> param) {
                try {
                    Parent parent = FXMLLoader.load(getClass().getResource("../resources/listcellDNS.fxml"));
                    Button btn = (Button) parent.lookup("#dns_name");

                    btn.setEffect(effect);
                    btn.setStyle("-fx-background-color:#66ccff;");
                    btn.setPrefSize(170, 50);
                    btn.setWrapText(true);

                    CheckBox chbox = (CheckBox) parent.lookup("#chbox");

                    final ListCell<DNS> cell = new ListCell<DNS>(){
                        @Override
                        protected void updateItem(DNS item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                btn.setText(item.getName());
                                btn.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        System.out.println("Click to " + item.getName());
                                        callback.initRecordListFromDNS(item);
                                    }
                                });
                                try {
                                    Tooltip tt = new Tooltip();
                                    Parent ttP = FXMLLoader.load(getClass().getResource("../resources/tooltip.fxml"));
                                    tt.setGraphic(ttP);

                                    Label lab = (Label) ttP.lookup("#labelTT");
                                    lab.setText(item.getName());
                                    btn.setTooltip(tt);
                                } catch (IOException e) {
                                }
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

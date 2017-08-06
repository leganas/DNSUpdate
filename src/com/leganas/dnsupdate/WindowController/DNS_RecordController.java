package com.leganas.dnsupdate.WindowController;

import com.leganas.dnsupdate.Assets.DNS;
import com.leganas.dnsupdate.Assets.DNSRecord;
import com.leganas.dnsupdate.Setting;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import static com.leganas.dnsupdate.Setting.loadDNSList;
import static com.leganas.dnsupdate.Setting.saveDNSList;
import static com.leganas.dnsupdate.Utils.ListViewUtil_fromDNSList.ArrayListDNS_TO_ObservaleList;
import static com.leganas.dnsupdate.Utils.ListViewUtil_fromDNSList.initListViewDNS;
import static com.leganas.dnsupdate.Utils.ListViewUtil_fromDNSRecord.ArrayListDNSRecord_TO_ObservaleList;
import static com.leganas.dnsupdate.Utils.ListViewUtil_fromDNSRecord.initListViewDNSRecord;

/**
 * Created by AndreyLS on 04.08.2017.
 */
public class DNS_RecordController {

    private Stage mainStage;

    public void click_cancel(ActionEvent actionEvent) {
        loadDNSList();
        ((Stage)((Node)actionEvent.getSource()).getScene().getWindow()).close();
    }

    public void click_save(ActionEvent actionEvent) {
        saveDNSList();
        ((Stage)((Node)actionEvent.getSource()).getScene().getWindow()).close();
    }

    public void click_help(ActionEvent actionEvent) {
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        initStage();
    }

    @FXML
    ListView<DNS> list_dns_view;
    ObservableList<DNS> listDNS;

    @FXML
    ListView<DNSRecord> list_record_view;
    ObservableList<DNSRecord> listRecord;


    public void initStage(){
        initDNSList();
        if (Setting.dnsList.getList().size() > 0) initRecordListFromDNS(Setting.dnsList.getList().get(0));
    }

    public void initDNSList(){
        listDNS = ArrayListDNS_TO_ObservaleList(Setting.dnsList.getList());
        //Parent root = mainStage.getScene().getRoot();
        initListViewDNS(list_dns_view,listDNS, this);
//        list_dns_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DNS>() {
//            @Override
//            public void changed(ObservableValue<? extends DNS> observable, DNS oldValue, DNS newValue) {
//                System.out.println("Click " + newValue.getName());
//            }
//        });
    }

    public void initRecordListFromDNS(DNS item){
        listRecord = ArrayListDNSRecord_TO_ObservaleList(item.getDnsRecords());
        initListViewDNSRecord(list_record_view,listRecord);
        list_record_view.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DNSRecord>() {
            @Override
            public void changed(ObservableValue<? extends DNSRecord> observable, DNSRecord oldValue, DNSRecord newValue) {
                System.out.println("Click " + newValue.getHostName());
            }
        });
    }

    public void click_dnsName(ActionEvent actionEvent) {
        // Это не работает т.к. мы заменили обработчик
        Object bt = actionEvent.getSource();

        if (!(bt instanceof Button)) {return;} // если событие вызвала не кнопка то нахуй

        /*        Node nod = (Node) actionEvent.getSource();
        Stage stage = (Stage) nod.getScene().getWindow();
        stage.setTitle(((Button) bt).getText());*/

        ((Button) bt).setText("Click");
    }
}

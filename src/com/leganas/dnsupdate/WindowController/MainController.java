package com.leganas.dnsupdate.WindowController;

import com.leganas.dnsupdate.Assets.DNS;
import com.leganas.dnsupdate.Assets.DNSList;
import com.leganas.dnsupdate.Assets.DNSRecord;
import com.leganas.dnsupdate.ServerServiceWindow;
import com.leganas.dnsupdate.Setting;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.leganas.dnsupdate.ServerServiceWindow.IPAddres;
import static com.leganas.dnsupdate.ServerServiceWindow.status;

public class MainController {
    Stage mainStage;


    @FXML
    Button bt_setip;

    @FXML
    ProgressBar prbar;

    @FXML
    Label ip;

    @FXML
    WebView webView;

    WebEngine webEngine;



    public DNSList currentDNSList = new DNSList();
    int currentDNS = 0;

    ServerServiceWindow.Status lastStatus;

    /**Счётчик текущей записи для редактирования*/
    int cR = 0;
    /**Счётчик количества сработок завершений загрузок страницы (какого то хуя всегда по разному показывает)*/
    int reload_count = 0;

    /**Обработчик кнопки обновить IP*/
    public void click(ActionEvent actionEvent) {
        Parent root = mainStage.getScene().getRoot();
        status = ServerServiceWindow.Status.login_request;
        lastStatus = status;
        cR = 0;

        Setting.loadDNSList();


//        SplitPane splitPane = (SplitPane) root.lookup("#sp");
//        ObservableList<Node> nodes = splitPane.getItems();
//        AnchorPane anchorPane = (AnchorPane) nodes.get(1);
//        webView = (WebView) anchorPane.lookup("#web_view");

        webView.setVisible(false);
        java.net.CookieHandler.setDefault(new java.net.CookieManager());
        webEngine = webView.getEngine();
        webEngine.load("https://user.hoster.by/");

        prbar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
        final boolean[] edit = {false};

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            succeeded_run();
                        }
                    }
                });
    }

    /**Индикатор необходимости редактирования*/
    boolean[] edit = {false};

    /**Метод отрабатывающий последовательность действий после каждой полностью загруженной сраницы*/
    private void succeeded_run() {
        reload_count++;
//        System.out.println("Загрезка страницы завершена, текущий статус : " + webEngine.getLocation());
//        System.out.println("Всего было сработок : " + reload_count + " status : " + status + " : ");
        // Если страница загрузилась то
        if (status == ServerServiceWindow.Status.login_request) {
              status = ServerServiceWindow.Status.procces;
              reload_count=0;
              login();
        } else
        if (status == ServerServiceWindow.Status.forward_to_dnslist){
            if (webEngine.getLocation().equals("https://user.hoster.by/services/")) {
                status = ServerServiceWindow.Status.procces;
                reload_count=0;
                forward_to_DNSList();
            } else {
                // вывести ошибку
                System.out.println("Ошибка авторизации : " + webEngine.getLocation());
                status = ServerServiceWindow.Status.finish;
            }
        } else
        if (status == ServerServiceWindow.Status.dnslist_request) {
            status = ServerServiceWindow.Status.procces;
            reload_count=0;
            currentDNSList.setList(getDNSList());
            status = ServerServiceWindow.Status.run_dns_editor_request;
        }

        // Эта часть выполняется всегда (но сюда мы попадаем либо после
        // выполнения всех этапов предыдущих, либо при переборе списка dnslist)
        if (status == ServerServiceWindow.Status.run_dns_editor_request) {
            status = ServerServiceWindow.Status.procces;
            reload_count=0;

            forward_to_edit(currentDNS);
        } else
        if (status == ServerServiceWindow.Status.getRecords){
            status = ServerServiceWindow.Status.procces;
            reload_count = 0;
            // получаем список записей созданых на текущем DNS имени
            ArrayList<DNSRecord> records = getRecords();
            if (records != null) {
                currentDNSList.getList().get(currentDNS).setDnsRecords(records);
                edit[0] = checkList(Setting.dnsList, currentDNSList);
                status = ServerServiceWindow.Status.editdns;
            }
        }

        if (status == ServerServiceWindow.Status.editdns) {
            status = ServerServiceWindow.Status.procces;
            reload_count = 0;
            // запускаем процесс изменения записи
            // в нутри метода поверим помечены ли они как подлежащие проверке(изменению)
            if (edit[0] == true) {editRecord(); } else {status = ServerServiceWindow.Status.last;}
        }

        if (status == ServerServiceWindow.Status.last) {
            int lendnslist = currentDNSList.getList().size();
            if (currentDNS < lendnslist-1) {
                reload_count = 0;
                currentDNS++;
                System.out.println("Переходим к обработке следующего DNS имени");
                forward_to_DNSList(); // форвард на страницу списка DNS (типа обрабатываем следующее DNS имя)
                status = ServerServiceWindow.Status.run_dns_editor_request;
            } else status = ServerServiceWindow.Status.finish;

        }

        if (status == ServerServiceWindow.Status.finish) {
            if (lastStatus != status)  System.out.println("Обработка завершена");
            lastStatus = status;
            reload_count = 0;
        }
    }

    /**Сравнивает сохранённые настройки и текущий список
     * если они совпадают по названиям элементов но разные по значениями
     * то запускает процедуру редактирования текущих значений
     *
     * если же списки разные , значит была изменена структура записей на сайте
     * настройки сбрасываются и перезапысываются
     * (нужно заново выставлять флаги и необходимые действия)*/
    private boolean checkList(DNSList dnsList, DNSList currentDNSList) {
        // индиктор необходимости записи новой конфигурации
        boolean save = false;
        // индикатор необходимости обновления текущего списка записей на сервере
        boolean ret = false;
        if (dnsList == null) {
            dnsList = new DNSList();

        }
        if (dnsList.getList() == null) {
            dnsList.setList(new ArrayList<>());
        }

        if (currentDNSList.getList().size() == dnsList.getList().size()) {
            for (int i=0;i<dnsList.getList().size();i++){
                currentDNSList.getList().get(i).setFlag_to_update(dnsList.getList().get(i).getFlag_to_update());
                if (currentDNSList.getList().get(i).getDnsRecords().size() == dnsList.getList().get(i).getDnsRecords().size()) {
                    int size = currentDNSList.getList().get(i).getDnsRecords().size();
                    for (int z=0; z < size;z++) {
                        String a, b, c;
                        boolean flag_to_update;
                        a = dnsList.getList().get(i).getDnsRecords().get(z).getId();
                        b = dnsList.getList().get(i).getDnsRecords().get(z).getHostName();
                        c = dnsList.getList().get(i).getDnsRecords().get(z).getValue();
                        flag_to_update = dnsList.getList().get(i).getDnsRecords().get(z).isFlag_to_update();
                        String a1, b1, c1;
                        a1 = currentDNSList.getList().get(i).getDnsRecords().get(z).getId();
                        b1 = currentDNSList.getList().get(i).getDnsRecords().get(z).getHostName();
                        c1 = currentDNSList.getList().get(i).getDnsRecords().get(z).getValue();
                        currentDNSList.getList().get(i).getDnsRecords().get(z).setFlag_to_update(flag_to_update);
                        if (a.equals(a1) && b.equals(b1)) {
                            // Если одинаковые то проверяем , нужно ли поменять в текущих на реальный IP
                            String setValue = "";
                            if (c.equals("@real_ip")) {
                                String ip = getCurrentIP();
                                if (currentDNSList.getList().get(i).getDnsRecords().get(z).getValue() != ip) {
                                    setValue = ip;
                                }
                            } else setValue = dnsList.getList().get(i).getDnsRecords().get(z).getValue();

                            if (!currentDNSList.getList().get(i).getDnsRecords().get(z).getValue().equals(setValue)) {
                                currentDNSList.getList().get(i).getDnsRecords().get(z).setValue(setValue);

                                if ((flag_to_update == true) && (dnsList.getList().get(i).getFlag_to_update())) ret = true;
                            }
                        } else
                        {
                            // Если нет то делаем так что бы были одинаковые
                            dnsList.getList().get(i).setDnsRecords(currentDNSList.getList().get(i).getDnsRecords());
                            save = true;
                            ret = false;
                        }
                    }
                } else {
                    dnsList.setTdName(currentDNSList.tdName);
                    dnsList.setList(currentDNSList.list);
                    save = true;
                }
            }
        } else {
            dnsList.setTdName(currentDNSList.tdName);
            dnsList.setList(currentDNSList.list);
            save = true;
        }
        if (save == true) {
            Setting.saveDNSList();
            System.out.println("Перезаписана конфигурация");
        }
        if (ret == true) {System.out.println("Требуются изменения на сервере");} else {
            status = ServerServiceWindow.Status.last;
            // Тут вопрос вот нужно ли запускать обработку заново
//            succeeded_run();
        }
        return ret;
    }
    /**Cчётчик повторов getRecords*/
    int repeat = 0;


    /**Получаем записи,
     * пока корректно обрабатываются A, CNAME записи
     * остальные тоже будут работать но с ньюансами с точкой в конце*/
    private ArrayList<DNSRecord> getRecords() {
        ArrayList<DNSRecord> records = new ArrayList<>();
        JSObject jsRecordList = (JSObject) webEngine.executeScript("document.getElementsByClassName('dnse_group_record')");
        int len = Integer.parseInt(jsRecordList.getMember("length").toString());
        if (len == 4) {
            System.out.println("Не нашли ни одной записи, их либо нет либо ложная срботка");
            // ждём 1 секунду и повторим поиск записей (и так 3 раза)
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    status = ServerServiceWindow.Status.getRecords;
                    succeeded_run();
                }
            });
            repeat++;
            // Пробуем прочитать 3 раза ,через каждую секунду , если не успели загрузится записи ,
            // значит их там тупо нет и это пустой DNS идём бальше и создаём пустой массив
            // что в свою очередт повлечёт перезапись настроек
            // криво как то но хуля делать , я хз как точно определить когда последняя сработка
            // завершения загрузки всей страницы со всеми данными
            if (repeat < 3) return null;
        }
        repeat = 0;
        // почему то находит +4 , в рот ни ебу почему, в браузере этот скрипт находит как положено
        // те 4 что в конце нам не нужны отсееваем, надеюсь дизайн не изменится :)
        for (int i=0;i<len-4;i++) {
            DNSRecord dnsRecord = new DNSRecord();
            String txt = ((JSObject)jsRecordList.getSlot(i)).getMember("innerHTML").toString();
            String txt2[] = txt.split("\"");
            dnsRecord.setId(txt2[21]);
            String hostname = txt2[13].substring(0, txt2[13].length()-1);
            dnsRecord.setHostName(hostname);
            String value;
            if (dnsRecord.getId().indexOf("CNAME") >-1) {
                value = txt2[19].substring(0, txt2[19].length()-1);
            } else {value = txt2[19].substring(0, txt2[19].length());}
            dnsRecord.setValue(value);
            System.out.println("Обнаружены записи : " + dnsRecord.getHostName() + " : " + dnsRecord.getValue());
            records.add(dnsRecord);
        }
        return records;
    }

    /**Запуск процедуры ввода логина и пароля*/
    private void login(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // заполняем логин пароль
                // по простому разу через JavaScript

//                webEngine.executeScript("document.getElementsByClassName('input_text')[0].value='leganas@gmail.com'");
//                webEngine.executeScript("document.getElementsByClassName('input_text')[1].value='kj4cuetd'");
//                webEngine.executeScript("document.getElementById(\"userlogin\").submit()");

                // по сложному :) получаем объекты JavaScript и заполняем поля и выполняем методы при помощи Java
                // плюс в том что мы можем использовать данные выполнения JavaScript в коде на Java

                // Ищем все элементы <INPUT> получаем на выходе HTMLCollection
                JSObject jsObject = (JSObject) webEngine.executeScript("document.getElementsByClassName('input_text')");
                // получаем 0й элемент коллекции это наше поле логина
                ((JSObject) jsObject.getSlot(0)).setMember("value","leganas@gmail.com");
                // получаем 2й элемент коллекции это наш пароль
                ((JSObject) jsObject.getSlot(1)).setMember("value","kj4cuetd");
                // Находим форму и выполняем JavaScript функцию submit()
                JSObject form = (JSObject) webEngine.executeScript("document.getElementById(\"userlogin\")");
                form.call("submit");
                status = ServerServiceWindow.Status.forward_to_dnslist;
            }
        });
    }

    /**Перенаправления на страницу со споском ваших DNS имён*/
    private void forward_to_DNSList(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webEngine.load("https://user.hoster.by/services/domains/");
                status = ServerServiceWindow.Status.dnslist_request;
            }

        });
    }

    /**Получаем список DNS имён активных*/
    private ArrayList<DNS> getDNSList() {
        ArrayList<DNS> list = new ArrayList<>();
        JSObject jsDNSList = (JSObject) webEngine.executeScript("document.getElementsByClassName('status_ok')");
        int len = Integer.parseInt(jsDNSList.getMember("length").toString());

        // Проходимся по всем DNS именам, (-1 так как 1й элемент выдаётся лишний это не строка таблицы  а левый элемент дизайна с похожим названием)
        for(int i=0;i<len-1;i++){
            JSObject td = (JSObject) jsDNSList.getSlot(i);
            String name = td.getMember("outerText").toString();
            String names[] = name.split("\\t");
            String dnsname = names[0];
            System.out.println("Обнаружены активные DNS имена : " + dnsname);

            //выполняем поиск в нутри таблицы на элемент хранящий ссылку Расщиренный редактор днс
            JSObject urls = (JSObject) td.call("getElementsByClassName","edit dns");
            String url = urls.getSlot(1).toString();
            list.add(new DNS(dnsname,url));
        }
        return list;
    }

    /**Перенаправляет на страницу редактирования текущего DNS имени*/
    private void forward_to_edit(int currentDNS){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Переходим на страницу редактирования DNS : " + currentDNSList.getList().get(currentDNS).getName());
                webEngine.load(currentDNSList.getList().get(currentDNS).getUrl_dns_editor());
                status = ServerServiceWindow.Status.getRecords;
            }
        });
    }

    /**Редактирует запись*/
    private void editRecord(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String dopS = "";
                System.out.println("cR = " + cR);
                // изза припиженой автозамены на Hoster.by прихоится добавлять в конце точку
                // надо бы и для других типов записей такое предумотреть , но я их не сипользую по сему оставлю это
                // для того кому это нужно будет (ну или отпишитесь сделаю)
                if (currentDNSList.getList().get(currentDNS).getDnsRecords().get(cR).getId().indexOf("CNAME_") >-1) {dopS = ".";}
                if (currentDNSList.getList().get(currentDNS).getDnsRecords().get(cR).isFlag_to_update()) {
                    webEngine.executeScript("document.getElementsByClassName(\"dnse_manage_button edit\")[" + cR + "].onclick();");
                    webEngine.executeScript("document.getElementById(\"input_content\").value = \"" + currentDNSList.getList().get(currentDNS).getDnsRecords().get(cR).getValue() + dopS + "\";");
                    webEngine.executeScript("document.getElementById(\"form_submit_button\").click();");
                }
                if (cR < currentDNSList.getList().get(currentDNS).getDnsRecords().size()-1) {
                    status = ServerServiceWindow.Status.editdns;
                    cR++;} else {status = ServerServiceWindow.Status.last;}

                relod_edit_page();
            }
        });
    }

    /**Эмитируем сработку окончания загрузки страницы (т.к. походу применяются AJAX метод и
     * стандартный обработкичк не срабатывает) в ручную с задержкой запускаем следующую итерацию*/
    private void relod_edit_page() {
        Platform.runLater(new Runnable() {
           @Override
           public void run() {
               try {
                   // Делаем паузу перед заменой следующего значения
                   // можно и без неё но там какие то ошибки выскакивают т.к. слишком быстро, не приятно
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                        e.printStackTrace();
               }
               System.out.println("Принудительная перезагрузка обработки");
               succeeded_run(); // просто метод обработчик запускаем

               // можно реально вызвать перезагрузку страницы сработает обработчик события,
               // но так как выше помоему лучше
               //webEngine.load(currentDNSList.getList().get(currentDNS).getUrl_dns_editor());
               // или
               //webEngine.reload();
           }
        });
    }

    public void setMainStage(Stage stage){
        this.mainStage = stage;
        ip.setText(IPAddres);
    }

    public static String getCurrentIP() {
        String result = null;
        try {
            BufferedReader reader = null;
            try {
                URL url = new URL("https://myip.by/");
                InputStream inputStream = null;
                inputStream = url.openStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder allText = new StringBuilder();
                char[] buff = new char[10024];

                int count = 0;
                while ((count = reader.read(buff)) != -1) {
                    allText.append(buff, 0, count);
                }
                // Строка содержащая IP имеет следующий вид
                // <a href="whois.php?127.0.0.1">whois 127.0.0.1</a>
                Integer indStart = allText.indexOf("\">whois ");
                Integer indEnd = allText.indexOf("</a>", indStart);

                String ipAddress = new String(allText.substring(indStart + 8, indEnd));
                if (ipAddress.split("\\.").length == 4) { // минимальная (неполная)
                    //проверка что выбранный текст является ip адресом.
                    result = ipAddress;
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void setCurrentIP(String IP){
        String ipa = getCurrentIP();;

        IPAddres = getCurrentIP();
        ip.setText(IPAddres);

        System.out.println("Current IP " + IPAddres);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Обновление DNS");
                click(null);
            }
        });
    }

    public void click_menu_exit(ActionEvent actionEvent) {
    }

    public void click_login(ActionEvent actionEvent) throws IOException {
        Parent parent;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/login.fxml"));
        parent = (Parent) loader.load();
        LoginController loginController = loader.getController();

        Stage newStage = new Stage();
        newStage.setTitle("Enter Login/Password by hoster.by");
        newStage.setScene(new Scene(parent));
        newStage.setResizable(false);
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.initOwner(mainStage);
        loginController.setMainStage(newStage);
        newStage.show();


    }

    public void click_dns(ActionEvent actionEvent) throws IOException {
        Parent parent;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../resources/dns_and_records.fxml"));
        parent = (Parent) loader.load();
        DNS_RecordController controller= loader.getController();

        Stage newStage = new Stage();
        newStage.setTitle("List DNS and Records by hoster.by");
        newStage.setScene(new Scene(parent));
        newStage.setResizable(false);
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.initOwner(mainStage);
        controller.setMainStage(newStage);
        newStage.show();
    }

    public void click_about(ActionEvent actionEvent) {
    }

    public void click_setting(ActionEvent actionEvent) {
    }

}

package com.leganas.dnsupdate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.leganas.dnsupdate.Assets.Account;
import com.leganas.dnsupdate.Assets.DNSRecord;
import com.leganas.dnsupdate.WindowController.Controller;
import com.leganas.dnsupdate.Assets.DNS;
import com.leganas.dnsupdate.Assets.DNSList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ServerServiceWindow extends Application {
    private static String ABS_PATH_TO_JFXWEBKIT_DLL = "";

    public enum Status {
        /**Фаза посылки запроса на ввод логина и пароля*/
        login_request,
        /**Фаза перенаправления на список DNS имён*/
        forward_to_dnslist,
        /**Фаза получения списка активных DNS на этом логине*/
        dnslist_request,
        /**Фаза посылки запроса на открытие окна Расширенный DNS-редактор (для каждого активного DNS)*/
        run_dns_editor_request,
        /**Фаза посылки запроса на открытие окна и заполнения данными нужных записей*/
        editdns,
        getRecords, last, procces, /**Все задачи завершены*/
        finish
    }

    public static Status status = Status.login_request;

    public static String IPAddres = "52.42.138.63";


    private static final String iconImageLoc =
            "http://icons.iconarchive.com/icons/scafer31000/bubble-circle-3/16/GameCenter-icon.png";

    private Stage stage;

    // По этому таймеру рядом с иконкой выводятся переодические напоминания
    private Timer notificationTimer = new Timer();

    // format used to display the current time in a tray icon notification.
    private DateFormat timeFormat = SimpleDateFormat.getTimeInstance();

    Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.stage = primaryStage;

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DNS.class, new DNS.DNSConverter());
        Gson gson = builder.create();

        Setting.dnsList = new DNSList();
//       временное создание списка DNS
//        Setting.dnsList.list = new ArrayList<>();
//        Setting.dnsList.list.add(new DNS("legan.by","",true, new ArrayList<DNSRecord>()));
//        String JsonTest = gson.toJson(Setting.dnsList);
//        System.out.println(JsonTest);
//        ReadWrite.writeJson("dnslist.json",JsonTest);

        // временная заполнялка account
//        Setting.account = new Account("логин","пароль",true);
//        String JsonTest = gson.toJson(Setting.account);
//        System.out.println(JsonTest);
//        ReadWrite.writeJson("account.json",JsonTest);


        try {
            String newJson2 = ReadWrite.read("account.json");
            Setting.account = gson.fromJson(newJson2,Account.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        try {
            String newJson = ReadWrite.read("dnslist.json");
            Setting.dnsList = gson.fromJson(newJson,DNSList.class);
        } catch (FileNotFoundException e) {
            Setting.dnsList.list = new ArrayList<>();
        } catch (JsonSyntaxException e) {
            Setting.dnsList.list = new ArrayList<>();
        }


        if ("Windows XP".equals(System.getProperty("os.name"))){
            ABS_PATH_TO_JFXWEBKIT_DLL = System.getProperty("user.dir");
            System.load(ABS_PATH_TO_JFXWEBKIT_DLL + "\\jfxwebkit.dll");
        }

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
//                Assets.serverController.dispose();
                System.exit(0);
            }
        });


        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        // Устанавливает стиль без кнопок закрыть и свернуть
//        stage.initStyle(StageStyle.TRANSPARENT);

        // Создаём разметку програмно и заполняем
//        StackPane layout = createLayout();
//        Scene scene = new Scene(layout);
//        scene.setFill(Color.TRANSPARENT);
//        stage.setScene(scene);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/sample.fxml"));
        Parent root = (Parent) loader.load();
        controller = loader.getController();
        stage.setTitle("Hello World");
        stage.setScene(new Scene(root, 1200, 500));
        controller.setMainStage(stage);
        stage.show();
    }

    private StackPane createLayout() {
        // create the layout for the javafx stage.
        StackPane layout = new StackPane(createContent());
        layout.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
        layout.setPrefSize(300, 200);

        // this dummy app just hides itself when the app screen is clicked.
        // a real app might have some interactive UI and a separate icon which hides the app window.
        layout.setOnMouseClicked(event -> stage.hide());
        return layout;
    }

    /**
     * For this dummy app, the (JavaFX scenegraph) content, just says "hello, world".
     * A real app, might load an FXML or something like that.
     *
     * @return the main window application content.
     */
    private Node createContent() {
        Label hello = new Label("Server Transport");
        hello.setStyle("-fx-font-size: 40px; -fx-text-fill: forestgreen;");
        Label instructions = new Label("(нажмите чтобы скрыть в трей)");
        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");

        VBox content = new VBox(10, hello, instructions);
        content.setAlignment(Pos.CENTER);

        return content;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            URL imageLoc = new URL(iconImageLoc);
            java.awt.Image image = ImageIO.read(imageLoc);
            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("Server, transport");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                notificationTimer.cancel();
                Platform.exit();
                tray.remove(trayIcon);

//                Assets.serverController.dispose();

                System.exit(0);
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // create a timer which periodically displays a notification message.
            notificationTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {

//                            controller.setCurrentIP(controller.getCurrentIP());

                            javax.swing.SwingUtilities.invokeLater(() ->
                                    trayIcon.displayMessage(
                                            "hello",
                                            "Current IP " + controller.getCurrentIP(),
                                            java.awt.TrayIcon.MessageType.INFO));
                        }
                    },
                    5_000,
                    60_000
            );

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

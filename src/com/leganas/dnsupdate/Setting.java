package com.leganas.dnsupdate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.leganas.dnsupdate.Assets.Account;
import com.leganas.dnsupdate.Assets.DNS;
import com.leganas.dnsupdate.Assets.DNSList;
import com.leganas.dnsupdate.Utils.ReadWrite;
import javafx.scene.control.Button;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by AndreyLS on 03.08.2017.
 */
public class Setting {
    public static Account account;
    public static DNSList dnsList;
    public static Button set_ip;
    public static boolean globalAuto_check_flag = true;

    public static void loadDNSList(){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DNS.class, new DNS.DNSConverter());
        Gson gson = builder.create();

        try {
            String newJson = ReadWrite.read("dnslist.json");
            Setting.dnsList = gson.fromJson(newJson,DNSList.class);
        } catch (FileNotFoundException e) {
            Setting.dnsList.list = new ArrayList<>();
        } catch (JsonSyntaxException e) {
            Setting.dnsList.list = new ArrayList<>();
        }

    }

    public static void saveDNSList(){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DNS.class, new DNS.DNSConverter());
        Gson gson = builder.create();

        String JsonTest = gson.toJson(dnsList);
        System.out.println(JsonTest);
        ReadWrite.writeJson("dnslist.json",JsonTest);
    }

    public static void loadAccount(){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Account.class, new Account.accountConverter());
        Gson gson = builder.create();
        String newJson2 = null;
        try {
            newJson2 = ReadWrite.read("account.json");
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        }
        Setting.account = gson.fromJson(newJson2,Account.class);
        if (Setting.account == null) Setting.account = new Account();
    }

    public static void saveAccount(){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Account.class, new Account.accountConverter());
        Gson gson = builder.create();
        String JsonTest = gson.toJson(Setting.account);
        System.out.println(JsonTest);
        ReadWrite.writeJson("account.json",JsonTest);
    }
}

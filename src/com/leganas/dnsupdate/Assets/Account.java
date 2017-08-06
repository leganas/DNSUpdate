package com.leganas.dnsupdate.Assets;

import com.google.gson.*;
import com.leganas.dnsupdate.Setting;

import java.lang.reflect.Type;

/**
 * Created by AndreyLS on 03.08.2017.
 */
public class Account {
    String login = "";
    String password = "";
    boolean check_flag = false;

    public Account(String login, String password, boolean check_flag) {
        this.login = login;
        this.password = password;
        this.check_flag = check_flag;
    }

    public Account() {

    }

    public static class accountConverter implements JsonSerializer<Account>, JsonDeserializer<Account> {
        @Override
        public Account deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            // Лень создавать отдельно для этого чекбокса реализацию записи в файл
            // заебеним всё сюда
            Boolean globalAuto_check_flag = object.get("globalAuto_check_flag").getAsBoolean();
            Setting.globalAuto_check_flag = globalAuto_check_flag;
            String login = object.get("login").getAsString();
            String password = object.get("password").getAsString();
            Boolean check_flag = object.get("check_flag").getAsBoolean();
            return new Account(login,password,check_flag);
        }

        @Override
        public JsonElement serialize(Account Account, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            object.addProperty("login", Account.getLogin());
            object.addProperty("password", Account.getPassword());
            object.addProperty("check_flag", Account.isCheck_flag());
            object.addProperty("globalAuto_check_flag", Setting.globalAuto_check_flag);
            return object;
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCheck_flag() {
        return check_flag;
    }

    public void setCheck_flag(boolean check_flag) {
        this.check_flag = check_flag;
    }
}

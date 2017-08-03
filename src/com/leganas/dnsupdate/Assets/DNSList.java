package com.leganas.dnsupdate.Assets;

import com.google.gson.*;
import com.leganas.dnsupdate.Setting;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by AndreyLS on 03.08.2017.
 */
public class DNSList {
    public String tdName = "Список обновляемых DNS и записей";
    public ArrayList<DNS> list = new ArrayList<>();

    public String getTdName() {
        return tdName;
    }

    public void setTdName(String tdName) {
        this.tdName = tdName;
    }

    public ArrayList<DNS> getList() {
        return list;
    }

    public void setList(ArrayList<DNS> list) {
        this.list = list;
    }

    public static class DNSListConverter implements JsonSerializer<DNSList>, JsonDeserializer<DNSList> {

        @Override
        public DNSList deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            JsonObject dnslistOb = object.get("dnslist").getAsJsonObject();
            JsonArray dnsListJson = dnslistOb.get("list").getAsJsonArray();

            DNSList dnsList = new DNSList();
            JsonElement tbNameOb = dnslistOb.get("tbName");

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DNS.class, new DNS.DNSConverter());
            Gson gson = builder.create();

            for (int i=0;i<dnsListJson.size();i++){
                DNS dns;
                JsonElement element = dnsListJson.get(i);
                dns = gson.fromJson(element, DNS.class);
                dnsList.list.add(dns);
            }
            return dnsList;
        }

        @Override
        public JsonElement serialize(DNSList dnsList, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            JsonObject dnslistOb = new JsonObject();
            JsonArray dnsListJson = new JsonArray();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DNS.class, new DNS.DNSConverter());
            Gson gson = builder.create();

            for (int i=0;i< Setting.dnsList.list.size();i++){
                dnsListJson.add(gson.toJsonTree(Setting.dnsList.list.get(i),DNS.class));
            }

            dnslistOb.addProperty("tbName",Setting.dnsList.tdName);
            dnslistOb.add("list",dnsListJson);
            object.add("dnslist",dnslistOb);

            return object;
        }
    }
}

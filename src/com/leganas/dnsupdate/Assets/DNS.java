package com.leganas.dnsupdate.Assets;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Класс описывающий DNS запись
 */
public class DNS {
    String name;
    String url_dns_editor;
    boolean flag_to_update = false;

    ArrayList<DNSRecord> dnsRecords;

    public ArrayList<DNSRecord> getDnsRecords() {
        return dnsRecords;
    }

    public void setDnsRecords(ArrayList<DNSRecord> dnsRecords) {
        this.dnsRecords = dnsRecords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl_dns_editor() {
        return url_dns_editor;
    }

    public void setUrl_dns_editor(String url_dns_editor) {
        this.url_dns_editor = url_dns_editor;
    }

    public Boolean getFlag_to_update() {
        return flag_to_update;
    }

    public void setFlag_to_update(Boolean flag_to_update) {
        this.flag_to_update = flag_to_update;
    }

    public static class DNSConverter implements JsonSerializer<DNS>, JsonDeserializer<DNS> {

        @Override
        public DNS deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            String name = object.get("name").getAsString();
            String url = object.get("url").getAsString();
            Boolean flag = object.get("flag").getAsBoolean();
            JsonArray dnsRecords = object.get("dnsRecords").getAsJsonArray();

            ArrayList<DNSRecord> records = new ArrayList<>();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DNSRecord.class, new DNSRecord.DNSRecordConverter());
            Gson gson = builder.create();

            for (int i =0; i<dnsRecords.size();i++) {
                DNSRecord record;
                JsonElement element = dnsRecords.get(i);
                record = gson.fromJson(element, DNSRecord.class);
                records.add(record);
            }

            return new DNS(name,url,flag,records);
        }

        @Override
        public JsonElement serialize(DNS dns, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            JsonArray dnsRecords = new JsonArray();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DNSRecord.class, new DNSRecord.DNSRecordConverter());
            Gson gson = builder.create();

            for (int i=0;i<dns.getDnsRecords().size();i++){
                dnsRecords.add(gson.toJsonTree(dns.getDnsRecords().get(i),DNSRecord.class));
            }

            object.addProperty("name", dns.getName());
            object.addProperty("url", dns.getUrl_dns_editor());
            object.addProperty("flag", dns.getFlag_to_update());
            object.add("dnsRecords",dnsRecords);
            return object;
        }
    }

    public DNS(String name, String url_dns_editor) {
        this.name = name;
        this.url_dns_editor = url_dns_editor;
        this.flag_to_update = false;
    }

    public DNS(String name, String url_dns_editor, Boolean flag_to_update) {
        this.name = name;
        this.url_dns_editor = url_dns_editor;
        this.flag_to_update = flag_to_update;
    }

    public DNS(String name, String url_dns_editor, Boolean flag_to_update, ArrayList<DNSRecord> dnsRecords) {
        this.name = name;
        this.url_dns_editor = url_dns_editor;
        this.flag_to_update = flag_to_update;
        this.dnsRecords = dnsRecords;
    }
}

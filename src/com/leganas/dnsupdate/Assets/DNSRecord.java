package com.leganas.dnsupdate.Assets;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by AndreyLS on 03.08.2017.
 */
public class DNSRecord {
    String Id;
    String hostName;
    String value;
    boolean flag_to_update = false;

    public static class DNSRecordConverter implements JsonSerializer<DNSRecord>, JsonDeserializer<DNSRecord> {

        @Override
        public DNSRecord deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject object = jsonElement.getAsJsonObject();
            String className = object.get("Id").getAsString();
            String hostName = object.get("hostName").getAsString();
            String value = object.get("value").getAsString();
            boolean flag_to_update = object.get("flag_to_update").getAsBoolean();
            return new DNSRecord(className,hostName,value,flag_to_update);
        }

        @Override
        public JsonElement serialize(DNSRecord dnsRecord, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            object.addProperty("Id", dnsRecord.getId());
            object.addProperty("hostName", dnsRecord.getHostName());
            object.addProperty("value", dnsRecord.getValue());
            object.addProperty("flag_to_update",dnsRecord.isFlag_to_update());
            return object;
        }
    }

    public DNSRecord() {

    }

    public DNSRecord(String id, String hostName, String value, boolean flag_to_update) {
        Id = id;
        this.hostName = hostName;
        this.value = value;
        this.flag_to_update = flag_to_update;
    }

    public DNSRecord(String className, String hostName, String value) {
        this.Id = className;
        this.hostName = hostName;
        this.value = value;
    }

    public boolean isFlag_to_update() {
        return flag_to_update;
    }

    public void setFlag_to_update(boolean flag_to_update) {
        this.flag_to_update = flag_to_update;
    }

    public String getId() {
        return Id;
    }

    public void setId(String className) {
        this.Id = className;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

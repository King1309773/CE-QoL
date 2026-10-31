package dev.rlcraftceaddons.ceqol.presets;

import com.google.gson.*;
import com.google.gson.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bounded, strict objects and scalar primitives; no arrays or duplicate keys. */
public final class PresetJson {
    public static final int MAX_BYTES=131072;
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private PresetJson() { }
    public static JsonObject document(String name,JsonObject settings) {
        JsonObject doc=new JsonObject();doc.addProperty("format","ceqol-preset");doc.addProperty("version",1);
        doc.addProperty("name",name);doc.add("settings",settings);return doc;
    }
    public static JsonObject validate(JsonObject doc,PresetSchema schema)throws IOException {
        try {
            if(doc.size()!=4 || !doc.get("format").isJsonPrimitive() || !doc.get("format").getAsJsonPrimitive().isString()
                    || !"ceqol-preset".equals(doc.get("format").getAsString())
                    || !doc.get("version").getAsJsonPrimitive().isNumber() || doc.get("version").getAsBigDecimal().intValueExact()!=1
                    || !doc.get("name").getAsJsonPrimitive().isString())throw new IllegalArgumentException();
            name(doc.get("name").getAsString());schema.validate(doc.getAsJsonObject("settings"));return doc;
        }catch(RuntimeException e){throw new IOException("Invalid or unsupported preset",e);}
    }
    public static void name(String name)throws IOException {
        if(name==null || name.length()>32 || !name.equals(name.trim()) || !name.matches("[\\p{L}\\p{N}_ -]+")
                || name.toUpperCase(Locale.ROOT).matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]"))throw new IOException("Invalid preset name");
    }
    public static byte[] encode(JsonObject doc){return JSON.toJson(doc).getBytes(StandardCharsets.UTF_8);}
    public static JsonObject decode(byte[] bytes,PresetSchema schema)throws IOException {
        if(bytes.length>MAX_BYTES)throw new IOException("Preset file is too large");
        try(JsonReader reader=new JsonReader(new InputStreamReader(new ByteArrayInputStream(bytes),StandardCharsets.UTF_8))) {
            reader.setLenient(false);JsonElement doc=read(reader,0);
            if(reader.peek()!=JsonToken.END_DOCUMENT || !doc.isJsonObject())throw new IOException("Trailing or invalid JSON");
            return validate(doc.getAsJsonObject(),schema);
        }catch(RuntimeException e){throw new IOException("Invalid JSON preset",e);}
    }
    private static JsonElement read(JsonReader reader,int depth)throws IOException {
        if(depth>4)throw new IOException("Preset nesting is too deep");
        switch(reader.peek()) {
            case BEGIN_OBJECT:
                JsonObject object=new JsonObject();Set<String> keys=new HashSet<>();reader.beginObject();
                while(reader.hasNext()){String name=reader.nextName();if(!keys.add(name))throw new IOException("Duplicate JSON key");object.add(name,read(reader,depth+1));}
                reader.endObject();return object;
            case STRING:return new JsonPrimitive(reader.nextString());
            case NUMBER:return new JsonPrimitive(new java.math.BigDecimal(reader.nextString()));
            case BOOLEAN:return new JsonPrimitive(reader.nextBoolean());
            default:throw new IOException("Unexpected JSON token");
        }
    }
}

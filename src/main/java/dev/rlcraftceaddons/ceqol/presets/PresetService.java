package dev.rlcraftceaddons.ceqol.presets;

import com.google.gson.*;
import dev.rlcraftceaddons.ceqol.config.ConfigFileStore;
import dev.rlcraftceaddons.sdtemperaturehud.client.HudSettings;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** User-triggered operations only. Importing never applies a preset. */
public final class PresetService {
    private final Path config;
    private final PresetSchema schema=new PresetSchema();
    private final PresetFiles files;
    private final Runnable refreshTemperature;
    public PresetService(Path config,Runnable refreshTemperature) {
        this.config=config.toAbsolutePath().normalize();files=new PresetFiles(this.config);this.refreshTemperature=refreshTemperature;
    }
    public JsonObject capture(String name,String scope)throws IOException {return PresetJson.document(name,schema.capture(scope,false));}
    public JsonObject defaults(String scope)throws IOException {return PresetJson.document("Defaults",schema.capture(scope,true));}
    public List<String> list(String folder)throws IOException {return files.list(folder);}
    public Path directory(String folder)throws IOException {return files.directory(folder);}
    public JsonObject read(String name)throws IOException {return PresetJson.decode(files.read("presets",name),schema);}
    public void save(JsonObject doc,boolean replace)throws IOException {
        PresetJson.validate(doc,schema);PresetFiles.write(files.file("presets",doc.get("name").getAsString()),PresetJson.encode(doc),replace);
    }
    public void export(String name,boolean replace)throws IOException {
        JsonObject doc=read(name);PresetFiles.write(files.file("exports",name),PresetJson.encode(doc),replace);
    }
    public JsonObject importFile(String filename,boolean replace)throws IOException {
        JsonObject doc=inspectImport(filename);save(doc,replace);return doc;
    }
    public JsonObject inspectImport(String filename)throws IOException {return PresetJson.decode(files.read("imports",filename),schema);}
    public boolean hasUndo()throws IOException {return Files.isRegularFile(files.file("preset-backups","Last before apply"));}
    public void undo()throws IOException {
        apply(PresetJson.decode(files.read("preset-backups","Last before apply"),schema));
    }
    public synchronized void apply(JsonObject document)throws IOException {
        JsonObject doc=PresetJson.validate(document,schema);
        JsonObject settings=doc.getAsJsonObject("settings");
        JsonObject before=schema.subsetCurrent(settings);
        JsonObject checkpoint=PresetJson.document("Before apply",before);
        Map<Path,byte[]> originals=new LinkedHashMap<>(),written=new LinkedHashMap<>();
        Path attempt=files.directory("preset-backups").resolve(UUID.randomUUID().toString());Files.createDirectory(attempt);
        for(Map.Entry<String,JsonElement> entry:settings.entrySet()) {
            Path target=config.resolve(schema.file(entry.getKey()));
            if(Files.isSymbolicLink(target))throw new IOException("Symbolic config targets cannot be replaced by a preset");
            byte[] bytes=Files.exists(target)?Files.readAllBytes(target):null;originals.put(target,bytes);
            if(bytes!=null)PresetFiles.write(attempt.resolve(target.getFileName()),bytes,false);
        }
        PresetFiles.write(attempt.resolve("before.json"),PresetJson.encode(checkpoint),false);
        boolean temperature=settings.has("temperature");
        try {
            for(Map.Entry<String,JsonElement> entry:settings.entrySet()) {
                String group=entry.getKey();JsonObject values=entry.getValue().getAsJsonObject();Path target=config.resolve(schema.file(group));
                ConfigFileStore.write(target.toFile(),c->{
                    try {schema.write(c,group,values);}catch(IOException e){throw new IllegalArgumentException(e);}
                },c->schema.matches(c,group,values));
                written.put(target,Files.readAllBytes(target));
            }
            schema.applyLive(settings);
            if(temperature){
                try {refreshTemperature.run();}
                finally {Path temp=config.resolve("sdtemperaturehud.cfg");if(Files.isRegularFile(temp))written.put(temp,Files.readAllBytes(temp));}
                HudSettings.refreshFromConfig();
            }
            for(Map.Entry<String,JsonElement> entry:settings.entrySet())if(!schema.matches(
                    ConfigFileStore.read(config.resolve(schema.file(entry.getKey())).toFile()),entry.getKey(),entry.getValue().getAsJsonObject()))
                throw new IOException("Preset persistence verification failed");
            PresetFiles.write(files.file("preset-backups","Last before apply"),PresetJson.encode(checkpoint),true);
        }catch(IOException|RuntimeException problem) {
            IOException failure=new IOException("Preset application failed; previous settings restored where possible",problem);
            for(Map.Entry<Path,byte[]> entry:originals.entrySet()) {
                Path path=entry.getKey();byte[] original=entry.getValue();
                try {
                    if(!written.containsKey(path))continue;
                    byte[] current=Files.exists(path)?Files.readAllBytes(path):null;
                    // A concurrent external edit must not be silently overwritten by rollback.
                    if(!Arrays.equals(current,written.get(path)))throw new IOException("Config changed externally during rollback");
                    if(original==null)Files.deleteIfExists(path);else PresetFiles.write(path,original,true);
                }catch(IOException rollback){failure.addSuppressed(rollback);}
            }
            schema.applyLive(before);
            if(temperature)try{
                if(refreshTemperature instanceof TemperaturePresetBridge)((TemperaturePresetBridge)refreshTemperature).reloadOnly();
                else refreshTemperature.run();
                HudSettings.refreshFromConfig();
            }catch(RuntimeException rollback){failure.addSuppressed(rollback);}
            throw failure;
        }
    }
}

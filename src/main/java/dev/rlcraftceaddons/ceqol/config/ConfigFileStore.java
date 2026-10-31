package dev.rlcraftceaddons.ceqol.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraftforge.common.config.Configuration;

/** Writes only verified staged bytes. Live settings are committed by callers after success. */
public final class ConfigFileStore {
    private ConfigFileStore() { }
    public static synchronized void write(File file, Consumer<Configuration> edit,
                                           Predicate<Configuration> verify) throws IOException {
        Path target=file.getCanonicalFile().toPath();
        Path parent=target.getParent();
        Files.createDirectories(parent);
        boolean existed=Files.exists(target);
        byte[] original=existed?Files.readAllBytes(target):new byte[0];
        Path stage=Files.createTempFile(parent,"ceqol-config-",".tmp");
        Path backupStage=null;
        try {
            Files.write(stage,original);
            Configuration config=read(stage.toFile());
            edit.accept(config);
            if(!config.hasChanged() && existed)return;
            config.save();
            // Forge's save catches IO errors; read back and check actual serialized values.
            Configuration saved=read(stage.toFile());
            if(Files.size(stage)==0 || !verify.test(saved) || saved.hasChanged())
                throw new IOException("Configuration serialization could not be verified");
            if(Files.exists(target)!=existed || existed && !Arrays.equals(original,Files.readAllBytes(target)))
                throw new IOException("Configuration was changed externally; retry to merge latest values");
            if(existed) {
                backupStage=Files.createTempFile(parent,"ceqol-backup-",".tmp");
                Files.write(backupStage,original);
                replace(backupStage,target.resolveSibling(target.getFileName()+".bak"));
            }
            replace(stage,target);
        } catch(RuntimeException failure) {
            throw new IOException("Configuration could not be saved",failure);
        } finally {
            Files.deleteIfExists(stage);
            if(backupStage!=null)Files.deleteIfExists(backupStage);
        }
    }
    /** Suppress Forge constructor recovery: malformed input must never turn into empty defaults. */
    public static Configuration read(File file)throws IOException {return new StrictConfiguration(file);}
    private static final class StrictConfiguration extends Configuration {
        private RuntimeException parseFailure;
        StrictConfiguration(File file)throws IOException {
            super(file);
            if(parseFailure!=null)throw new IOException("Malformed configuration; original preserved",parseFailure);
        }
        @Override public void load() {
            // Called virtually by Configuration's constructor, before this constructor body.
            try {super.load();}catch(RuntimeException failure){parseFailure=failure;}
        }
    }
    private static void replace(Path source,Path target)throws IOException {
        try {Files.move(source,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(AtomicMoveNotSupportedException unsupported) {
            Files.move(source,target,StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

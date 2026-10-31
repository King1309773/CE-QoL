package dev.rlcraftceaddons.ceqol.presets;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** All user-selected names stay below one of the fixed preset directories. */
final class PresetFiles {
    private final Path config;
    PresetFiles(Path config){this.config=config.toAbsolutePath().normalize();}
    Path directory(String folder)throws IOException {
        if(!Arrays.asList("presets","imports","exports","preset-backups").contains(folder))throw new IOException("Unknown preset folder");
        Files.createDirectories(config);
        Path root=config.toRealPath(),base=config.resolve("ceqol");
        if(Files.exists(base,LinkOption.NOFOLLOW_LINKS)) {
            if(!base.toRealPath().startsWith(root))throw new IOException("Preset directory escapes config folder");
        }else Files.createDirectory(base);
        Path dir=base.resolve(folder);
        if(Files.exists(dir,LinkOption.NOFOLLOW_LINKS)) {
            if(!dir.toRealPath().startsWith(root))throw new IOException("Preset directory escapes config folder");
        }else Files.createDirectory(dir);
        if(!dir.toRealPath().startsWith(root))throw new IOException("Preset directory escapes config folder");
        return dir;
    }
    Path file(String folder,String name)throws IOException {
        PresetJson.name(name);Path file=directory(folder).resolve(name+".json");
        if(Files.isSymbolicLink(file) || Files.exists(file) && !Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))throw new IOException("Invalid preset file");
        return file;
    }
    List<String> list(String folder)throws IOException {
        List<String> result=new ArrayList<>();
        try(DirectoryStream<Path> stream=Files.newDirectoryStream(directory(folder),"*.json")) {
            for(Path path:stream) {
                if(result.size()>=200)break;
                String name=path.getFileName().toString();name=name.substring(0,name.length()-5);
                try {PresetJson.name(name);if(!Files.isSymbolicLink(path)&&Files.isRegularFile(path))result.add(name);}catch(IOException ignored){}
            }
        }
        result.sort(String.CASE_INSENSITIVE_ORDER);return result;
    }
    byte[] read(String folder,String name)throws IOException {
        Path file=file(folder,name);if(Files.size(file)>PresetJson.MAX_BYTES)throw new IOException("Preset too large");
        byte[] bytes=Files.readAllBytes(file);if(bytes.length>PresetJson.MAX_BYTES)throw new IOException("Preset too large");return bytes;
    }
    static void write(Path file,byte[] bytes,boolean replace)throws IOException {
        Files.createDirectories(file.getParent());
        if(Files.isSymbolicLink(file))throw new IOException("Refusing symbolic preset target");
        Path stage=Files.createTempFile(file.getParent(),"ceqol-preset-",".tmp");
        try {
            Files.write(stage,bytes);
            if(!replace) {Files.move(stage,file);return;}
            try {Files.move(stage,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(stage,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(stage);}
    }
}

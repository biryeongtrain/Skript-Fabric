package ch.njol.skript.config;

import java.io.File;

public class Config {

    private final String name;
    private final String fileName;
    private final File file;
    private boolean valid = true;

    public Config(String name, String fileName, File file) {
        this.name = name;
        this.fileName = fileName;
        this.file = file;
    }

    public String name() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public boolean valid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
    }
}

package ch.njol.skript.config;

import java.io.File;

public class Config implements NodeNavigator {

    private final String name;
    private final String fileName;
    private final File file;
    private final SectionNode mainNode;
    private boolean valid = true;
    private int errors;

    public Config(String name, String fileName, File file) {
        this.name = name;
        this.fileName = fileName;
        this.file = file;
        this.mainNode = new SectionNode(this);
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
        return valid && errors == 0;
    }

    public void invalidate() {
        valid = false;
    }

    void recordError() {
        errors++;
    }

    public int errorCount() {
        return errors;
    }

    public SectionNode getMainNode() {
        return mainNode;
    }

    public String getByPath(String path) {
        return getValue(path);
    }

    @Override
    public Node get(String key) {
        return mainNode.get(key);
    }

    @Override
    public Node getCurrentNode() {
        return mainNode;
    }
}

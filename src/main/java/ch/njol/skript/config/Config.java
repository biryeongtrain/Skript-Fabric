package ch.njol.skript.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.jetbrains.annotations.Nullable;

public class Config implements NodeNavigator {

    private final String name;
    private final String fileName;
    private final File file;
    private final SectionNode mainNode;
    private String separator = ":";
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

    public String getSeparator() {
        return separator;
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

    public void load(Object object) {
        String path = object instanceof OptionSection section ? section.key + "." : "";
        load(object.getClass(), object, path);
    }

    public void load(Class<?> type) {
        load(type, null, "");
    }

    private void load(Class<?> type, @Nullable Object instance, String path) {
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            if (instance == null && !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                if (OptionSection.class.isAssignableFrom(field.getType())) {
                    OptionSection section = (OptionSection) field.get(instance);
                    if (section != null) {
                        load(section.getClass(), section, path + section.key + ".");
                    }
                } else if (Option.class.isAssignableFrom(field.getType())) {
                    Option<?> option = (Option<?>) field.get(instance);
                    if (option != null) {
                        option.set(this, path);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
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

package ch.njol.skript.config;

public class EntryNode extends Node {

    private String value;

    public EntryNode() {
    }

    public EntryNode(String key, String value) {
        super(key);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

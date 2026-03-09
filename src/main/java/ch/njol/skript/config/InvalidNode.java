package ch.njol.skript.config;

public class InvalidNode extends VoidNode {

    public InvalidNode(String value, String comment, SectionNode parent, int lineNum) {
        super(value, comment, parent, lineNum);
        Config config = getConfig();
        if (config != null) {
            config.recordError();
        }
    }
}

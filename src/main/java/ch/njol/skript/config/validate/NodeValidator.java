package ch.njol.skript.config.validate;

import ch.njol.skript.config.Node;

public interface NodeValidator {

    boolean validate(Node node);
}

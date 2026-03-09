package ch.njol.skript.config.validate;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import java.util.Locale;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

@Deprecated(since = "2.10.0", forRemoval = true)
public class EnumEntryValidator<E extends Enum<E>> extends EntryValidator {

    private final Class<E> enumType;
    private final Consumer<E> setter;
    private @Nullable String allowedValues;

    public EnumEntryValidator(Class<E> enumType, Consumer<E> setter) {
        this.enumType = enumType;
        this.setter = setter;
        if (enumType.getEnumConstants().length <= 12) {
            StringBuilder builder = new StringBuilder();
            for (E constant : enumType.getEnumConstants()) {
                if (builder.length() != 0) {
                    builder.append(", ");
                }
                builder.append(constant.name());
            }
            allowedValues = builder.toString();
        }
    }

    public EnumEntryValidator(Class<E> enumType, Consumer<E> setter, String allowedValues) {
        this.enumType = enumType;
        this.setter = setter;
        this.allowedValues = allowedValues;
    }

    @Override
    public boolean validate(Node node) {
        if (!super.validate(node)) {
            return false;
        }
        EntryNode entryNode = (EntryNode) node;
        try {
            setter.accept(Enum.valueOf(enumType, entryNode.getValue().toUpperCase(Locale.ENGLISH).replace(' ', '_')));
            return true;
        } catch (IllegalArgumentException e) {
            Skript.error("'" + entryNode.getValue() + "' is not a valid value for '" + entryNode.getKey()
                    + "'" + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
            return false;
        }
    }
}

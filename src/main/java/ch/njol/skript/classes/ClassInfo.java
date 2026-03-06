package ch.njol.skript.classes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

public class ClassInfo<T> {

    public interface Parser<T> {
        boolean canParse(ch.njol.skript.lang.ParseContext context);

        @Nullable T parse(String input, ch.njol.skript.lang.ParseContext context);
    }

    private final Class<T> type;
    private final Map<Property<?>, Property.PropertyInfo<?>> properties = new ConcurrentHashMap<>();
    private @Nullable Parser<T> parser;

    public ClassInfo(Class<T> type) {
        this.type = type;
    }

    public Class<T> getC() {
        return type;
    }

    public boolean hasProperty(Property<?> property) {
        return properties.containsKey(property);
    }

    @SuppressWarnings("unchecked")
    public <H extends PropertyHandler<?>> @Nullable Property.PropertyInfo<H> getPropertyInfo(Property<H> property) {
        return (Property.PropertyInfo<H>) properties.get(property);
    }

    public <H extends PropertyHandler<?>> void setPropertyInfo(Property<H> property, H handler) {
        properties.put(property, new Property.PropertyInfo<>(property, handler));
    }

    public @Nullable Parser<T> getParser() {
        return parser;
    }

    public void setParser(@Nullable Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public String toString() {
        return type.getSimpleName();
    }
}

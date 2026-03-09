package ch.njol.skript.entity;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class EntityType implements Cloneable {

    private static boolean registered;

    public int amount = -1;
    public final EntityData<?> data;

    public EntityType(EntityData<?> data, int amount) {
        this.data = data;
        this.amount = amount;
    }

    public EntityType(Class<? extends Entity> entityClass, int amount) {
        this(EntityData.fromClass(entityClass), amount);
    }

    public EntityType(Entity entity) {
        this(EntityData.fromEntity(entity), 1);
    }

    private EntityType(EntityData<?> data) {
        this(data, 1);
    }

    public static synchronized void register() {
        EntityData.register();
        if (registered) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(EntityType.class, "entitytype")
                .defaultExpression(new SimpleLiteral<>(new EntityType(new SimpleEntityData("entity", Entity.class)), true))
                .parser(new Parser<EntityType>() {
                    @Override
                    public @Nullable EntityType parse(String input, ParseContext context) {
                        return EntityType.parse(input);
                    }

                    @Override
                    public String toString(EntityType object, int flags) {
                        return object.toString(flags);
                    }

                    @Override
                    public String toVariableNameString(EntityType object) {
                        return "entitytype:" + object;
                    }
                }));
        registered = true;
    }

    public static @Nullable EntityType parse(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String trimmed = input.trim();
        int amount = -1;
        int split = trimmed.indexOf(' ');
        if (split > 0) {
            String first = trimmed.substring(0, split);
            if (first.matches("\\d+")) {
                amount = Utils.parseInt(first);
                trimmed = trimmed.substring(split + 1);
            } else if ("a".equalsIgnoreCase(first) || "an".equalsIgnoreCase(first)) {
                amount = 1;
                trimmed = trimmed.substring(split + 1);
            }
        }
        EntityData<?> data = EntityData.parse(trimmed);
        if (data == null) {
            return null;
        }
        return new EntityType(data, amount);
    }

    public int getAmount() {
        return amount == -1 ? 1 : amount;
    }

    public boolean isInstance(Entity entity) {
        return data.isInstance(entity);
    }

    public String toString(int flags) {
        return getAmount() == 1 ? data.toString(flags) : getAmount() + " " + data.toString(flags);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public EntityType clone() {
        return new EntityType(data, amount);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityType other)) {
            return false;
        }
        return amount == other.amount && data.equals(other.data);
    }

    @Override
    public int hashCode() {
        return 31 * data.hashCode() + amount;
    }
}

package ch.njol.skript.entity;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import java.util.Iterator;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public abstract class EntityData<E extends Entity> {

    private static boolean registered;

    private final String codeName;
    private final Class<? extends E> entityClass;

    protected EntityData(String codeName, Class<? extends E> entityClass) {
        this.codeName = codeName;
        this.entityClass = entityClass;
    }

    public static synchronized void register() {
        EntityDataRegistry.ensureInitialized();
        if (registered) {
            return;
        }
        Classes.registerClassInfo(new ClassInfo<>(EntityData.class, "entitydata")
                .user("entity ?types?")
                .defaultExpression(new SimpleLiteral<>(new SimpleEntityData("entity", Entity.class), true))
                .before("entitytype")
                .supplier(() -> {
                    Iterator<EntityData<?>> iterator = EntityDataRegistry.all().iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public EntityData<?> next() {
                            return iterator.next();
                        }
                    };
                })
                .parser(new Parser<EntityData<?>>() {
                    @Override
                    public @Nullable EntityData<?> parse(String input, ParseContext context) {
                        return EntityData.parse(input);
                    }

                    @Override
                    public String toString(EntityData<?> object, int flags) {
                        return object.toString(flags);
                    }

                    @Override
                    public String toVariableNameString(EntityData<?> object) {
                        return "entitydata:" + object;
                    }
                }));
        registered = true;
    }

    public static @Nullable EntityData<?> parse(String input) {
        EntityDataRegistry.ensureInitialized();
        return EntityDataRegistry.parse(input);
    }

    public static @Nullable EntityData<?> fromEntity(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        EntityData<?> exact = EntityDataRegistry.fromType(entity.getType());
        return exact != null ? exact : EntityDataRegistry.fromClass(entity.getClass());
    }

    public static @Nullable EntityData<?> fromClass(Class<? extends Entity> entityClass) {
        EntityDataRegistry.ensureInitialized();
        return EntityDataRegistry.fromClass(entityClass);
    }

    public final String getCodeName() {
        return codeName;
    }

    public Class<? extends E> getType() {
        return entityClass;
    }

    public boolean isInstance(@Nullable Entity entity) {
        return entity != null && matches(entity.getType());
    }

    public abstract boolean matches(net.minecraft.world.entity.EntityType<?> entityType);

    public String toString(int flags) {
        return codeName;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    @Override
    public int hashCode() {
        return codeName.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityData<?> other)) {
            return false;
        }
        return codeName.equals(other.codeName);
    }
}

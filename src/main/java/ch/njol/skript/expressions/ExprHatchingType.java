package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Hatching Entity Type")
@Description("The type of the entity that will be hatched in an egg throw event.")
@Example("""
    on player egg throw:
        set the hatching entity type to a chicken
    """)
@Events("Egg Throw")
@Since("2.7")
public class ExprHatchingType extends SimpleExpression<EntityData<?>> {

    private static final @Nullable Class<?> EGG_THROW_EVENT = resolveEggThrowEventClass();

    static {
        @SuppressWarnings("unchecked")
        Class<EntityData<?>> type = (Class<EntityData<?>>) (Class<?>) EntityData.class;
        Skript.registerExpression(ExprHatchingType.class, type, "[the] hatching entity [type]");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (EGG_THROW_EVENT == null || !getParser().isCurrentEvent(EGG_THROW_EVENT)) {
            Skript.error("You can't use 'the hatching entity type' outside of a Player Egg Throw event.");
            return false;
        }
        return true;
    }

    @Override
    protected EntityData<?> @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEggThrowEventHandle handle) || handle.hatchingType() == null) {
            return null;
        }
        EntityData<?> data = EntityData.fromClass(handle.hatchingType().getBaseClass());
        return data == null ? null : new EntityData[]{data};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET -> new Class[]{EntityData.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricEggThrowEventHandle handle)) {
            return;
        }
        EntityType<?> type = delta == null ? EntityType.CHICKEN : resolveType((EntityData<?>) delta[0]);
        if (type != null && type.canSerialize() && type.canSummon()) {
            handle.setHatchingType(type);
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends EntityData<?>> getReturnType() {
        return (Class<? extends EntityData<?>>) (Class<?>) EntityData.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the hatching entity type";
    }

    private static @Nullable EntityType<?> resolveType(@Nullable EntityData<?> data) {
        if (data == null) {
            return null;
        }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (data.matches(type)) {
                return type;
            }
        }
        return null;
    }

    private static @Nullable Class<?> resolveEggThrowEventClass() {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow");
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Hatching Number")
@Description("The number of entities that will be hatched in an egg throw event.")
@Example("""
    on player egg throw:
        set the hatching number to 2
    """)
@Events("Egg Throw")
@Since("2.7")
public class ExprHatchingNumber extends SimpleExpression<Byte> {

    private static final @Nullable Class<?> EGG_THROW_EVENT = resolveEggThrowEventClass();

    static {
        Skript.registerExpression(ExprHatchingNumber.class, Byte.class, "[the] hatching number");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (EGG_THROW_EVENT == null || !getParser().isCurrentEvent(EGG_THROW_EVENT)) {
            Skript.error("You can't use 'the hatching number' outside of a Player Egg Throw event.");
            return false;
        }
        return true;
    }

    @Override
    protected Byte @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEggThrowEventHandle handle)) {
            return null;
        }
        return new Byte[]{handle.hatches()};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricEggThrowEventHandle handle) || delta == null || delta[0] == null) {
            return;
        }

        int value = ((Number) delta[0]).intValue();
        if (mode != ChangeMode.SET) {
            if (mode == ChangeMode.REMOVE) {
                value *= -1;
            }
            value += handle.hatches();
        }
        handle.setHatches((byte) Math.min(Math.max(0, value), Byte.MAX_VALUE));
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Byte> getReturnType() {
        return Byte.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the hatching number";
    }

    private static @Nullable Class<?> resolveEggThrowEventClass() {
        try {
            return Class.forName("ch.njol.skript.effects.FabricEffectEventHandles$PlayerEggThrow");
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}

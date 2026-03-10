package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Toggle")
@Description("Toggle the state of a block or boolean.")
@Example("""
        # use arrows to toggle switches, doors, etc.
        on projectile hit:
            projectile is arrow
            toggle the block at the arrow
        """)
@Example("""
        # With booleans
        toggle gravity of player
        """)
@Since("1.4, 2.12 (booleans)")
public final class EffToggle extends Effect {

    private enum Action {
        ACTIVATE, DEACTIVATE, TOGGLE;

        boolean apply(boolean current) {
            return switch (this) {
                case ACTIVATE -> true;
                case DEACTIVATE -> false;
                case TOGGLE -> !current;
            };
        }
    }

    private enum Type {
        BLOCKS, BOOLEANS, MIXED;

        static Type fromClass(Expression<?> expression) {
            boolean blockType = expression.canReturn(FabricBlock.class);
            boolean booleanType = expression.canReturn(Boolean.class);
            if (blockType && !booleanType) {
                return BLOCKS;
            }
            if (booleanType && !blockType) {
                return BOOLEANS;
            }
            return MIXED;
        }
    }

    private static final Patterns<Action> PATTERNS = new Patterns<>(new Object[][]{
            {"(open|turn on|activate) %blocks%", Action.ACTIVATE},
            {"(close|turn off|de[-]activate) %blocks%", Action.DEACTIVATE},
            {"(toggle|switch) [[the] state of] %blocks/booleans%", Action.TOGGLE}
    });

    private static boolean registered;

    private Expression<?> togglables;
    private Action action;
    private Type type;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffToggle.class, PATTERNS.getPatterns());
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        togglables = expressions[0];
        action = PATTERNS.getInfo(matchedPattern);
        type = Type.fromClass(togglables);
        if (type == Type.BOOLEANS
                && !ChangerUtils.acceptsChange(togglables, ChangeMode.SET, Boolean.class)) {
            Skript.error("Cannot toggle '" + togglables + "' as it cannot be set to booleans.");
            return false;
        }
        if (type == Type.MIXED
                && !(ChangerUtils.acceptsChange(togglables, ChangeMode.SET, FabricBlock.class)
                && ChangerUtils.acceptsChange(togglables, ChangeMode.SET, Boolean.class))) {
            Skript.error("Cannot toggle '" + togglables + "' as it cannot be set to both blocks and booleans.");
            return false;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        switch (type) {
            case BLOCKS -> toggleBlocks(event);
            case BOOLEANS -> toggleBooleans(event);
            case MIXED -> toggleMixed(event);
        }
    }

    private void toggleBlocks(SkriptEvent event) {
        for (Object value : togglables.getArray(event)) {
            if (value instanceof FabricBlock block) {
                toggleSingleBlock(block);
            }
        }
    }

    private void toggleSingleBlock(FabricBlock block) {
        BlockState state = block.state();
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            block.level().setBlock(block.position(), state.setValue(BlockStateProperties.OPEN,
                    action.apply(state.getValue(BlockStateProperties.OPEN))), 3);
            return;
        }
        if (state.hasProperty(BlockStateProperties.POWERED)) {
            block.level().setBlock(block.position(), state.setValue(BlockStateProperties.POWERED,
                    action.apply(state.getValue(BlockStateProperties.POWERED))), 3);
        }
    }

    private void toggleBooleans(SkriptEvent event) {
        changeInPlace(event, value -> {
            if (!(value instanceof Boolean bool)) {
                return null;
            }
            return action.apply(bool);
        });
    }

    private void toggleMixed(SkriptEvent event) {
        changeInPlace(event, value -> {
            if (value instanceof FabricBlock block) {
                toggleSingleBlock(block);
                return block;
            }
            if (value instanceof Boolean bool) {
                return action.apply(bool);
            }
            return value;
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void changeInPlace(SkriptEvent event, Function<Object, Object> changeFunction) {
        ((Expression) togglables).changeInPlace(event, changeFunction);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String actionText = switch (action) {
            case ACTIVATE -> "activate";
            case DEACTIVATE -> "deactivate";
            case TOGGLE -> "toggle";
        };
        return actionText + " " + togglables.toString(event, debug);
    }
}

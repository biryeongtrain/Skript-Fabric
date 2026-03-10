package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.Comparator;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Age of Block/Entity")
@Description({
        "Returns the age or maximum age of blocks and age for entities (there in no maximum age for entities).",
        "For blocks, 'Age' represents the different growth stages that a crop-like block can go through. " +
                "A value of 0 indicates that the crop was freshly planted, whilst a value equal to 'maximum age' indicates that the crop is ripe and ready to be harvested.",
        "For entities, 'Age' represents the time left for them to become adults and it's in minus increasing to be 0 which means they're adults, " +
                "e.g. A baby cow needs 20 minutes to become an adult which equals to 24,000 ticks so their age will be -24000 once spawned."
})
@Example("""
    # Set targeted crop to fully grown crop
    set age of targeted block to maximum age of targeted block
    """)
@Example("""
    # Spawn a baby cow that will only need 1 minute to become an adult
    spawn a baby cow at player
    set age of last spawned entity to -1200 # in ticks = 60 seconds
    """)
@Since("2.7")
public class ExprAge extends SimplePropertyExpression<Object, Integer> {

    static {
        register(ExprAge.class, Integer.class, "[:max[imum]] age", "blocks/livingentities");
    }

    private boolean isMax;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        isMax = parseResult.hasTag("max");
        setExpr((Expression<? extends Object>) exprs[0]);
        if (isMax && !getExpr().canReturn(FabricBlock.class)) {
            Skript.error("Cannot use 'max age' expression with entities, use just the 'age' expression instead");
            return false;
        }
        return true;
    }

    @Override
    public @Nullable Integer convert(Object object) {
        if (object instanceof FabricBlock block) {
            IntegerProperty property = findAgeProperty(block.state());
            if (property == null) {
                return null;
            }
            return isMax ? maximumAge(property) : block.state().getValue(property);
        }
        if (object instanceof AgeableMob ageable) {
            return isMax ? null : ageable.getAge();
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (isMax) {
            return null;
        }
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int newValue = mode == ChangeMode.RESET ? 0 : delta == null ? 0 : ((Number) delta[0]).intValue();
        for (Object object : getExpr().getArray(event)) {
            Integer oldValue = convert(object);
            if (oldValue == null && mode != ChangeMode.RESET) {
                continue;
            }
            switch (mode) {
                case REMOVE -> setAge(object, oldValue - newValue);
                case ADD -> setAge(object, oldValue + newValue);
                case SET -> setAge(object, newValue);
                case RESET -> {
                    if (object instanceof AgeableMob) {
                        newValue = -24000;
                    }
                    setAge(object, newValue);
                }
                default -> {
                }
            }
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return (isMax ? "maximum " : "") + "age";
    }

    static @Nullable IntegerProperty findAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integerProperty && "age".equals(integerProperty.getName())) {
                return integerProperty;
            }
        }
        return null;
    }

    private static int maximumAge(IntegerProperty property) {
        return property.getPossibleValues().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    private static void setAge(Object object, int value) {
        if (object instanceof FabricBlock block) {
            IntegerProperty property = findAgeProperty(block.state());
            if (property == null) {
                return;
            }
            int clamped = Math.max(0, Math.min(value, maximumAge(property)));
            block.level().setBlock(block.position(), block.state().setValue(property, clamped), 3);
            return;
        }
        if (object instanceof AgeableMob ageable) {
            ageable.setAge(value);
        }
    }
}

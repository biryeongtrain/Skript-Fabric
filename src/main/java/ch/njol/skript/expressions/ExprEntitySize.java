package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity Size")
@Description({
        "Changes the entity size of slimes and phantoms. This is not the same as changing the scale attribute of an entity.",
        "When changing the size of a slime, its health is fully resorted and will have changes done to its max health, movement speed and attack damage.",
        "The default minecraft size of a slime is anywhere between 0 and 2, with a maximum of 126.",
        "The default minecraft size of a phantom is 0 with a maximum size of 64."
})
@Example("""
    spawn a slime at player:
        set entity size of event-entity to 5
        set name of event-entity to "King Slime Jorg"
    """)
@Since("2.11")
public class ExprEntitySize extends SimplePropertyExpression<LivingEntity, Integer> {

    private static final int MAXIMUM_SLIME_SIZE = 127;
    private static final int MAXIMUM_PHANTOM_SIZE = 64;

    static {
        register(ExprEntitySize.class, Integer.class, "entity size", "livingentities");
    }

    @Override
    public @Nullable Integer convert(LivingEntity entity) {
        if (entity instanceof Phantom phantom) {
            return phantom.getPhantomSize();
        }
        if (entity instanceof Slime slime) {
            return slime.getSize() - 1;
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int sizeDelta = delta == null ? 0 : ((Number) delta[0]).intValue();
        if (mode == ChangeMode.REMOVE) {
            sizeDelta = -sizeDelta;
        }
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (entity instanceof Phantom phantom) {
                int next = switch (mode) {
                    case ADD, REMOVE -> phantom.getPhantomSize() + sizeDelta;
                    case SET, RESET -> sizeDelta;
                    default -> phantom.getPhantomSize();
                };
                phantom.setPhantomSize(Math.max(0, Math.min(next, MAXIMUM_PHANTOM_SIZE)));
            } else if (entity instanceof Slime slime) {
                int next = switch (mode) {
                    case ADD, REMOVE -> slime.getSize() + sizeDelta;
                    case SET, RESET -> sizeDelta + 1;
                    default -> slime.getSize();
                };
                slime.setSize(Math.max(1, Math.min(next, MAXIMUM_SLIME_SIZE)), true);
            }
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "entity size";
    }
}

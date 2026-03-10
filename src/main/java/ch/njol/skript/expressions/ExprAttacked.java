package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricDamageEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Attacked")
@Description("The victim of a damage event, e.g. when a player attacks a zombie this expression represents the zombie.")
@Example("""
	on damage:
		victim is a creeper
	""")
@Since("1.3")
@Events("damage")
public class ExprAttacked extends SimpleExpression<Entity> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(ExprAttacked.class, Entity.class, "[the] (attacked|damaged|victim) [<(.+)>]");
    }

    @SuppressWarnings({"null", "NotNullFieldNotInitialized"})
    private EntityData<?> type;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        String rawType = parseResult.regexes.size() == 0 ? null : parseResult.regexes.get(0).group();
        if (rawType == null) {
            type = EntityData.fromClass(Entity.class);
            if (type == null) {
                type = EntityData.parse("entity");
            }
            return type != null;
        }
        EntityData<?> parsed = EntityData.parse(rawType);
        if (parsed == null) {
            Skript.error("'" + rawType + "' is not an entity type");
            return false;
        }
        type = parsed;
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricDamageEventHandle.class};
    }

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricDamageEventHandle handle)) {
            return null;
        }
        Entity entity = handle.entity();
        if (entity == null || type == null || !type.isInstance(entity)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Entity[] one = (Entity[]) java.lang.reflect.Array.newInstance(type.getType(), 1);
        one[0] = entity;
        return one;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return type == null ? Entity.class : type.getType();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return event == null ? "the attacked " + type : String.valueOf(getSingle(event));
    }
}

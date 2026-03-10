package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item Despawn")
@Description("Prevent a dropped item from naturally despawning through Minecraft's timer.")
@Example("prevent all dropped items from naturally despawning")
@Example("allow all dropped items to naturally despawn")
@Since("2.11")
public class EffItemDespawn extends Effect {

    private static boolean registered;

    private Expression<ItemEntity> entities;
    private boolean prevent;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffItemDespawn.class,
                "(prevent|disallow) %itementities% from (naturally despawning|despawning naturally)",
                "allow natural despawning of %itementities%",
                "allow %itementities% to (naturally despawn|despawn naturally)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        prevent = matchedPattern == 0;
        entities = (Expression<ItemEntity>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (ItemEntity item : entities.getArray(event)) {
            if (prevent) {
                EffectRuntimeSupport.invokeCompatible(item, "setNeverDespawn");
            } else {
                EffectRuntimeSupport.setField(item, "itemAge", 0);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        if (prevent) {
            builder.append("prevent", entities, "from naturally despawning");
        } else {
            builder.append("allow", entities, "to naturally despawn");
        }
        return builder.toString();
    }
}

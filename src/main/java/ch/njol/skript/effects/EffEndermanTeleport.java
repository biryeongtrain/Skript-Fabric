package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Enderman Teleport")
@Description({
        "Make an enderman teleport randomly or towards an entity.",
        "Teleporting towards an entity teleports in the direction to the entity and not to them."
})
@Example("make last spawned enderman teleport randomly")
@Example("""
        loop 10 times:
            make all endermen teleport towards player
        """)
@RequiredPlugins("Minecraft 1.20.1+")
@Since("2.11")
public class EffEndermanTeleport extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private @Nullable Expression<Entity> target;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffEndermanTeleport.class,
                "make %livingentities% (randomly teleport|teleport randomly)",
                "force %livingentities% to (randomly teleport|teleport randomly)",
                "make %livingentities% teleport [randomly] towards %entity%",
                "force %livingentities% to teleport [randomly] towards %entity%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        if (matchedPattern >= 2) {
            target = (Expression<Entity>) exprs[1];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Entity targetEntity = target == null ? null : target.getSingle(event);
        for (LivingEntity entity : entities.getArray(event)) {
            if (!(entity instanceof EnderMan enderman)) {
                continue;
            }
            if (targetEntity == null) {
                EffectRuntimeSupport.invokeCompatible(enderman, new String[]{"teleport", "teleportRandomly", "randomTeleport"});
            } else {
                Object invoked = EffectRuntimeSupport.invokeCompatible(enderman, new String[]{"teleportTowards"}, targetEntity);
                if (invoked == null) {
                    EffectRuntimeSupport.invokeCompatible(enderman, new String[]{"lookAt"}, targetEntity);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("make", entities);
        if (target == null) {
            builder.append("randomly teleport");
        } else {
            builder.append("teleport towards", target);
        }
        return builder.toString();
    }
}

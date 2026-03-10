package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.effects.EffDrop;
import ch.njol.skript.effects.EffFireworkLaunch;
import ch.njol.skript.effects.EffLightning;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLastSpawnedEntity extends SimpleExpression<Entity> {

    static {
        Skript.registerExpression(
                ExprLastSpawnedEntity.class,
                Entity.class,
                "[the] [last[ly]] (0:spawned|1:shot) %*entitydata%",
                "[the] [last[ly]] dropped (2:item)",
                "[the] [last[ly]] (created|struck) (3:lightning)",
                "[the] [last[ly]] (launched|deployed) (4:firework)"
        );
    }

    private EntityData<?> type;
    private int from;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        from = parseResult.mark;
        type = switch (from) {
            case 2 -> EntityData.fromClass(ItemEntity.class);
            case 3 -> EntityData.fromClass(LightningBolt.class);
            case 4 -> EntityData.fromClass(FireworkRocketEntity.class);
            default -> ((Literal<EntityData<?>>) exprs[0]).getSingle(null);
        };
        return type != null;
    }

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        Entity spawned = switch (from) {
            case 0 -> (Entity) ExpressionHandleSupport.staticField("ch.njol.skript.sections.EffSecSpawn", "lastSpawned");
            case 1 -> (Entity) ExpressionHandleSupport.staticField("ch.njol.skript.sections.EffSecShoot", "lastSpawned");
            case 2 -> EffDrop.lastSpawned;
            case 3 -> EffLightning.lastSpawned;
            case 4 -> EffFireworkLaunch.lastSpawned;
            default -> null;
        };
        if (spawned == null || !type.isInstance(spawned)) {
            return null;
        }
        Entity[] values = (Entity[]) Array.newInstance(type.getType(), 1);
        values[0] = spawned;
        return values;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return type.getType();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the last spawned " + type;
    }
}

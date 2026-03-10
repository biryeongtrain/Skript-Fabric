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
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Zombify Villager")
@Description({
        "Turn a villager into a zombie villager. Cure a zombie villager immediately or after specified amount of time.",
        "This effect removes the old entity and creates a new entity.",
        "Zombifying a villager stored in a variable will update the variable to the new zombie villager.",
        "Curing a zombie villager does not update the variable."
})
@Example("zombify last spawned villager")
@Example("""
        set {_villager} to last spawned villager
        zombify {_villager}
        if {_villager} is a zombie villager:
            # This will pass because '{_villager}' gets changed to the new zombie villager
        """)
@Example("""
        set {_villager} to last spawned villager
        zombify last spawned villager
        if {_villager} is a zombie villager:
            # This will fail because the variable was not provided when zombifying
        """)
@Example("unzombify {_zombieVillager}")
@Example("unzombify {_zombieVillager} after 2 seconds")
@Since("2.11")
public class EffZombify extends Effect {

    private static boolean registered;

    private Expression<LivingEntity> entities;
    private @Nullable Expression<Timespan> timespan;
    private boolean zombify;
    private boolean changeInPlace;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffZombify.class,
                "zombify %livingentities%",
                "unzombify %livingentities% [(in|after) %-timespan%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        zombify = matchedPattern == 0;
        if (!zombify && exprs[1] != null) {
            timespan = (Expression<Timespan>) exprs[1];
        }
        changeInPlace = ChangerUtils.acceptsChange(entities, ChangeMode.SET, LivingEntity.class);
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        int ticks = 0;
        if (timespan != null) {
            Timespan value = timespan.getSingle(event);
            if (value != null) {
                ticks = (int) value.getAs(TimePeriod.TICK);
            }
        }
        int finalTicks = ticks;
        Function<LivingEntity, LivingEntity> changeFunction = entity -> {
            if (zombify && entity instanceof Villager villager) {
                Object converted = EffectRuntimeSupport.invokeCompatible(villager, "zombify");
                if (converted instanceof LivingEntity livingEntity) {
                    return livingEntity;
                }
                Object reflective = EffectRuntimeSupport.invokeCompatible(villager, "convertToZombieType", villager.level());
                if (reflective instanceof LivingEntity livingEntity) {
                    return livingEntity;
                }
            } else if (!zombify && entity instanceof ZombieVillager zombieVillager) {
                zombieVillager.setVillagerConversionTime(finalTicks);
            }
            return entity;
        };
        if (changeInPlace) {
            entities.changeInPlace(event, changeFunction);
            return;
        }
        for (LivingEntity entity : entities.getAll(event)) {
            changeFunction.apply(entity);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append(zombify ? "zombify" : "unzombify");
        builder.append(entities);
        if (timespan != null) {
            builder.append("after", timespan);
        }
        return builder.toString();
    }
}

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Can See")
@Description("Checks whether the given players can see the provided entities.")
@Example("""
	if sender can't see the player-argument:
		message "who dat?"
	""")
@Example("""
	if the player can see the last spawned entity:
		message "hello there!"
	""")
@Since("2.3, 2.10 (entities)")
@RequiredPlugins("Minecraft 1.19+ (entities)")
public class CondCanSee extends Condition {

    static {
        Skript.registerCondition(
                CondCanSee.class,
                "%entities% (is|are) [visible|:invisible] for %players%",
                "%players% can see %entities%",
                "%entities% (is|are)(n't| not) [visible|:invisible] for %players%",
                "%players% can('t| not) see %entities%"
        );
    }

    private Expression<ServerPlayer> viewers;
    private Expression<Entity> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        if (matchedPattern == 1 || matchedPattern == 3) {
            viewers = (Expression<ServerPlayer>) exprs[0];
            entities = (Expression<Entity>) exprs[1];
        } else {
            entities = (Expression<Entity>) exprs[0];
            viewers = (Expression<ServerPlayer>) exprs[1];
        }
        setNegated(matchedPattern > 1 ^ result.hasTag("invisible"));
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return viewers.check(event, player -> entities.check(event, entity -> !entity.isInvisibleTo(player), false), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyType.CAN, event, debug, viewers,
                "see " + entities.toString(event, debug));
    }
}

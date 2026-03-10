package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Keep Inventory / Experience")
@Description("Keeps the inventory or/and experiences of the dead player in a death event.")
@Example("""
        on death of a player:
            if the victim is an op:
                keep the inventory and experiences
        """)
@Since("2.4")
@Events("death")
public final class EffKeepInventory extends Effect implements EventRestrictedSyntax {

    private static boolean registered;

    private boolean keepItems;
    private boolean keepExp;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffKeepInventory.class,
                "keep [the] (inventory|items) [(1:and [e]xp[erience][s] [point[s]])]",
                "keep [the] [e]xp[erience][s] [point[s]] [(1:and (inventory|items))]"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        keepItems = matchedPattern == 0 || parseResult.mark == 1;
        keepExp = matchedPattern == 1 || parseResult.mark == 1;
        if (!getParser().isCurrentEvent(FabricEffectEventHandles.EntityDeath.class)) {
            Skript.error("The keep inventory/experience effect can't be used outside of a death event");
            return false;
        }
        if (isDelayed.isTrue()) {
            Skript.error("Can't keep the inventory/experience anymore after the event has already passed");
            return false;
        }
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEffectEventHandles.EntityDeath.class};
    }

    @Override
    protected void execute(SkriptEvent event) {
        Object handle = event.handle();
        if (keepItems) {
            if (EffectRuntimeSupport.invokeCompatible(handle, new String[]{"setKeepInventory", "setKeepItems"}, true) == null) {
                EffectRuntimeSupport.setBooleanField(handle, "keepInventory", true);
            }
        }
        if (keepExp) {
            if (EffectRuntimeSupport.invokeCompatible(handle, new String[]{"setKeepLevel", "setKeepExperience"}, true) == null) {
                EffectRuntimeSupport.setBooleanField(handle, "keepLevel", true);
                EffectRuntimeSupport.setBooleanField(handle, "keepExperience", true);
            }
            EffectRuntimeSupport.invokeCompatible(handle, new String[]{"setDroppedExp", "setDroppedExperience"}, 0);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (keepItems && !keepExp) {
            return "keep the inventory";
        }
        return "keep the experience" + (keepItems ? " and inventory" : "");
    }
}

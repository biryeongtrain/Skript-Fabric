package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricLootGenerateEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Cancel Drops")
@Description({
        "Cancels drops of items in a death, block break, block drop, and block harvest events.",
        "The dropped experience can be cancelled in a death and block break events.",
        "Please note that using this in a death event doesn't keep items or experience of a dead player. If you want to do that, "
                + "use the <a href='#EffKeepInventory'>Keep Inventory / Experience</a> effect."
})
@Example("""
        on death of a zombie:
            if name of the entity is "&cSpecial":
                cancel drops of items
        """)
@Example("""
        on break of a coal ore:
            cancel the experience drops
        """)
@Example("""
        on player block harvest:
            cancel the item drops
        """)
@Since("2.4, 2.12 (harvest event)")
@RequiredPlugins("1.12.2 or newer (cancelling item drops of blocks)")
@Events({"death", "break / mine", "block drop", "harvest block"})
public final class EffCancelDrops extends Effect implements EventRestrictedSyntax {

    private static boolean registered;
    private boolean cancelItems;
    private boolean cancelExps;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffCancelDrops.class,
                "(cancel|clear|delete) [the] drops [of (items:items|xp:[e]xp[erience][s])]",
                "(cancel|clear|delete) [the] (items:item|xp:[e]xp[erience]) drops"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        cancelItems = !parseResult.hasTag("xp");
        cancelExps = !parseResult.hasTag("items");
        if (isDelayed.isTrue()) {
            Skript.error("Can't cancel the drops anymore after the event has already passed");
            return false;
        }
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricLootGenerateEventHandle.class, FabricBlockBreakHandle.class};
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.handle() instanceof FabricLootGenerateEventHandle handle && cancelItems) {
            handle.loot().clear();
            return;
        }
        if (event.handle() instanceof FabricBlockBreakHandle && (cancelItems || cancelExps)) {
            Skript.error("Cancelling block-break drops is not wired in the Fabric runtime yet");
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (cancelItems && !cancelExps) {
            return "cancel the drops of items";
        }
        if (cancelExps && !cancelItems) {
            return "cancel the drops of experiences";
        }
        return "cancel the drops";
    }
}

package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffPullHookedEntity extends Effect {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'pull hooked entity' effect can only be used in a fishing event.");
            return false;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return;
        }
        FishingHook hook = handle.hook();
        Entity hooked = hook.getHookedIn();
        if (hooked != null) {
            PrivateFishingHookAccess.pullEntity(hook, hooked);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "pull in hooked entity";
    }
}

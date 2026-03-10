package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("unchecked")
public class EvtSpectate extends SkriptEvent {

    private static final int STOP = -1;
    private static final int SWAP = 0;
    private static final int START = 1;

    private @Nullable Literal<EntityData<?>> datas;
    private int pattern;

    public static synchronized void register() {
        EntityData.register();
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(
                EvtSpectate.class,
                "[player] stop spectating [(of|from) %-*entitydatas%]",
                "[player] (swap|switch) spectating [(of|from) %-*entitydatas%]",
                "[player] start spectating [of %-*entitydatas%]"
        );
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtSpectate.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        pattern = matchedPattern - 1;
        datas = args.length > 0 ? (Literal<EntityData<?>>) args[0] : null;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.Spectate handle)) {
            return false;
        }
        if (pattern == START && handle.action() != FabricPlayerEventHandles.SpectateAction.START) {
            return false;
        }
        if (pattern == SWAP && handle.action() != FabricPlayerEventHandles.SpectateAction.SWAP) {
            return false;
        }
        if (pattern == STOP && handle.action() != FabricPlayerEventHandles.SpectateAction.STOP) {
            return false;
        }
        if (datas == null) {
            return true;
        }
        net.minecraft.world.entity.Entity entity = switch (handle.action()) {
            case START -> handle.newTarget();
            case SWAP, STOP -> handle.currentTarget();
        };
        if (entity == null) {
            return false;
        }
        for (EntityData<?> data : datas.getAll(null)) {
            if (data != null && data.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.Spectate.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        String action = pattern == START ? "start" : pattern == SWAP ? "swap" : "stop";
        return action + " spectating" + (datas != null ? " of " + datas.toString(event, debug) : "");
    }
}

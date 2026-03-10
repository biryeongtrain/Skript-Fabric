package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@SuppressWarnings("unchecked")
public class EvtMove extends SkriptEvent {

    private enum MoveType {
        MOVE("move"),
        MOVE_OR_ROTATE("move or rotate"),
        ROTATE("rotate");

        private final String name;

        MoveType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private EntityData<?> entityData;
    private boolean playerOnly;
    private MoveType moveType;

    public static synchronized void register() {
        EntityData.register();
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(
                EvtMove.class,
                "%entitydata% (move|walk|step|rotate:(turn[ing] around|rotate))",
                "%entitydata% (move|walk|step) or (turn[ing] around|rotate)",
                "%entitydata% (turn[ing] around|rotate) or (move|walk|step)"
        );
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtMove.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        entityData = ((Literal<EntityData<?>>) args[0]).getSingle(null);
        if (entityData == null) {
            return false;
        }
        playerOnly = ServerPlayer.class.isAssignableFrom(entityData.getType());
        if (matchedPattern > 0) {
            moveType = MoveType.MOVE_OR_ROTATE;
        } else if (parseResult.hasTag("rotate")) {
            moveType = MoveType.ROTATE;
        } else {
            moveType = MoveType.MOVE;
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.Move handle)) {
            return false;
        }
        if (!entityData.isInstance(handle.entity())) {
            return false;
        }
        if (playerOnly && !(handle.entity() instanceof ServerPlayer)) {
            return false;
        }
        return switch (moveType) {
            case MOVE -> hasChangedPosition(handle.from(), handle.to());
            case ROTATE -> hasChangedOrientation(handle);
            case MOVE_OR_ROTATE -> hasChangedPosition(handle.from(), handle.to()) || hasChangedOrientation(handle);
        };
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.Move.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return entityData + " " + moveType;
    }

    private static boolean hasChangedPosition(@Nullable FabricLocation from, @Nullable FabricLocation to) {
        if (from == null || to == null) {
            return false;
        }
        return from.level() != to.level()
                || from.position().x != to.position().x
                || from.position().y != to.position().y
                || from.position().z != to.position().z;
    }

    private static boolean hasChangedOrientation(FabricPlayerEventHandles.Move handle) {
        return handle.fromYaw() != handle.toYaw() || handle.fromPitch() != handle.toPitch();
    }
}

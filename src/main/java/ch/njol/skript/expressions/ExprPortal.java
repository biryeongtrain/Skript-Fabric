package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Portal")
@Description("The portal block or blocks associated with the current portal event.")
@Example("""
    on portal:
        loop portal blocks:
            broadcast \"%loop-block% is part of the active portal\"
    """)
@Since("2.4, 2.13 (Fabric)")
@Events("portal")
public class ExprPortal extends SimpleExpression<FabricBlock> {

    static {
        Skript.registerExpression(
                ExprPortal.class,
                FabricBlock.class,
                "[the] portal['s] blocks",
                "[the] blocks of [the] portal"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        if (getParser().isCurrentEvent(FabricEventCompatHandles.Portal.class)
                || ReflectiveHandleAccess.currentEventSupports("portalBlocks", "getPortalBlocks", "portalBlock", "getPortalBlock")) {
            return true;
        }
        Skript.error("The 'portal' expression may only be used in a portal event.");
        return false;
    }

    @Override
    protected FabricBlock @Nullable [] get(SkriptEvent event) {
        Object reflected = ReflectiveHandleAccess.invokeNoArg(event.handle(), "portalBlocks", "getPortalBlocks", "portalBlock", "getPortalBlock");
        if (reflected instanceof FabricBlock block) {
            return new FabricBlock[]{block};
        }
        if (reflected instanceof FabricBlock[] blocks) {
            return blocks.length == 0 ? null : blocks;
        }
        if (reflected instanceof Iterable<?> iterable) {
            java.util.List<FabricBlock> blocks = new java.util.ArrayList<>();
            for (Object value : iterable) {
                if (value instanceof FabricBlock block) {
                    blocks.add(block);
                }
            }
            return blocks.isEmpty() ? null : blocks.toArray(FabricBlock[]::new);
        }
        if (!(event.handle() instanceof FabricEventCompatHandles.Portal handle)
                || !(handle.entity() != null && handle.entity().level() instanceof ServerLevel level)) {
            return null;
        }
        return portalBlocks(level, handle.entity());
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the portal blocks";
    }

    static @Nullable FabricBlock[] portalBlocks(ServerLevel level, Entity entity) {
        BlockPos origin = entity.blockPosition();
        java.util.LinkedHashSet<BlockPos> positions = new java.util.LinkedHashSet<>();
        addIfPortal(level, positions, origin);
        addIfPortal(level, positions, origin.above());
        addIfPortal(level, positions, origin.below());
        return positions.isEmpty()
                ? null
                : positions.stream().map(position -> new FabricBlock(level, position)).toArray(FabricBlock[]::new);
    }

    private static void addIfPortal(ServerLevel level, java.util.Set<BlockPos> positions, BlockPos position) {
        var state = level.getBlockState(position);
        if (state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY)) {
            positions.add(position.immutable());
        }
    }
}

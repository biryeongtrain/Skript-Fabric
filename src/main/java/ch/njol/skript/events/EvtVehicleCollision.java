package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class EvtVehicleCollision extends SkriptEvent {

    private @Nullable Literal<?> expr;
    private boolean blockCollision;
    private boolean entityCollision;
    private final List<FabricItemType> itemTypes = new ArrayList<>();
    private final List<BlockState> blockStates = new ArrayList<>();
    private final List<EntityData<?>> entityDatas = new ArrayList<>();

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtVehicleCollision.class)) {
            return;
        }
        Skript.registerEvent(
                EvtVehicleCollision.class,
                "vehicle collision [(with|of) [a[n]] %-itemtypes/blockdatas/entitydatas%]",
                "vehicle block collision [(with|of) [a[n]] %-itemtypes/blockdatas%]",
                "vehicle entity collision [(with|of) [a[n]] %-entitydatas%]"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        expr = args[0];
        if (expr != null) {
            for (Object object : expr.getAll(null)) {
                if (object instanceof FabricItemType itemType) {
                    itemTypes.add(itemType);
                } else if (object instanceof BlockState blockState) {
                    blockStates.add(blockState);
                } else if (object instanceof EntityData<?> entityData) {
                    entityDatas.add(entityData);
                }
            }
        }
        blockCollision = matchedPattern == 1;
        entityCollision = matchedPattern == 2;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.VehicleCollision handle)) {
            return false;
        }
        if (blockCollision && handle.blockState() == null) {
            return false;
        }
        if (entityCollision && handle.entity() == null) {
            return false;
        }
        if (expr == null) {
            return true;
        }
        if (handle.blockState() != null && (!itemTypes.isEmpty() || !blockStates.isEmpty()) && matchesBlock(handle.blockState())) {
            return true;
        }
        return handle.entity() != null && !entityDatas.isEmpty() && matchesEntity(handle.entity());
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.VehicleCollision.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("vehicle");
        if (blockCollision) {
            builder.append("block");
        } else if (entityCollision) {
            builder.append("entity");
        }
        builder.append("collision");
        if (expr != null) {
            builder.append("of", expr);
        }
        return builder.toString();
    }

    private boolean matchesBlock(BlockState state) {
        Item item = state.getBlock().asItem();
        for (FabricItemType itemType : itemTypes) {
            if (item != null && itemType.matches(new ItemStack(item, Math.max(1, itemType.amount())))) {
                return true;
            }
        }
        for (BlockState blockState : blockStates) {
            if (state.equals(blockState)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesEntity(Entity entity) {
        for (EntityData<?> entityData : entityDatas) {
            if (entityData.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }
}

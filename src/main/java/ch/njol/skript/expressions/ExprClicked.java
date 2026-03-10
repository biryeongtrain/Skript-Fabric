package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Locale;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprClicked extends SimpleExpression<Object> implements EventRestrictedSyntax {

    static {
        Skript.registerExpression(
                ExprClicked.class,
                Object.class,
                "[the] clicked (block|%-*itemtype/entitydata%)"
        );
    }

    private @Nullable EntityData<?> entityType;
    private @Nullable FabricItemType itemType;
    private boolean anyEntity;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Object type = exprs.length == 0 || exprs[0] == null ? null : ((Literal<Object>) exprs[0]).getSingle(null);
        if (type instanceof EntityData<?> data) {
            entityType = data;
        } else if (type instanceof FabricItemType fabricItemType) {
            itemType = fabricItemType;
        } else {
            String raw = parseResult.expr == null ? "" : parseResult.expr.toLowerCase(Locale.ENGLISH);
            anyEntity = raw.contains("clicked entity");
        }
        return true;
    }

    @Override
    public Class<?>[] supportedEvents() {
        return new Class<?>[]{FabricEventCompatHandles.Click.class};
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Click handle)) {
            return null;
        }
        if (entityType != null || anyEntity) {
            Entity entity = handle.entity();
            if (entity == null) {
                return null;
            }
            if (entityType != null && !entityType.isInstance(entity)) {
                return null;
            }
            return new Entity[]{entity};
        }
        if (handle.blockState() == null) {
            return null;
        }
        FabricBlock block = new FabricBlock(handle.level(), handle.position());
        if (itemType == null) {
            return new FabricBlock[]{block};
        }
        ItemStack stack = new ItemStack(handle.blockState().getBlock().asItem());
        return itemType.isOfType(stack) ? new FabricBlock[]{block} : null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        if (entityType != null) {
            return entityType.getType();
        }
        return anyEntity ? Entity.class : FabricBlock.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (entityType != null) {
            return "clicked " + entityType;
        }
        if (anyEntity) {
            return "clicked entity";
        }
        return "clicked " + (itemType == null ? "block" : itemType);
    }
}

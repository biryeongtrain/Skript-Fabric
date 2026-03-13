package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Type of")
@Description("The type of an entity or item.")
@Example("message \"This is a %type of player's tool%\"")
@Since("1.4, Fabric")
public final class ExprTypeOf extends SimplePropertyExpression<Object, Object> {

    private Class<?>[] returnTypes = new Class<?>[0];
    private Class<?> superReturnType = Object.class;

    static {
        register(ExprTypeOf.class, Object.class, "type", "entities/entitydatas/itemtypes/itemstacks");
        Skript.registerExpression(
                (Class) ExprTypeOf.class,
                EntityData.class,
                PropertyExpression.getPatterns("type", "entities/entitydatas")
        );
        Skript.registerExpression(
                (Class) ExprTypeOf.class,
                FabricItemType.class,
                PropertyExpression.getPatterns("type", "itemtypes/itemstacks")
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        List<Class<?>> possibleTypes = new ArrayList<>();
        if (expressions[0].canReturn(Entity.class) || expressions[0].canReturn(EntityData.class)) {
            possibleTypes.add(EntityData.class);
        }
        if (expressions[0].canReturn(FabricItemType.class) || expressions[0].canReturn(ItemStack.class)) {
            possibleTypes.add(FabricItemType.class);
        }
        returnTypes = possibleTypes.toArray(Class[]::new);
        superReturnType = returnTypes.length == 1 ? returnTypes[0] : Object.class;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Object convert(Object object) {
        if (object instanceof Entity entity) {
            return EntityData.fromEntity(entity);
        }
        if (object instanceof EntityData<?> entityData) {
            return entityData;
        }
        if (object instanceof FabricItemType itemType) {
            return new FabricItemType(itemType.item());
        }
        if (object instanceof ItemStack itemStack) {
            return new FabricItemType(itemStack.getItem());
        }
        return null;
    }

    @Override
    public Class<?> getReturnType() {
        return superReturnType;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return returnTypes;
    }

    @Override
    protected String getPropertyName() {
        return "type";
    }
}

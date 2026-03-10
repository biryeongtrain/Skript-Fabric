package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Raw Name")
@Description("The raw Minecraft material name of the given item. Note that this is not guaranteed to give same results on all servers.")
@Example("raw name of tool of player")
@Since("unknown (2.2)")
public class ExprRawName extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprRawName.class, String.class, "(raw|minecraft|vanilla) name[s] of %itemtypes%");
    }

    private Expression<FabricItemType> types;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        types = (Expression<FabricItemType>) exprs[0];
        return true;
    }

    @Override
    protected @Nullable String[] get(SkriptEvent event) {
        return Arrays.stream(types.getAll(event))
                .map(FabricItemType::item)
                .map(BuiltInRegistries.ITEM::getKey)
                .map(MinecraftResourceParser::display)
                .toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return types.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "minecraft name of " + types.toString(event, debug);
    }
}

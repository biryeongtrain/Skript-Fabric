package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Sign Text")
@Description("A line of text on a sign. Can be changed using the live sign block state.")
@Example("""
    set line 2 of target block to "%player%"
    send line 1 of target block
    """)
@Since("1.3, Fabric")
public class ExprSignText extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                ExprSignText.class,
                String.class,
                "[the] line %number% of %blocks%",
                "[the] (1¦1st|1¦first|2¦2nd|2¦second|3¦3rd|3¦third|4¦4th|4¦fourth) line of %blocks%"
        );
    }

    private Expression<Number> line;
    private Expression<FabricBlock> block;

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern == 0) {
            line = (Expression<Number>) expressions[0];
            block = (Expression<FabricBlock>) expressions[1];
        } else {
            line = new SimpleLiteral<>(parseResult.mark, false);
            block = (Expression<FabricBlock>) expressions[0];
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Number selectedLine = line.getSingle(event);
        if (selectedLine == null) {
            return new String[0];
        }
        int index = selectedLine.intValue() - 1;
        if (index < 0 || index > 3) {
            return new String[0];
        }
        FabricBlock signBlock = block.getSingle(event);
        if (signBlock == null || !(signBlock.level().getBlockEntity(signBlock.position()) instanceof SignBlockEntity sign)) {
            return new String[0];
        }
        return new String[]{sign.getFrontText().getMessage(index, false).getString()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case DELETE, SET -> new Class[]{String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
        Number selectedLine = line.getSingle(event);
        if (selectedLine == null) {
            return;
        }
        int index = selectedLine.intValue() - 1;
        if (index < 0 || index > 3) {
            return;
        }
        Component updatedValue = mode == ChangeMode.DELETE
                ? Component.empty()
                : SkriptTextPlaceholders.resolveComponent(String.valueOf(delta[0]), event);
        for (FabricBlock signBlock : block.getArray(event)) {
            if (!(signBlock.level().getBlockEntity(signBlock.position()) instanceof SignBlockEntity sign)) {
                continue;
            }
            SignText updated = sign.getFrontText().setMessage(index, updatedValue);
            sign.setText(updated, true);
            sign.setChanged();
            signBlock.level().sendBlockUpdated(signBlock.position(), signBlock.state(), signBlock.state(), 3);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "line " + line.toString(event, debug) + " of " + block.toString(event, debug);
    }
}

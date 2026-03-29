package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Command Block Command")
@Description(
        "Gets or sets the command associated with a command block or minecart with command block."
)
@Example("send command of {_block}")
@Example("set command of {_cmdMinecart} to \"say asdf\"")
@Since("2.10")
public class ExprCommandBlockCommand extends SimplePropertyExpression<Object, String> {

    static {
        register(ExprCommandBlockCommand.class, String.class, "[command[ ]block] command", "blocks/entities");
    }

    @Override
    public @Nullable String convert(Object holder) {
        BaseCommandBlock commandBlock = commandBlock(holder);
        if (commandBlock == null) {
            return null;
        }
        String command = commandBlock.getCommand();
        return command.isEmpty() ? null : command;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
            return new Class[]{String.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        String newCommand = delta == null ? "" : (String) delta[0];
        for (Object holder : getExpr().getArray(event)) {
            BaseCommandBlock commandBlock = commandBlock(holder);
            if (commandBlock == null) {
                continue;
            }
            switch (mode) {
                case RESET, DELETE, SET -> commandBlock.setCommand(newCommand);
            }
        }
    }

    private @Nullable BaseCommandBlock commandBlock(Object holder) {
        if (holder instanceof FabricBlock block && block.block() instanceof CommandBlock) {
            if (block.level().getBlockEntity(block.position()) instanceof CommandBlockEntity entity) {
                return entity.getCommandBlock();
            }
            return null;
        }
        if (holder instanceof MinecartCommandBlock minecart) {
            return minecart.getCommandBlock();
        }
        return null;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "command block command";
    }
}

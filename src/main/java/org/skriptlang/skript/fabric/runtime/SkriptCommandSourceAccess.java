package org.skriptlang.skript.fabric.runtime;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public enum SkriptCommandSourceAccess implements SkriptCommandTree.SourceAccess<CommandSourceStack> {
    INSTANCE;

    @Override
    public boolean canUse(CommandSourceStack source) {
        return source.hasPermission(2);
    }

    @Override
    public void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }

    @Override
    public void failure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
    }
}

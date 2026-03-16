package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.expressions.ExprCommand;
import ch.njol.skript.expressions.ExprCommandSender;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventPlayer;
import org.skriptlang.skript.bukkit.base.expressions.ExprEventEntity;

final class PlayerEventBindingTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void playerBackedLiveHandlesBindEventPlayer() {
        assertInstanceOf(ExprEventPlayer.class, parseExpressionInEvent("event-player", resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$Command")));
        assertInstanceOf(ExprEventPlayer.class, parseExpressionInEvent("event-player", resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$Level")));
        assertInstanceOf(ExprEventPlayer.class, parseExpressionInEvent("event-player", resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$ExperienceChange")));
        assertInstanceOf(ExprEventPlayer.class, parseExpressionInEvent("event-player", resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$Spectate")));
    }

    @Test
    void teleportHandleBindsEventEntity() {
        assertInstanceOf(ExprEventEntity.class, parseExpressionInEvent("event-entity", resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$Teleport")));
    }

    @Disabled("Moved to GameTest")
    @Test
    void commandHandleBindsCommandExpressions() {
        Class<?> commandEventClass = resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        assertInstanceOf(ExprCommand.class, parseExpressionInEvent("full command", new Class[]{String.class}, commandEventClass));
        assertInstanceOf(ExprCommandSender.class, parseExpressionInEvent("command sender", new Class[]{ServerPlayer.class}, commandEventClass));
    }

    private Expression<?> parseExpressionInEvent(String input, Class<?>... eventClasses) {
        return parseExpressionInEvent(input, new Class[]{Object.class}, eventClasses);
    }

    private Expression<?> parseExpressionInEvent(String input, Class<?>[] returnTypes, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("player-event", eventClasses);
            Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                    .parseExpression(returnTypes);
            assertNotNull(parsed, input);
            return parsed;
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }

    private static Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

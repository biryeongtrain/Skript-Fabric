package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Title - Send")
@Description({
        "Sends a title or subtitle to the given player(s) with optional fadein, stay, and fadeout times.",
        "If no input is given for the times, the client keeps the last configured title timing."
})
@Example("send title \"Competition Started\" with subtitle \"Have fun\" to player for 5 seconds")
@Example("send subtitle \"Party!\" to all players")
@Since("2.3")
public class EffSendTitle extends Effect {

    private static boolean registered;

    private @Nullable Expression<String> title;
    private @Nullable Expression<String> subtitle;
    private @Nullable Expression<ServerPlayer> recipients;
    private @Nullable Expression<Timespan> fadeIn;
    private @Nullable Expression<Timespan> stay;
    private @Nullable Expression<Timespan> fadeOut;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffSendTitle.class,
                "send title %string% [with subtitle %-string%] [to %-players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]",
                "send subtitle %string% [to %-players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        title = matchedPattern == 0 ? (Expression<String>) expressions[0] : null;
        subtitle = (Expression<String>) expressions[1 - matchedPattern];
        recipients = (Expression<ServerPlayer>) expressions[2 - matchedPattern];
        stay = (Expression<Timespan>) expressions[3 - matchedPattern];
        fadeIn = (Expression<Timespan>) expressions[4 - matchedPattern];
        fadeOut = (Expression<Timespan>) expressions[5 - matchedPattern];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Component titleComponent = title == null ? null : EffectRuntimeSupport.componentOf(title.getSingle(event), event);
        Component subtitleComponent = subtitle == null ? null : EffectRuntimeSupport.componentOf(subtitle.getSingle(event), event);
        boolean sendAnimation = stay != null || fadeIn != null || fadeOut != null;
        int resolvedFadeIn = ticks(fadeIn, event, 10);
        int resolvedStay = ticks(stay, event, 70);
        int resolvedFadeOut = ticks(fadeOut, event, 20);

        for (ServerPlayer player : EffectRuntimeSupport.playersOrEvent(recipients == null ? null : recipients.getArray(event), event)) {
            if (sendAnimation) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(resolvedFadeIn, resolvedStay, resolvedFadeOut));
            }
            if (titleComponent != null) {
                player.connection.send(new ClientboundSetTitleTextPacket(titleComponent));
            }
            if (subtitleComponent != null) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitleComponent));
            }
        }
    }

    private int ticks(@Nullable Expression<Timespan> expression, SkriptEvent event, int defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        Timespan value = expression.getSingle(event);
        return value == null ? defaultValue : (int) value.getAs(Timespan.TimePeriod.TICK);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder("send ");
        if (title == null) {
            builder.append("subtitle ").append(subtitle == null ? "" : subtitle.toString(event, debug));
        } else {
            builder.append("title ").append(title.toString(event, debug));
            if (subtitle != null) {
                builder.append(" with subtitle ").append(subtitle.toString(event, debug));
            }
        }
        if (recipients != null) {
            builder.append(" to ").append(recipients.toString(event, debug));
        }
        return builder.toString();
    }
}

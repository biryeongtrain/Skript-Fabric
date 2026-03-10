package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLastLoginTime extends SimpleExpression<Date> {

    static {
        Skript.registerExpression(ExprLastLoginTime.class, Date.class,
                "(1¦last|2¦first) login of %offlineplayers%",
                "%offlineplayers%'[s] (1¦last|2¦first) login");
    }

    private Expression<GameProfile> players;
    private boolean first;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<GameProfile>) exprs[0];
        first = parseResult.mark == 2;
        return true;
    }

    @Override
    protected Date @Nullable [] get(SkriptEvent event) {
        if (ExpressionRuntimeSupport.resolveServer(event) == null) {
            return new Date[0];
        }
        return players.stream(event)
                .map(profile -> resolveLoginDate(ExpressionRuntimeSupport.playerDataFile(event, profile), first))
                .filter(java.util.Objects::nonNull)
                .toArray(Date[]::new);
    }

    static @Nullable Date resolveLoginDate(@Nullable Path file, boolean first) {
        FileTime time = resolveLoginTime(file, first);
        return time == null ? null : new Date(time.toMillis());
    }

    static @Nullable FileTime resolveLoginTime(@Nullable Path file, boolean first) {
        if (file == null) {
            return null;
        }
        if (!Files.exists(file)) {
            return null;
        }
        try {
            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
            FileTime creation = attributes.creationTime();
            if (first && creation != null && creation.toMillis() > 0L) {
                return creation;
            }
            return first ? attributes.lastModifiedTime() : attributes.lastModifiedTime();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read player data timestamps for " + file, exception);
        }
    }

    @Override
    public boolean isSingle() {
        return players.isSingle();
    }

    @Override
    public Class<? extends Date> getReturnType() {
        return Date.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (first ? "first" : "last") + " login of " + players.toString(event, debug);
    }
}

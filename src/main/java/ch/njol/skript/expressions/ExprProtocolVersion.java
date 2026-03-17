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
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Protocol Version")
@Description({
        "The protocol version number of the server.",
        "When used in a server list ping event, the 'fake' modifier allows changing the displayed protocol version."
})
@Example("send protocol version")
@Example("""
        on server list ping:
            set the fake protocol version to 0
        """)
@Since("2.3, Fabric")
public class ExprProtocolVersion extends SimpleExpression<Long> {

    private static final int MODE_DEFAULT = 0;
    private static final int MODE_SENT = 1;
    private static final int MODE_REQUIRED = 2;
    private static final int MODE_FAKE = 3;

    static {
        Skript.registerExpression(
                ExprProtocolVersion.class,
                Long.class,
                "[the] [(1:sent|2:required|3:fake)] [server] protocol version [number]"
        );
    }

    private int mode;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        mode = parseResult.mark;
        return true;
    }

    @Override
    protected @Nullable Long[] get(SkriptEvent event) {
        if (mode == MODE_FAKE) {
            Object handle = event.handle();
            if (handle instanceof FabricServerListPingEventHandle pingHandle) {
                return new Long[]{(long) pingHandle.protocolVersion()};
            }
        }
        return new Long[]{(long) SharedConstants.getProtocolVersion()};
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode changeMode) {
        if (mode == MODE_FAKE && changeMode == ChangeMode.SET) {
            return new Class[]{Number.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode changeMode) {
        if (mode != MODE_FAKE || delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
            return;
        }
        Object handle = event.handle();
        if (handle instanceof FabricServerListPingEventHandle pingHandle) {
            pingHandle.setProtocolVersion(number.intValue());
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (mode) {
            case MODE_SENT -> "the sent protocol version";
            case MODE_REQUIRED -> "the required protocol version";
            case MODE_FAKE -> "the fake protocol version";
            default -> "the protocol version";
        };
    }
}

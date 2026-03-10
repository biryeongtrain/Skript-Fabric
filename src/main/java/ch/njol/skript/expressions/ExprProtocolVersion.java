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
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Protocol Version")
@Description("The current server protocol version on the Fabric compatibility surface.")
@Example("send protocol version")
@Since("2.3, Fabric")
public class ExprProtocolVersion extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprProtocolVersion.class, Long.class, "[the] [server] protocol version [number]");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected @Nullable Long[] get(SkriptEvent event) {
        return new Long[]{(long) SharedConstants.getProtocolVersion()};
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
        return "the protocol version";
    }
}

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.util.Kleenean;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;

public class ExprHash extends PropertyExpression<String, String> {

    private static final HexFormat HEX = HexFormat.of().withLowerCase();

    static {
        Skript.registerExpression(ExprHash.class, String.class,
                "%strings% hash[ed] with (:(MD5|SHA-256|SHA-384|SHA-512))");
    }

    private MessageDigest digest;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends String>) exprs[0]);
        String algorithm = parseResult.tags.get(0).toUpperCase(Locale.ENGLISH);
        try {
            digest = MessageDigest.getInstance(algorithm);
            Script currentScript = getParser().getCurrentScript();
            if ("MD5".equals(algorithm)
                    && currentScript != null
                    && !currentScript.suppressesWarning(ScriptWarning.DEPRECATED_SYNTAX)) {
                Skript.warning("MD5 is not secure and shouldn't be used if a cryptographically secure hashing algorithm is required.");
            }
            return true;
        } catch (NoSuchAlgorithmException exception) {
            Skript.error("Unsupported hashing algorithm: " + algorithm);
            return false;
        }
    }

    @Override
    protected String[] get(SkriptEvent event, String[] source) {
        String[] result = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = HEX.formatHex(digest.digest(source[i].getBytes(StandardCharsets.UTF_8)));
        }
        return result;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (getExpr() instanceof Literal<? extends String>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "hash of " + getExpr().toString(event, debug);
    }
}

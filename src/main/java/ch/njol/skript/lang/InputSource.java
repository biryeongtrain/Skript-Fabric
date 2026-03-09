package ch.njol.skript.lang;

import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.util.LiteralUtils;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * An InputSource represents a syntax that can provide a value for {@link ExprInput}.
 */
public interface InputSource {

    Set<ExprInput<?>> getDependentInputs();

    @Nullable Object getCurrentValue();

    default boolean hasIndices() {
        return false;
    }

    default @UnknownNullability String getCurrentIndex() {
        return null;
    }

    default @Nullable Expression<?> parseExpression(String expr, ParserInstance parser, int flags) {
        InputData inputData = parser.getData(InputData.class);
        InputSource originalSource = inputData.getSource();
        inputData.setSource(this);
        try {
            Expression<?> mappingExpr = ParserInstance.withInstance(parser, () -> {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Expression<?> parsed = new SkriptParser(expr, flags, ParseContext.DEFAULT)
                        .parseExpression(new Class[]{Object.class});
                return parsed;
            });
            if (mappingExpr != null && LiteralUtils.hasUnparsedLiteral(mappingExpr)) {
                mappingExpr = LiteralUtils.defendExpression(mappingExpr);
                if (!LiteralUtils.canInitSafely(mappingExpr)) {
                    return null;
                }
            }
            return mappingExpr;
        } finally {
            inputData.setSource(originalSource);
        }
    }

    class InputData extends ParserInstance.Data {

        private @Nullable InputSource source;

        public InputData(ParserInstance parserInstance) {
            super(parserInstance);
        }

        public void setSource(@Nullable InputSource source) {
            this.source = source;
        }

        public @Nullable InputSource getSource() {
            return source;
        }
    }
}

package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ReturnHandler;
import ch.njol.skript.lang.SectionExitHandler;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Return")
@Description("Makes a trigger or a section (e.g. a function) return a value")
@Example("""
        function double(i: number) :: number:
            return 2 * {_i}
        """)
@Since("2.2")
public class EffReturn extends Effect {

    private static boolean registered;

    private @UnknownNullability ReturnHandler<?> handler;
    private @UnknownNullability Expression<?> value;
    private @UnknownNullability List<SectionExitHandler> sectionsToExit;
    private int breakLevels;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffReturn.class, "return %objects%");
        if (!ParserInstance.isRegistered(ReturnHandler.ReturnHandlerStack.class)) {
            ParserInstance.registerData(ReturnHandler.ReturnHandlerStack.class, ReturnHandler.ReturnHandlerStack::new);
        }
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ParserInstance parser = getParser();
        handler = parser.getData(ReturnHandler.ReturnHandlerStack.class).getCurrentHandler();
        if (handler == null) {
            Skript.error("The return statement cannot be used here");
            return false;
        }
        if (!isDelayed.isFalse()) {
            Skript.error("A return statement after a delay is useless, as the calling trigger will resume when the delay starts (and won't get any returned value)");
            return false;
        }

        Class<?> returnType = handler.returnValueType();
        if (returnType == null) {
            Skript.error(handler + " doesn't return any value. Please use 'stop' or 'exit' if you want to stop the trigger.");
            return false;
        }

        RetainingLogHandler log = SkriptLogger.startRetainingLog();
        Expression<?> convertedExpr;
        try {
            convertedExpr = exprs[0].getConvertedExpression(returnType);
            if (convertedExpr == null && exprs[0] instanceof ch.njol.skript.lang.Variable<?> variable
                    && variable.getReturnType() == Object.class) {
                // Allow Object-typed variables (e.g. {_none}) in typed return statements.
                // At runtime the variable may be null (unset), which is valid for early return.
                convertedExpr = exprs[0];
            }
            if (convertedExpr == null) {
                // Don't print errors here — let the parsing framework try other syntax elements
                // (e.g. EffDoIf with "return X if Y") before reporting errors.
                // Returning false silently allows EffDoIf to handle the statement.
                log.clear();
                return false;
            }
            // Don't propagate intermediate conversion errors — the conversion succeeded
            // (possibly via Variable bypass) so discard any error-level log entries.
            log.clear();
        } finally {
            log.stop();
        }

        if (handler.isSingleReturnValue() && !convertedExpr.isSingle()) {
            String typeName = Classes.getSuperClassInfo(returnType).getName().getSingular();
            System.out.println("[DEBUG EffReturn] isSingleReturnValue=" + handler.isSingleReturnValue() + " convertedExpr.isSingle=" + convertedExpr.isSingle() + " for " + exprs[0]);
            Skript.error(handler + " is defined to only return a single " + typeName + ", but this return statement can return multiple values.");
            return false;
        }
        value = convertedExpr;

        if (handler instanceof TriggerSection handlerSection) {
            List<TriggerSection> innerSections = parser.getSectionsUntil(handlerSection);
            innerSections.add(0, handlerSection);
            breakLevels = innerSections.size();
            if (parser.getCurrentSections().size() < breakLevels) {
                breakLevels = -1;
            }
            sectionsToExit = innerSections.stream()
                    .filter(SectionExitHandler.class::isInstance)
                    .map(SectionExitHandler.class::cast)
                    .toList();
        } else {
            // Handler is not a TriggerSection (e.g. ScriptFunction) — stop entire trigger
            breakLevels = -1;
            sectionsToExit = parser.getCurrentSections().stream()
                    .filter(SectionExitHandler.class::isInstance)
                    .map(SectionExitHandler.class::cast)
                    .toList();
        }
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        debug(event, false);
        ((ReturnHandler) handler).returnValues(event, value);
        for (SectionExitHandler section : sectionsToExit) {
            section.exit(event);
        }
        if (handler instanceof TriggerSection handlerSection) {
            return handlerSection.getNext();
        }
        return null;
    }

    @Override
    protected void execute(SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExecutionIntent executionIntent() {
        if (breakLevels == -1) {
            return ExecutionIntent.stopTrigger();
        }
        return ExecutionIntent.stopSections(breakLevels);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "return " + value.toString(event, debug);
    }
}

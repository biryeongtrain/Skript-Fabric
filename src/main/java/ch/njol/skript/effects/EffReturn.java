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
            if (convertedExpr == null) {
                String typeName = Classes.getSuperClassInfo(returnType).getName().withIndefiniteArticle();
                log.printErrors(handler + " is declared to return " + typeName + ", but " + exprs[0].toString(null, false) + " is not of that type.");
                return false;
            }
            log.printLog();
        } finally {
            log.stop();
        }

        if (handler.isSingleReturnValue() && !convertedExpr.isSingle()) {
            String typeName = Classes.getSuperClassInfo(returnType).getName().getSingular();
            Skript.error(handler + " is defined to only return a single " + typeName + ", but this return statement can return multiple values.");
            return false;
        }
        value = convertedExpr;

        List<TriggerSection> innerSections = parser.getSectionsUntil((TriggerSection) handler);
        innerSections.add(0, (TriggerSection) handler);
        breakLevels = innerSections.size();
        if (parser.getCurrentSections().size() < breakLevels) {
            breakLevels = -1;
        }
        sectionsToExit = innerSections.stream()
                .filter(SectionExitHandler.class::isInstance)
                .map(SectionExitHandler.class::cast)
                .toList();
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        debug(event, false);
        ((ReturnHandler) handler).returnValues(event, value);
        for (SectionExitHandler section : sectionsToExit) {
            section.exit(event);
        }
        return ((TriggerSection) handler).getNext();
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

package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputEventHandle;

public final class EvtPlayerInput extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on player input",
            "on [player] (toggle|toggling) of (%-inputkeys%|(an|any) input key)",
            "on [player] press[ing] of (%-inputkeys%|(an|any) input key)",
            "on [player] (release|releasing) of (%-inputkeys%|(an|any) input key)",
            "on [player] input key (toggle|toggling)",
            "on [player] input key press[ing]",
            "on [player] input key (release|releasing)",
            "on [player] %-inputkeys% (toggle|toggling)",
            "on [player] %-inputkeys% press[ing]",
            "on [player] %-inputkeys% (release|releasing)"
    };

    private boolean unfiltered;
    private boolean requireAll;
    private @Nullable InputKey[] keysToCheck;
    private InputType type = InputType.TOGGLE;

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (matchedPattern == 0) {
            unfiltered = true;
            keysToCheck = null;
            requireAll = false;
            type = InputType.TOGGLE;
            return true;
        }
        unfiltered = false;
        keysToCheck = parseKeys(args.length > 0 ? args[0] : null);
        requireAll = args.length > 0 && args[0] != null && args[0].getAnd();
        if (args.length > 0 && args[0] != null && keysToCheck == null) {
            return false;
        }
        type = switch (matchedPattern) {
            case 1, 4, 7 -> InputType.TOGGLE;
            case 2, 5, 8 -> InputType.PRESS;
            case 3, 6, 9 -> InputType.RELEASE;
            default -> throw new IllegalStateException("Unexpected player input pattern index: " + matchedPattern);
        };
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerInputEventHandle handle)) {
            return false;
        }
        if (unfiltered) {
            return true;
        }
        Set<InputKey> previousKeys = Set.of(InputKey.fromInput(handle.previousInput()));
        Set<InputKey> currentKeys = Set.of(InputKey.fromInput(handle.currentInput()));
        Set<InputKey> requestedKeys = keysToCheck != null ? Set.of(keysToCheck) : null;
        return type.checkInputKeys(previousKeys, currentKeys, requestedKeys, requireAll);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerInputEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        if (unfiltered) {
            return "on player input";
        }
        if (keysToCheck == null) {
            return "on player " + type.name().toLowerCase() + " any input key";
        }
        return "on player " + type.name().toLowerCase() + " " + Classes.toString(keysToCheck, false);
    }

    private static @Nullable InputKey[] parseKeys(@Nullable Literal<?> literal) {
        if (literal == null) {
            return null;
        }
        List<InputKey> parsed = new ArrayList<>();
        for (Object raw : literal.getArray(null)) {
            if (raw == null) {
                continue;
            }
            InputKey key = raw instanceof InputKey direct ? direct : InputKey.parse(String.valueOf(raw));
            if (key == null) {
                return null;
            }
            parsed.add(key);
        }
        return parsed.toArray(InputKey[]::new);
    }

    private enum InputType {

        TOGGLE {
            @Override
            boolean checkKeyState(boolean inPrevious, boolean inCurrent) {
                return inPrevious != inCurrent;
            }
        },
        PRESS {
            @Override
            boolean checkKeyState(boolean inPrevious, boolean inCurrent) {
                return !inPrevious && inCurrent;
            }
        },
        RELEASE {
            @Override
            boolean checkKeyState(boolean inPrevious, boolean inCurrent) {
                return inPrevious && !inCurrent;
            }
        };

        abstract boolean checkKeyState(boolean inPrevious, boolean inCurrent);

        boolean checkInputKeys(Set<InputKey> previous, Set<InputKey> current, @Nullable Set<InputKey> keysToCheck, boolean and) {
            if (keysToCheck == null) {
                return switch (this) {
                    case TOGGLE -> true;
                    case PRESS -> previous.size() <= current.size();
                    case RELEASE -> previous.size() >= current.size();
                };
            }
            for (InputKey key : keysToCheck) {
                boolean inPrevious = previous.contains(key);
                boolean inCurrent = current.contains(key);
                if (and && !checkKeyState(inPrevious, inCurrent)) {
                    return false;
                }
                if (!and && checkKeyState(inPrevious, inCurrent)) {
                    return true;
                }
            }
            return and;
        }
    }
}

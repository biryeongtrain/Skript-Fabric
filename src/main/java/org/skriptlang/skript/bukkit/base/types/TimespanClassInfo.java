package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public final class TimespanClassInfo {

    private static final Pattern SIMPLE_TIMESPAN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s+([a-zA-Z]+)\\s*$");

    private TimespanClassInfo() {
    }

    public static void register() {
        ClassInfo<Timespan> info = new ClassInfo<>(Timespan.class, "timespan");
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<Timespan> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable Timespan parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            Matcher matcher = SIMPLE_TIMESPAN.matcher(input.trim().toLowerCase(Locale.ENGLISH));
            if (!matcher.matches()) {
                return null;
            }
            double amount;
            try {
                amount = Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException exception) {
                return null;
            }
            Timespan.TimePeriod period = Timespan.TimePeriod.fromToken(matcher.group(2));
            if (period == null) {
                return null;
            }
            return new Timespan(Math.round(amount * period.millis()));
        }
    }
}

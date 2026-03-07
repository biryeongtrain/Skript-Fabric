package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceTypeSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprDamageType extends SimpleExpression<String> {

    private Expression<DamageSource> sources;
    private boolean sectionContext;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length == 0) {
            if (!getParser().isCurrentEvent(DamageSourceSectionContext.class)) {
                Skript.error("The event-only damage type expression can only be used in a custom damage source section.");
                return false;
            }
            sectionContext = true;
            return true;
        }
        if (expressions.length != 1 || !expressions[0].canReturn(DamageSource.class)) {
            return false;
        }
        sources = (Expression<DamageSource>) expressions[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (sectionContext) {
            if (event.handle() instanceof DamageSourceSectionContext context) {
                DamageSource preview = context.build();
                return new String[]{DamageSourceTypeSupport.display(preview)};
            }
            return new String[0];
        }
        List<String> values = new ArrayList<>();
        for (DamageSource source : sources.getAll(event)) {
            values.add(DamageSourceTypeSupport.display(source));
        }
        return values.toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return sectionContext || sources.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (!sectionContext) {
            Skript.error("You cannot change the attributes of a damage source outside a custom damage source section.");
            return null;
        }
        return mode == ChangeMode.SET ? new Class[]{String.class, DamageSource.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || !(event.handle() instanceof DamageSourceSectionContext context) || delta == null || delta.length == 0) {
            return;
        }
        Holder<DamageType> holder = DamageSourceTypeSupport.parseHolder(delta[0], event.level());
        if (holder == null) {
            Skript.error("Unsupported damage type value.");
            return;
        }
        context.damageType(holder);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return sectionContext ? "damage type" : "damage type of " + sources.toString(event, debug);
    }
}

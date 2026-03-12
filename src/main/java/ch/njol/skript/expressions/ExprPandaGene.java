package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Panda;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprPandaGene extends SimplePropertyExpression<LivingEntity, Panda.Gene> {

    static {
        register(ExprPandaGene.class, Panda.Gene.class, "(:main|hidden) gene[s]", "livingentities");
    }

    private boolean mainGene;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        mainGene = parseResult.hasTag("main");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Panda.Gene convert(LivingEntity entity) {
        if (!(entity instanceof Panda panda)) {
            return null;
        }
        return mainGene ? panda.getMainGene() : panda.getHiddenGene();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Panda.Gene.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(delta != null && delta.length > 0 && delta[0] instanceof Panda.Gene gene)) {
            return;
        }
        for (LivingEntity entity : getExpr().getArray(event)) {
            if (!(entity instanceof Panda panda)) {
                continue;
            }
            if (mainGene) {
                panda.setMainGene(gene);
            } else {
                panda.setHiddenGene(gene);
            }
        }
    }

    @Override
    public Class<? extends Panda.Gene> getReturnType() {
        return Panda.Gene.class;
    }

    @Override
    protected String getPropertyName() {
        return mainGene ? "main gene" : "hidden gene";
    }
}

package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBlockSound extends SimpleExpression<String> {

    private enum SoundKind {
        BREAK,
        FALL,
        HIT,
        PLACE,
        STEP
    }

    static {
        SimplePropertyExpression.register(ExprBlockSound.class, String.class,
                "(1:break|2:fall|3:hit|4:place|5:step) sound[s]",
                "blocks/blockstates/itemtypes");
    }

    private SoundKind soundKind;
    private Expression<?> objects;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        soundKind = SoundKind.values()[parseResult.mark - 1];
        objects = exprs[0];
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        return objects.stream(event)
                .map(this::sound)
                .filter(Objects::nonNull)
                .map(BuiltInRegistries.SOUND_EVENT::getKey)
                .filter(Objects::nonNull)
                .map(ResourceLocation::toString)
                .distinct()
                .toArray(String[]::new);
    }

    private @Nullable SoundEvent sound(Object object) {
        SoundType soundType = null;
        if (object instanceof FabricBlock block) {
            soundType = block.state().getSoundType();
        } else if (object instanceof BlockState state) {
            soundType = state.getSoundType();
        } else if (object instanceof FabricItemType itemType && itemType.item() instanceof net.minecraft.world.item.BlockItem blockItem) {
            soundType = blockItem.getBlock().defaultBlockState().getSoundType();
        }
        if (soundType == null) {
            return null;
        }
        return switch (soundKind) {
            case BREAK -> soundType.getBreakSound();
            case FALL -> soundType.getFallSound();
            case HIT -> soundType.getHitSound();
            case PLACE -> soundType.getPlaceSound();
            case STEP -> soundType.getStepSound();
        };
    }

    @Override
    public boolean isSingle() {
        return objects.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
}

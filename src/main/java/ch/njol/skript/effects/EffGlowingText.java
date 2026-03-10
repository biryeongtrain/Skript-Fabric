package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Sign Glow")
@Description("Makes a sign (either a block or item) have glowing text or normal text")
@Example("make target block of player have glowing text")
@Since("2.8.0")
public class EffGlowingText extends Effect {

    private static boolean registered;

    private Expression<?> objects;
    private boolean glowing;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffGlowingText.class,
                "make %blocks/itemtypes% have glowing text",
                "make %blocks/itemtypes% have (normal|non[-| ]glowing) text"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        objects = exprs[0];
        glowing = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Object object : objects.getArray(event)) {
            if (object instanceof FabricBlock block) {
                BlockEntity blockEntity = block.level().getBlockEntity(block.position());
                applySignGlowing(blockEntity);
            } else if (object instanceof FabricItemType itemType) {
                itemType.toStack();
            }
        }
    }

    private void applySignGlowing(@Nullable Object signHolder) {
        if (signHolder == null) {
            return;
        }
        Object front = EffectRuntimeSupport.invokeCompatible(signHolder, "getFrontText");
        Object back = EffectRuntimeSupport.invokeCompatible(signHolder, "getBackText");
        Object updatedFront = front == null ? null : EffectRuntimeSupport.invokeCompatible(front, "setHasGlowingText", glowing);
        Object updatedBack = back == null ? null : EffectRuntimeSupport.invokeCompatible(back, "setHasGlowingText", glowing);
        if (updatedFront != null) {
            EffectRuntimeSupport.invokeCompatible(signHolder, "setText", updatedFront, true);
        }
        if (updatedBack != null) {
            EffectRuntimeSupport.invokeCompatible(signHolder, "setText", updatedBack, false);
        }
        EffectRuntimeSupport.invokeCompatible(signHolder, new String[]{"setChanged", "setRemoved"});
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + objects.toString(event, debug) + " have " + (glowing ? "glowing text" : "normal text");
    }
}

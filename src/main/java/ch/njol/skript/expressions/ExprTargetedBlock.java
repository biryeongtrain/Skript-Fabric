package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Targeted Block")
@Description({
        "The block at the crosshair.",
        "The actual target block uses the real outline hit box, while the regular target block treats the first non-air block on the view ray as solid."
})
@Example("break target block of player")
@Example("set {_block} to actual target block of player")
@Since("1.0, 2.9.0 (actual/exact), Fabric")
public class ExprTargetedBlock extends PropertyExpression<LivingEntity, FabricBlock> {

    private static final int MAX_TARGET_BLOCK_DISTANCE = 100;

    static {
        Skript.registerExpression(
                ExprTargetedBlock.class,
                FabricBlock.class,
                "[the] [actual:(actual[ly]|exact)] target[ed] block[s] [of %livingentities%]",
                "%livingentities%'[s] [actual:(actual[ly]|exact)] target[ed] block[s]"
        );
    }

    private boolean actual;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        setExpr((Expression<LivingEntity>) exprs[0]);
        actual = parser.hasTag("actual");
        return true;
    }

    @Override
    protected FabricBlock[] get(SkriptEvent event, LivingEntity[] source) {
        return get(source, this::targetBlock);
    }

    @Override
    public Class<FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String block = getExpr().isSingle() ? "block" : "blocks";
        return "the " + (actual ? "actual " : "") + "target " + block + " of " + getExpr().toString(event, debug);
    }

    private @Nullable FabricBlock targetBlock(LivingEntity livingEntity) {
        if (!(livingEntity.level() instanceof ServerLevel level)) {
            return null;
        }
        BlockPos target = actual ? traceActualBlock(livingEntity, level) : traceFirstSolidBlock(livingEntity, level);
        if (target == null || level.getBlockState(target).isAir()) {
            return null;
        }
        return new FabricBlock(level, target);
    }

    private @Nullable BlockPos traceActualBlock(LivingEntity livingEntity, ServerLevel level) {
        Vec3 start = livingEntity.getEyePosition();
        Vec3 end = start.add(livingEntity.getLookAngle().normalize().scale(MAX_TARGET_BLOCK_DISTANCE));
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, livingEntity));
        return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit.getBlockPos() : null;
    }

    private @Nullable BlockPos traceFirstSolidBlock(LivingEntity livingEntity, ServerLevel level) {
        Vec3 origin = livingEntity.getEyePosition();
        Vec3 step = livingEntity.getLookAngle().normalize().scale(0.2D);
        Vec3 current = origin;
        for (int index = 0; index < MAX_TARGET_BLOCK_DISTANCE * 5; index++) {
            current = current.add(step);
            BlockPos pos = BlockPos.containing(current);
            if (!level.getBlockState(pos).isAir()) {
                return pos;
            }
        }
        return null;
    }
}

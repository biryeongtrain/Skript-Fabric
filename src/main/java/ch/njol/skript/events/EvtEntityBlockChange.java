package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class EvtEntityBlockChange extends SkriptEvent {

    private enum ChangeEvent {
        ENDERMAN_PLACE("enderman place", handle -> handle.entity() instanceof EnderMan && !isAir(handle.to())),
        ENDERMAN_PICKUP("enderman pickup", handle -> handle.entity() instanceof EnderMan && isAir(handle.to())),
        SHEEP_EAT("sheep eat", handle -> handle.entity() instanceof Sheep),
        SILVERFISH_ENTER("silverfish enter", handle -> handle.entity() instanceof Silverfish && !isAir(handle.to())),
        SILVERFISH_EXIT("silverfish exit", handle -> handle.entity() instanceof Silverfish && isAir(handle.to())),
        FALLING_BLOCK_FALLING("falling block fall[ing]", handle -> handle.entity() instanceof FallingBlockEntity && isAir(handle.to())),
        FALLING_BLOCK_LANDING("falling block land[ing]", handle -> handle.entity() instanceof FallingBlockEntity && !isAir(handle.to())),
        GENERIC("(entity|%*-entitydatas%) chang(e|ing) block[s]");

        private static final String[] PATTERNS = java.util.Arrays.stream(values())
                .map(ChangeEvent::pattern)
                .toArray(String[]::new);

        private final String pattern;
        private final @Nullable Predicate<FabricEventCompatHandles.EntityBlockChange> checker;

        ChangeEvent(String pattern) {
            this(pattern, null);
        }

        ChangeEvent(String pattern, @Nullable Predicate<FabricEventCompatHandles.EntityBlockChange> checker) {
            this.pattern = pattern;
            this.checker = checker;
        }

        private String pattern() {
            return pattern;
        }
    }

    private @Nullable Literal<EntityData<?>> datas;
    private ChangeEvent changeEvent;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtEntityBlockChange.class)) {
            return;
        }
        Skript.registerEvent(EvtEntityBlockChange.class, ChangeEvent.PATTERNS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        changeEvent = ChangeEvent.values()[matchedPattern];
        if (changeEvent == ChangeEvent.GENERIC) {
            datas = (Literal<EntityData<?>>) args[0];
        } else {
            datas = null;
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.EntityBlockChange handle)) {
            return false;
        }
        if (datas != null && !datas.check(event, data -> data.isInstance(handle.entity()))) {
            return false;
        }
        return changeEvent.checker == null || changeEvent.checker.test(handle);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.EntityBlockChange.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return changeEvent.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
    }

    private static boolean isAir(@Nullable BlockState state) {
        return state == null || state.isAir();
    }
}

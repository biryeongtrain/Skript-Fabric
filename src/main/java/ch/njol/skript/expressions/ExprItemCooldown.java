package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item Cooldown")
@Description("""
        Gets the current cooldown of a provided item for a player.
        If the provided item has a cooldown group component specified the cooldown of the group will be prioritized.
        Otherwise the cooldown of the item material will be used.
        """)
@Example("""
        on right click using stick:
            set item cooldown of player's tool for player to 1 minute
            set item cooldown of stone and grass for all players to 20 seconds
            reset item cooldown of cobblestone and dirt for all players
        """)
@RequiredPlugins("MC 1.21.2 (cooldown group)")
@Since({"2.8.0", "2.12 (cooldown group)"})
public class ExprItemCooldown extends SimpleExpression<Timespan> {

    static {
        ch.njol.skript.Skript.registerExpression(
                ExprItemCooldown.class,
                Timespan.class,
                "[the] [item] cooldown of %itemtypes% for %players%",
                "%players%'[s] [item] cooldown for %itemtypes%"
        );
    }

    private Expression<ServerPlayer> players;
    private Expression<FabricItemType> itemTypes;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[matchedPattern ^ 1];
        itemTypes = (Expression<FabricItemType>) exprs[matchedPattern];
        return true;
    }

    @Override
    protected Timespan[] get(SkriptEvent event) {
        ServerPlayer[] playerArray = players.getArray(event);
        List<ItemStack> stacks = convertToItemList(itemTypes.getArray(event));
        List<Timespan> cooldowns = new ArrayList<>(playerArray.length * Math.max(stacks.size(), 1));
        for (ServerPlayer player : playerArray) {
            for (ItemStack stack : stacks) {
                float progress = player.getCooldowns().getCooldownPercent(stack, 0.0F);
                int ticks = Math.round(progress * currentCooldownTicks(player, stack));
                cooldowns.add(new Timespan(Timespan.TimePeriod.TICK, ticks));
            }
        }
        return cooldowns.toArray(Timespan[]::new);
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Timespan.class};
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.RESET && mode != ChangeMode.DELETE && delta == null) {
            return;
        }

        int ticks = delta == null ? 0 : (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
        if (mode == ChangeMode.REMOVE && ticks != 0) {
            ticks = -ticks;
        }

        ServerPlayer[] playerArray = players.getArray(event);
        List<ItemStack> stacks = convertToItemList(itemTypes.getArray(event));
        for (ServerPlayer player : playerArray) {
            for (ItemStack stack : stacks) {
                switch (mode) {
                    case RESET, DELETE -> removeCooldown(player, stack);
                    case SET -> player.getCooldowns().addCooldown(stack, Math.max(ticks, 0));
                    case ADD, REMOVE -> player.getCooldowns().addCooldown(
                            stack,
                            Math.max(currentCooldownTicks(player, stack) + ticks, 0)
                    );
                }
            }
        }
    }

    private int currentCooldownTicks(ServerPlayer player, ItemStack stack) {
        try {
            Object cooldowns = player.getCooldowns();
            Object group = cooldownGroup(cooldowns, stack);
            FieldHandle fields = FieldHandle.resolve(cooldowns.getClass());
            @SuppressWarnings("unchecked")
            Map<Object, Object> entries = (Map<Object, Object>) fields.entries().get(cooldowns);
            Object entry = entries.get(group);
            if (entry == null) {
                return 0;
            }
            int currentTick = fields.tick().getInt(cooldowns);
            int endTick = fields.endTick(entry);
            return Math.max(0, endTick - currentTick);
        } catch (ReflectiveOperationException exception) {
            float progress = player.getCooldowns().getCooldownPercent(stack, 0.0F);
            return progress > 0.0F ? 1 : 0;
        }
    }

    private List<ItemStack> convertToItemList(FabricItemType... types) {
        return Arrays.stream(types)
                .map(FabricItemType::toStack)
                .filter(stack -> !stack.isEmpty())
                .distinct()
                .toList();
    }

    private void removeCooldown(ServerPlayer player, ItemStack stack) {
        Object cooldowns = player.getCooldowns();
        try {
            Object group = cooldownGroup(cooldowns, stack);
            cooldowns.getClass().getMethod("removeCooldown", group.getClass()).invoke(cooldowns, group);
        } catch (ReflectiveOperationException exception) {
            player.getCooldowns().addCooldown(stack, 0);
        }
    }

    private Object cooldownGroup(Object cooldowns, ItemStack stack) throws ReflectiveOperationException {
        for (java.lang.reflect.Method method : cooldowns.getClass().getMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == ItemStack.class && method.getReturnType() != void.class) {
                method.setAccessible(true);
                return method.invoke(cooldowns, stack);
            }
        }
        throw new NoSuchMethodException("Unable to resolve cooldown group accessor");
    }

    @Override
    public boolean isSingle() {
        return players.isSingle() && itemTypes.isSingle();
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "cooldown of " + itemTypes.toString(event, debug) + " for " + players.toString(event, debug);
    }

    private record FieldHandle(java.lang.reflect.Field tick, java.lang.reflect.Field entries) {

        private static FieldHandle resolve(Class<?> cooldownClass) throws NoSuchFieldException {
            java.lang.reflect.Field tickField = null;
            java.lang.reflect.Field entriesField = null;
            for (java.lang.reflect.Field field : cooldownClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == int.class && tickField == null) {
                    tickField = field;
                } else if (Map.class.isAssignableFrom(field.getType()) && entriesField == null) {
                    entriesField = field;
                }
            }
            if (tickField == null || entriesField == null) {
                throw new NoSuchFieldException("Unable to resolve cooldown tracker fields");
            }
            return new FieldHandle(tickField, entriesField);
        }

        private int endTick(Object entry) throws ReflectiveOperationException {
            for (java.lang.reflect.Method method : entry.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getReturnType() == int.class) {
                    method.setAccessible(true);
                    return (int) method.invoke(entry);
                }
            }
            throw new NoSuchMethodException("Unable to resolve cooldown entry end tick");
        }
    }
}

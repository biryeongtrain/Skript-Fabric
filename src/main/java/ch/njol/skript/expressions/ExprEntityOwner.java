package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity Owner")
@Description("The owner of a tameable entity (i.e. horse or wolf).")
@Example("""
        set owner of last spawned wolf to player
        if the owner of last spawned wolf is player:
    """)
@Since("2.5")
public class ExprEntityOwner extends SimplePropertyExpression<Entity, GameProfile> {

    static {
        register(ExprEntityOwner.class, GameProfile.class, "(owner|tamer)", "livingentities");
    }

    @Override
    public @Nullable GameProfile convert(Entity entity) {
        if (!(entity instanceof OwnableEntity ownable)) {
            return null;
        }
        LivingEntity owner = ownable.getOwner();
        if (owner instanceof ServerPlayer player) {
            return player.getGameProfile();
        }
        var reference = ownable.getOwnerReference();
        if (reference == null) {
            return null;
        }
        UUID uuid = reference.getUUID();
        String name = owner == null ? null : owner.getName().getString();
        return new GameProfile(uuid, name);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET -> new Class[]{GameProfile.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        GameProfile profile = delta == null ? null : (GameProfile) delta[0];
        for (Entity entity : getExpr().getArray(event)) {
            if (!(entity instanceof TamableAnimal tameable)) {
                continue;
            }
            if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET || profile == null || profile.id() == null) {
                tameable.setOwner(null);
                tameable.setTame(false, false);
                continue;
            }
            if (entity.level().getServer() == null) {
                continue;
            }
            ServerPlayer player = entity.level().getServer().getPlayerList().getPlayer(profile.id());
            if (player != null) {
                tameable.setOwner(player);
            }
        }
    }

    @Override
    public Class<GameProfile> getReturnType() {
        return GameProfile.class;
    }

    @Override
    protected String getPropertyName() {
        return "owner";
    }
}

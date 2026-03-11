package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Version")
@Description("The Fabric platform, Minecraft, or Skript version on the current compatibility surface.")
@Example("send minecraft version")
@Example("send skript version")
@Since("2.0, Fabric")
public class ExprVersion extends SimpleExpression<String> {

    private VersionType type;

    static {
        Skript.registerExpression(
                ExprVersion.class,
                String.class,
                "(0¦[craft]bukkit|1¦minecraft|2¦skript)( |-)version",
                "fabric version"
        );
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        type = matchedPattern == 1 ? VersionType.FABRIC : VersionType.values()[parseResult.mark];
        return true;
    }

    @Override
    protected String[] get(SkriptEvent event) {
        return new String[]{type.get()};
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return type + " version";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    private enum VersionType {
        FABRIC("Fabric") {
            @Override
            String get() {
                return FabricLoader.getInstance()
                        .getModContainer("fabricloader")
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown");
            }
        },
        MINECRAFT("Minecraft") {
            @Override
            String get() {
                return Skript.getMinecraftVersion().toString();
            }
        },
        SKRIPT("Skript") {
            @Override
            String get() {
                return FabricLoader.getInstance()
                        .getModContainer("skript-fabric-port")
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown");
            }
        };

        private final String name;

        VersionType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        abstract String get();
    }
}

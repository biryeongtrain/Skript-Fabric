package kim.biryeong.skriptFabric.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CommandNode.class, remap = false)
public interface CommandNodeAccessor<S> {

	@Accessor("children")
	Map<String, CommandNode<S>> skript$getChildren();

	@Accessor("literals")
	Map<String, LiteralCommandNode<S>> skript$getLiterals();

	@Accessor("arguments")
	Map<String, ArgumentCommandNode<S, ?>> skript$getArguments();
}

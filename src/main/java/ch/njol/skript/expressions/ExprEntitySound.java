package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import kim.biryeong.skriptFabric.mixin.EntitySoundAccessor;
import kim.biryeong.skriptFabric.mixin.LivingEntitySoundAccessor;
import kim.biryeong.skriptFabric.mixin.MobSoundAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.Objects;

@Name("Entity Sound")
@Description("Gets the sound that a given entity will make in a specific scenario.")
@Example("set {_sound} to death sound of target entity")
@Since("2.10")
public class ExprEntitySound extends SimpleExpression<String> {

	public enum SoundType {
		DAMAGE {
			@Override
			public @Nullable SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy) {
				DamageSources sources = entity.damageSources();
				return ((LivingEntitySoundAccessor) entity).skript$getHurtSound(sources.generic());
			}
		},
		DEATH {
			@Override
			public @Nullable SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy) {
				return ((LivingEntitySoundAccessor) entity).skript$getDeathSound();
			}
		},
		FALL {
			@Override
			public SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy) {
				if (height != -1)
					return ((LivingEntitySoundAccessor) entity).skript$getFallDamageSound(height);
				else {
					LivingEntity.Fallsounds fallSounds = ((LivingEntitySoundAccessor) entity).skript$getFallSounds();
					return bigOrSpeedy ? fallSounds.big() : fallSounds.small();
				}
			}
		},
		SWIM {
			@Override
			public SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy) {
				return ((EntitySoundAccessor) entity).skript$getSwimSound();
			}
		},
		SPLASH {
			@Override
			public SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy) {
				return bigOrSpeedy
					? ((EntitySoundAccessor) entity).skript$getSwimHighSpeedSplashSound()
					: ((EntitySoundAccessor) entity).skript$getSwimSplashSound();
			}
		},
		AMBIENT {
			@Override
			public @Nullable SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy) {
				return entity instanceof Mob mob ? ((MobSoundAccessor) mob).skript$getAmbientSound() : null;
			}
		};

		public abstract @Nullable SoundEvent getSound(LivingEntity entity, int height, boolean bigOrSpeedy);
	}

	private static final Patterns<SoundType> patterns = new Patterns<>(new Object[][]{
		{"[the] (damage|hurt) sound[s] of %livingentities%", SoundType.DAMAGE},
		{"%livingentities%'[s] (damage|hurt) sound[s]", SoundType.DAMAGE},

		{"[the] death sound[s] of %livingentities%", SoundType.DEATH},
		{"%livingentities%'[s] death sound[s]", SoundType.DEATH},

		{"[the] [high:(tall|high)|(low|normal)] fall damage sound[s] [from [[a] height [of]] %-number%] of %livingentities%", SoundType.FALL},
		{"%livingentities%'[s] [high:(tall|high)|low:(low|normal)] fall [damage] sound[s] [from [[a] height [of]] %-number%]", SoundType.FALL},

		{"[the] swim[ming] sound[s] of %livingentities%", SoundType.SWIM},
		{"%livingentities%'[s] swim[ming] sound[s]", SoundType.SWIM},

		{"[the] [fast:(fast|speedy)] splash sound[s] of %livingentities%", SoundType.SPLASH},
		{"%livingentities%'[s] [fast:(fast|speedy)] splash sound[s]", SoundType.SPLASH},

		{"[the] ambient sound[s] of %livingentities%", SoundType.AMBIENT},
		{"%livingentities%'[s] ambient sound[s]", SoundType.AMBIENT}
	});

	static {
		Skript.registerExpression(ExprEntitySound.class, String.class, patterns.getPatterns());
	}

	private boolean bigOrSpeedy;
	private SoundType soundType;
	private Expression<Number> height;
	private Expression<LivingEntity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		soundType = patterns.getInfo(matchedPattern);
		bigOrSpeedy = parseResult.hasTag("high") || parseResult.hasTag("fast");
		if (soundType == SoundType.FALL)
			height = (Expression<Number>) exprs[0];
		entities = (Expression<LivingEntity>) ((soundType == SoundType.FALL) ? exprs[1] : exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(SkriptEvent event) {
		int height = this.height == null ? -1 : this.height.getOptionalSingle(event).orElse(-1).intValue();

		return entities.stream(event)
			.map(entity -> soundType.getSound(entity, height, bigOrSpeedy))
			.filter(Objects::nonNull)
			.distinct()
			.map((SoundEvent sound) -> {
				var key = BuiltInRegistries.SOUND_EVENT.getKey(sound);
				return key != null ? key.getPath() : sound.location().getPath();
			})
			.toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable SkriptEvent event, boolean debug) {
		String sound = "unknown";
		switch (soundType) {
			case DAMAGE, DEATH, SWIM, AMBIENT -> sound = soundType.name().toLowerCase();
			case FALL -> {
				if (this.height == null) {
					sound = bigOrSpeedy ? "high fall damage" : "normal fall damage";
				} else {
					sound = "fall damage from a height of " + this.height.toString(event, debug);
				}
			}
			case SPLASH -> sound = bigOrSpeedy ? "speedy splash" : "splash";
		}
		return sound + " sound of " + entities.toString(event, debug);
	}

}

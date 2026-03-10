package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.effects.EffDoIf;
import ch.njol.skript.effects.EffEquip;
import ch.njol.skript.effects.EffHealth;
import ch.njol.skript.effects.EffReturn;
import org.skriptlang.skript.bukkit.breeding.elements.EffAllowAging;
import org.skriptlang.skript.bukkit.breeding.elements.EffBreedable;
import org.skriptlang.skript.bukkit.breeding.elements.EffMakeAdultOrBaby;
import org.skriptlang.skript.bukkit.brewing.elements.EffBrewingConsume;
import org.skriptlang.skript.bukkit.displays.text.EffTextDisplayDropShadow;
import org.skriptlang.skript.bukkit.displays.text.EffTextDisplaySeeThroughBlocks;
import org.skriptlang.skript.bukkit.fishing.elements.EffFishingLure;
import org.skriptlang.skript.bukkit.fishing.elements.EffPullHookedEntity;
import org.skriptlang.skript.bukkit.interactions.elements.effects.EffMakeResponsive;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompDamageable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompDispensable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompInteract;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompShearable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.EffEquipCompSwapEquipment;
import org.skriptlang.skript.bukkit.loottables.elements.effects.EffGenerateLoot;
import org.skriptlang.skript.bukkit.misc.effects.EffRotate;
import org.skriptlang.skript.bukkit.particles.elements.effects.EffPlayEffect;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffApplyPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPoison;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionAmbient;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionIcon;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionInfinite;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPotionParticles;
import org.skriptlang.skript.bukkit.potion.elements.effects.PotionPropertyEffect;
import org.skriptlang.skript.bukkit.tags.elements.EffRegisterTag;

final class SkriptFabricAdditionalEffects {

    private SkriptFabricAdditionalEffects() {
    }

    static void register() {
        Delay.register();
        EffDoIf.register();
        EffEquip.register();
        EffHealth.register();
        EffReturn.register();
        Skript.registerEffect(
                EffAllowAging.class,
                "lock age of %entities%",
                "prevent aging of %entities%",
                "prevent %entities% from aging",
                "unlock age of %entities%",
                "allow aging of %entities%",
                "allow %entities% to age"
        );
        Skript.registerEffect(
                EffBreedable.class,
                "make %entities% breedable",
                "unsterilize %entities%",
                "make %entities% (not |non(-| )|un)breedable",
                "sterilize %entities%"
        );
        Skript.registerEffect(
                EffMakeAdultOrBaby.class,
                "make %livingentities% [a[n]] (:adult|baby|child)",
                "force %livingentities% to be[come] a[n] (:adult|baby|child)"
        );
        Skript.registerEffect(
                EffBrewingConsume.class,
                "make [the] brewing stand consume [its|the] fuel",
                "prevent [the] brewing stand from consuming [its|the] fuel"
        );
        Skript.registerEffect(
                EffTextDisplayDropShadow.class,
                "(apply|add) (drop|text) shadow to [[the] text of] %entities%",
                "(apply|add) (drop|text) shadow to %entities%'[s] text",
                "(remove|clear) (drop|text) shadow from [[the] text of] %entities%",
                "(remove|clear) (drop|text) shadow from %entities%'[s] text"
        );
        Skript.registerEffect(
                EffTextDisplaySeeThroughBlocks.class,
                "make %entities% visible through (blocks|walls)",
                "force %entities% to be visible through (blocks|walls)",
                "(prevent|block) %entities% from being (visible|seen) through (blocks|walls)"
        );
        Skript.registerEffect(
                EffFishingLure.class,
                "apply [the] lure enchantment bonus",
                "remove [the] lure enchantment bonus"
        );
        Skript.registerEffect(
                EffPullHookedEntity.class,
                "(reel|pull) in hook[ed] entity"
        );
        Skript.registerEffect(
                EffMakeResponsive.class,
                "make %entities% responsive",
                "make %entities% (not |un)responsive"
        );
        Skript.registerEffect(
                EffEquipCompDamageable.class,
                "make %objects% lose durability when injured",
                "(allow|force) %objects% to lose durability when injured",
                "make %objects% not lose durability when injured",
                "(disallow|prevent) %objects% from losing durability when injured"
        );
        Skript.registerEffect(
                EffEquipCompDispensable.class,
                "allow %objects% to be dispensed",
                "make %objects% dispensable",
                "let %objects% be dispensed",
                "(block|prevent|disallow) %objects% from being dispensed",
                "make %objects% not dispensable"
        );
        Skript.registerEffect(
                EffEquipCompInteract.class,
                "allow %objects% to be equipped on[to] entities",
                "make %objects% equippable on[to] entities",
                "let %objects% be equipped on[to] entities",
                "(block|prevent|disallow) %objects% from being equipped on[to] entities",
                "make %objects% not equippable on[to] entities"
        );
        Skript.registerEffect(
                EffEquipCompShearable.class,
                "allow %objects% to be sheared off [of entities]",
                "(disallow|prevent) %objects% from being sheared off [of entities]"
        );
        Skript.registerEffect(
                EffEquipCompSwapEquipment.class,
                "(allow|force) %objects% to swap equipment [on right click|when right clicked]",
                "(make|let) %objects% swap equipment [on right click|when right clicked]",
                "(block|prevent|disallow) %objects% from swapping equipment [on right click|when right clicked]",
                "make %objects% not swap equipment [on right click|when right clicked]"
        );
        Skript.registerEffect(
                EffGenerateLoot.class,
                "generate [the] loot (of|using) %loottables% in %objects%",
                "generate [the] loot (of|using) %loottables% (with|using) %objects% in %objects%"
        );
        Skript.registerEffect(
                EffRotate.class,
                "rotate %objects% around [the] [global] x(-| )axis by %number%",
                "rotate %objects% around [the] [global] y(-| )axis by %number%",
                "rotate %objects% around [the] [global] z(-| )axis by %number%",
                "rotate %objects% around [the|its|their] local x(-| )ax(i|e)s by %number%",
                "rotate %objects% around [the|its|their] local y(-| )ax(i|e)s by %number%",
                "rotate %objects% around [the|its|their] local z(-| )ax(i|e)s by %number%",
                "rotate %objects% around [the] %vector% by %number%",
                "rotate %objects% by x %number%, y %number%(, [and]| and) z %number%"
        );
        Skript.registerEffect(
                EffPlayEffect.class,
                "force (play|show|draw) %objects% at %locations%",
                "force (play|draw) %objects% at %locations% (for|to) %players%",
                "force (play|show|draw) %objects% at %locations% (in|with) [a] [view] (radius|range) [of] %number%",
                "force (play|show|draw) %objects% on %entities%",
                "(play|show|draw) %objects% at %locations%",
                "(play|draw) %objects% at %locations% (for|to) %players%",
                "(play|show|draw) %objects% at %locations% (in|with) [a] [view] (radius|range) [of] %number%",
                "(play|show|draw) %objects% on %entities%"
        );
        Skript.registerEffect(
                EffApplyPotionEffect.class,
                "(apply|grant) %objects% to %entities%",
                "(apply|grant) %objects% to %entities% for %timespan%",
                "(affect|afflict) %entities% with %objects%",
                "(affect|afflict) %entities% with %objects% for %timespan%"
        );
        Skript.registerEffect(
                EffPoison.class,
                "poison %entities%",
                "poison %entities% for %timespan%",
                "(cure|unpoison) %entities% [(from|of) poison]"
        );
        Skript.registerEffect(EffPotionAmbient.class, PotionPropertyEffect.getPatterns(PotionPropertyEffect.Type.MAKE, "ambient"));
        Skript.registerEffect(EffPotionIcon.class, PotionPropertyEffect.getPatterns(PotionPropertyEffect.Type.SHOW, "icon[s]"));
        Skript.registerEffect(EffPotionInfinite.class, PotionPropertyEffect.getPatterns(PotionPropertyEffect.Type.MAKE, "(infinite|permanent)"));
        Skript.registerEffect(EffPotionParticles.class, PotionPropertyEffect.getPatterns(PotionPropertyEffect.Type.SHOW, "particles"));
        Skript.registerEffect(
                EffRegisterTag.class,
                "register item tag named %string% (containing|using) %objects%",
                "register custom item tag named %string% (containing|using) %objects%",
                "register block tag named %string% (containing|using) %objects%",
                "register custom block tag named %string% (containing|using) %objects%",
                "register entity tag named %string% (containing|using) %objects%",
                "register custom entity tag named %string% (containing|using) %objects%"
        );
    }
}

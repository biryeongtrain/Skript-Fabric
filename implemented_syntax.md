# Implemented Syntax 

---
## Structures

| # | Class | Description |
|---|-------|-------------|
| 1 | StructOptions | `options:` block for script-level constants |
| 2 | StructFunction | `function` declaration |
| 3 | StructCommand | `command` declaration |

## Sections

| # | Class | Description |
|---|-------|-------------|
| 1 | SecIf | `if`/`else if`/`else` conditional |
| 2 | SecFilter | `filter` section |
| 3 | SecFor | `loop` section |
| 4 | SecWhile | `while` loop |
| 5 | SecCatchErrors | `catch errors` section |
| 6 | SecCreateGui | GUI creation section |
| 7 | EffSecSpawn | `spawn` section |
| 8 | EffSecShoot | `shoot` section |
| 9 | ExprSecCreateWorldBorder | World border creation section |
| 10 | ExprSecDamageSource | Damage source creation section |
| 11 | ExprSecBlankEquipComp | Equippable component creation section |
| 12 | ExprSecCreateLootContext | Loot context creation section |
| 13 | ExprSecPotionEffect | Potion effect creation section |

## ClassInfo Types (25)

| # | Code Name | Class | Description |
|---|-----------|-------|-------------|
| 1 | player | PlayerClassInfo | ServerPlayer type |
| 2 | inventory | InventoryClassInfo | Inventory type |
| 3 | itemstack | ItemStackClassInfo | ItemStack type |
| 4 | itemtype | ItemTypeClassInfo | FabricItemType type |
| 5 | inputkey | InputKeyClassInfo | Input key type |
| 6 | location | LocationClassInfo | Location (BlockPos/Vec3) type |
| 7 | loottable | LootTableClassInfo | Loot table type |
| 8 | nameable | NameableClassInfo | Nameable entity type |
| 9 | billboardconstraints | DisplayBillboardConstraintsClassInfo | Display billboard type |
| 10 | itemdisplaytransform | ItemDisplayTransformClassInfo | Item display transform type |
| 11 | offlineplayer | OfflinePlayerClassInfo | Offline player type |
| 12 | potioneffectcause | PotionCauseClassInfo | Potion effect cause type |
| 13 | quaternion | QuaternionClassInfo | Quaternion rotation type |
| 14 | textdisplayalign | TextDisplayAlignClassInfo | Text display alignment type |
| 15 | timespan | TimespanClassInfo | Timespan duration type |
| 16 | world | WorldClassInfo | World/ServerLevel type |
| 17 | entity | EntityClassInfo | Entity type |
| 18 | damagesource | DamageSourceClassInfo | Damage source type |
| 19 | block | BlockClassInfo | Block/FabricBlock type |
| 20 | slot | SlotClassInfo | Inventory slot type |
| 21 | vector | VectorClassInfo | Vec3 vector type |
| 22 | particle | ParticleClassInfo | Particle type |
| 23 | gameeffect | GameEffectClassInfo | Game effect type |
| 24 | equippablecomponent | EquippableComponentClassInfo | Equippable component type |
| 25 | enchantment | EnchantmentClassInfo | Enchantment (Holder) type |
| 26 | enchantmenttype | EnchantmentTypeClassInfo | Enchantment + level pair type |

---

## Events

### Core Events (SkriptFabricBootstrap)

| # | Class | Trigger |
|---|-------|---------|
| 1 | EvtFabricBlockBreak | on block break |
| 2 | EvtAttackEntity | on attack entity |
| 3 | EvtBrewingFuel | on brewing fuel |
| 4 | EvtDamage | on damage |
| 5 | EvtFabricGameTest | on game test |
| 6 | EvtFishing | on fishing |
| 7 | EvtPlayerInput | on player input |
| 8 | EvtFabricServerTick | on server tick |
| 9 | EvtFabricUseBlock | on use block / right click block |
| 10 | EvtUseEntity | on use entity / right click entity |
| 11 | EvtUseItem | on use item / right click with item |
| 12 | EvtServerListPing | on server list ping |

### Additional Events (SkriptFabricAdditionalSyntax)

| # | Class | Trigger |
|---|-------|---------|
| 13 | EvtBucketCatch | on bucket catch |
| 14 | EvtBreeding | on breeding |
| 15 | EvtBrewingComplete | on brewing complete |
| 16 | EvtBrewingStart | on brewing start |
| 17 | EvtEntityPotion | on entity potion effect |
| 18 | EvtLoveModeEnter | on love mode enter |
| 19 | EvtLootGenerate | on loot generate |
| 20 | EvtFurnace | on furnace smelt/extract |
| 21 | EvtEnchantPrepare | on enchant prepare |
| 22 | EvtEnchantApply | on item enchant |
| 23 | EvtMending | on mending |
| 24 | EvtInventoryMove | on inventory item move |
| 25 | EvtChat | on chat |

### Recovered Events (Bundle)

| # | Class | Trigger |
|---|-------|---------|
| 26 | EvtAtTime | at time |
| 27 | EvtAreaCloudEffect | on area cloud effect |
| 28 | EvtBeaconEffect | on beacon effect |
| 29 | EvtBeaconToggle | on beacon toggle |
| 30 | EvtBlock | on block place/break/burn/fade/form/spread |
| 31 | EvtBlockFertilize | on block fertilize |
| 32 | EvtBookEdit | on book edit |
| 33 | EvtBookSign | on book sign |
| 34 | EvtClick | on click |
| 35 | EvtConnect | on connect |
| 36 | EvtEntity | on entity death/spawn/combust/tame/unleash |
| 37 | EvtEntityShootBow | on entity shoot bow |
| 38 | EvtEntityTarget | on entity target |
| 39 | EvtEntityTransform | on entity transform |
| 40 | EvtExperienceChange | on experience change |
| 41 | EvtExperienceCooldownChange | on experience cooldown change |
| 42 | EvtExperienceSpawn | on experience spawn |
| 43 | EvtExplode | on explode |
| 44 | EvtExplosionPrime | on explosion prime |
| 45 | EvtFirework | on firework |
| 46 | EvtFirstJoin | on first join |
| 47 | EvtGameMode | on gamemode change |
| 48 | EvtGrow | on grow |
| 49 | EvtHandItemSwap | on hand item swap |
| 50 | EvtHarvestBlock | on harvest block |
| 51 | EvtHealing | on healing |
| 52 | EvtItem | on item pickup/drop/despawn |
| 53 | EvtJoin | on join |
| 54 | EvtJump | on jump |
| 55 | EvtKick | on kick |
| 56 | EvtLeash | on leash |
| 57 | EvtLevel | on level change |
| 58 | EvtMove | on move |
| 59 | EvtMoveOn | on move on |
| 60 | EvtPeriodical | every (timespan) |
| 61 | EvtPiglinBarter | on piglin barter |
| 62 | EvtPlantGrowth | on plant growth |
| 63 | EvtPlayerArmorChange | on player armor change |
| 64 | EvtPlayerChunkEnter | on player chunk enter |
| 65 | EvtPlayerCommandSend | on player command send |
| 66 | EvtPlayerEggThrow | on player egg throw |
| 67 | EvtPortal | on portal |
| 68 | EvtPressurePlate | on pressure plate |
| 69 | EvtQuit | on quit |
| 70 | EvtReadyArrow | on ready arrow |
| 71 | EvtResourcePackResponse | on resource pack response |
| 72 | EvtRespawn | on respawn |
| 73 | EvtScript | on script load/unload |
| 74 | EvtSkript | on skript start/stop |
| 75 | EvtSpectate | on spectate |
| 76 | EvtTeleport | on teleport |
| 77 | EvtVehicleCollision | on vehicle collision |
| 78 | EvtWeatherChange | on weather change |
| 79 | EvtWorld | on world load/unload/save |
| 80 | EvtCommand | on command |
| 81 | EvtEntityBlockChange | on entity block change |
| 82 | EvtRealTime | at time in real time |

---

## Conditions

### Base Conditions (SkriptFabricBootstrap)

| # | Class | Pattern |
|---|-------|---------|
| 1 | CondIsEmpty | is empty |
| 2 | CondIsNamed | is named |
| 3 | CondIsAlive | is alive |
| 4 | CondIsBurning | is burning |
| 5 | CondIsSilent | is silent |
| 6 | CondIsInvisible | is invisible |
| 7 | CondIsInvulnerable | is invulnerable |
| 8 | CondAI | has AI |
| 9 | CondIsSprinting | is sprinting |
| 10 | CondContains | contains |
| 11 | CondItemInHand | is holding |
| 12 | CondIsWearing | is wearing |
| 13 | CondPermission | has permission |
| 14 | CondCompare | comparison (=, !=, >, <, >=, <=) |
| 15 | CondBrewingConsume | will consume brewing ingredient |
| 16 | CondCanAge | can age |
| 17 | CondCanBreed | can breed |
| 18 | CondIsAdult | is adult |
| 19 | CondIsBaby | is baby |
| 20 | CondIsInLove | is in love |
| 21 | CondScalesWithDifficulty | scales with difficulty |
| 22 | CondWasIndirect | was indirect |
| 23 | CondFishingLure | has lure |
| 24 | CondIsInOpenWater | is in open water |
| 25 | CondIsPressingKey | is pressing key |
| 26 | CondTextDisplayHasDropShadow | has drop shadow |
| 27 | CondTextDisplaySeeThroughBlocks | can see through blocks |
| 28 | CondIsResponsive | is responsive |
| 29 | CondHasLootTable | has loot table |
| 30 | CondIsLootable | is lootable |
| 31 | CondHasPotion | has potion effect |
| 32 | CondIsPoisoned | is poisoned |
| 33 | CondIsPotionAmbient | potion is ambient |
| 34 | CondIsPotionInstant | potion is instant |
| 35 | CondPotionHasIcon | potion has icon |
| 36 | CondPotionHasParticles | potion has particles |
| 37 | CondIsTagged | is tagged |
| 38 | CondEquipCompDamage | equip comp damages on hurt |
| 39 | CondEquipCompDispensable | equip comp is dispensable |
| 40 | CondEquipCompInteract | equip comp is interactable |
| 41 | CondEquipCompShearable | equip comp is shearable |
| 42 | CondEquipCompSwapEquipment | equip comp can swap |

### Recovered Conditions (Bundle)

| # | Class | Pattern |
|---|-------|---------|
| 43 | CondEntityStorageIsFull | entity storage is full |
| 44 | CondIsFuel | is fuel |
| 45 | CondIsOfType | is of type |
| 46 | CondIsResonating | is resonating |
| 47 | CondItemEnchantmentGlint | has enchantment glint |
| 48 | CondWillHatch | will hatch |
| 49 | CondCancelled | is cancelled |
| 50 | CondDamageCause | damage cause is |
| 51 | CondEntityUnload | entity unload |
| 52 | CondIncendiary | is incendiary |
| 53 | CondItemDespawn | item will despawn |
| 54 | CondIsPreferredTool | is preferred tool |
| 55 | CondIsSedated | is sedated |
| 56 | CondLeashWillDrop | leash will drop |
| 57 | CondRespawnLocation | has respawn location |
| 58 | CondScriptLoaded | script is loaded |

### Additional Conditions (SkriptFabricAdditionalSyntax - Forceinitialized)

| # | Class | Pattern |
|---|-------|---------|
| 59 | CondChatColors | chat colors enabled |
| 60 | CondChatFiltering | chat filtering enabled |
| 61 | CondChatVisibility | chat visibility |
| 62 | CondElytraBoostConsume | elytra boost will consume |
| 63 | CondFromMobSpawner | from mob spawner |
| 64 | CondHasClientWeather | has client weather |
| 65 | CondHasMetadata | has metadata |
| 66 | CondHasResourcePack | has resource pack |
| 67 | CondIsPluginEnabled | is plugin enabled |
| 68 | CondIsSkriptCommand | is skript command |
| 69 | CondIsSlimeChunk | is slime chunk |
| 70 | CondIsSpawnable | is spawnable |
| 71 | CondLeashed | is leashed |
| 72 | CondResourcePack | resource pack status |
| 73 | CondAlphanumeric | is alphanumeric |
| 74 | CondAllayCanDuplicate | allay can duplicate |
| 75 | CondAnchorWorks | anchor works |
| 76 | CondCanFly | can fly |
| 77 | CondCanHold | can hold |
| 78 | CondCanPickUpItems | can pick up items |
| 79 | CondCanSee | can see |
| 80 | CondChance | chance of |
| 81 | CondDate | date comparison |
| 82 | CondEndermanStaredAt | enderman is stared at |
| 83 | CondEntityIsInLiquid | is in liquid |
| 84 | CondEntityIsWet | is wet |
| 85 | CondGlowingText | has glowing text |
| 86 | CondGoatHasHorns | goat has horns |
| 87 | CondHasCustomModelData | has custom model data |
| 88 | CondHasItemCooldown | has item cooldown |
| 89 | CondHasLineOfSight | has line of sight |
| 90 | CondHasScoreboardTag | has scoreboard tag |
| 91 | CondIgnitionProcess | ignition process |
| 92 | CondIsBanned | is banned |
| 93 | CondIsBlock | is block |
| 94 | CondIsBlocking | is blocking |
| 95 | CondIsBlockRedstonePowered | block is powered |
| 96 | CondIsCharged | is charged |
| 97 | CondIsChargingFireball | is charging fireball |
| 98 | CondIsClimbing | is climbing |
| 99 | CondIsCommandBlockConditional | command block is conditional |
| 100 | CondIsCustomNameVisible | custom name is visible |
| 101 | CondIsDancing | is dancing |
| 102 | CondIsDashing | is dashing |
| 103 | CondIsDivisibleBy | is divisible by |
| 104 | CondIsEating | is eating |
| 105 | CondIsEdible | is edible |
| 106 | CondIsEnchanted | is enchanted |
| 107 | CondIsFireResistant | is fire resistant |
| 108 | CondIsFlammable | is flammable |
| 109 | CondIsFlying | is flying |
| 110 | CondIsFrozen | is frozen |
| 111 | CondIsGliding | is gliding |
| 112 | CondIsHandRaised | hand is raised |
| 113 | CondIsInfinite | is infinite |
| 114 | CondIsInteractable | is interactable |
| 115 | CondIsJumping | is jumping |
| 116 | CondIsLeashed | is leashed |
| 117 | CondIsLeftHanded | is left handed |
| 118 | CondIsLoaded | is loaded |
| 119 | CondIsOccluding | is occluding |
| 120 | CondIsOnGround | is on ground |
| 121 | CondIsOnline | is online |
| 122 | CondIsOp | is op |
| 123 | CondIsPassable | is passable |
| 124 | CondIsPathfinding | is pathfinding |
| 125 | CondIsPersistent | is persistent |
| 126 | CondIsPlayingDead | is playing dead |
| 127 | CondIsRiding | is riding |
| 128 | CondIsRinging | is ringing |
| 129 | CondIsRiptiding | is riptiding |
| 130 | CondIsSaddled | is saddled |
| 131 | CondIsScreaming | is screaming |
| 132 | CondIsSet | is set |
| 133 | CondIsSheared | is sheared |
| 134 | CondIsSleeping | is sleeping |
| 135 | CondIsSneaking | is sneaking |
| 136 | CondIsSolid | is solid |
| 137 | CondIsStackable | is stackable |
| 138 | CondIsSwimming | is swimming |
| 139 | CondIsTameable | is tameable |
| 140 | CondIsTamed | is tamed |
| 141 | CondIsTicking | is ticking |
| 142 | CondIsTransparent | is transparent |
| 143 | CondIsUnbreakable | is unbreakable |
| 144 | CondIsUsingFeature | is using feature |
| 145 | CondIsValid | is valid |
| 146 | CondIsVectorNormalized | vector is normalized |
| 147 | CondIsWhitelisted | is whitelisted |
| 148 | CondIsWithin | is within |
| 149 | CondLidState | lid state |
| 150 | CondMatches | matches |
| 151 | CondMinecraftVersion | minecraft version |
| 152 | CondPandaIsOnBack | panda is on back |
| 153 | CondPandaIsRolling | panda is rolling |
| 154 | CondPandaIsScared | panda is scared |
| 155 | CondPandaIsSneezing | panda is sneezing |
| 156 | CondPastFuture | is in past/future |
| 157 | CondPlayedBefore | has played before |
| 158 | CondPvP | pvp is enabled |
| 159 | CondStartsEndsWith | starts/ends with |
| 160 | CondStriderIsShivering | strider is shivering |
| 161 | CondTooltip | has tooltip |
| 162 | CondWithinRadius | is within radius |

---

## Effects

### Base Effects (SkriptFabricBootstrap)

| # | Class | Pattern |
|---|-------|---------|
| 1 | EffFeed | feed player |
| 2 | EffKill | kill entity |
| 3 | EffSilence | silence/unsilence entity |
| 4 | EffInvisible | make invisible/visible |
| 5 | EffInvulnerability | make invulnerable/vulnerable |
| 6 | EffSprinting | make sprint/stop sprinting |
| 7 | EffChange | set/add/remove/delete/reset |
| 8 | EffSetTestBlock | set test block |
| 9 | EffSetTestBlockAtBlock | set test block at block |
| 10 | EffSetTestBlockAboveBlock | set test block above block |
| 11 | EffSetTestBlockAtLocation | set test block at location |
| 12 | EffSetTestBlockUnderPlayer | set test block under player |
| 13 | EffSetTestEntityName | set test entity name |
| 14 | EffSetTestItemName | set test item name |
| 15 | EffSetFishingApproachAngle | set fishing approach angle |

### Communication Effects

| # | Class | Pattern |
|---|-------|---------|
| 16 | EffActionBar | send action bar |
| 17 | EffBroadcast | broadcast |
| 18 | EffKick | kick player |
| 19 | EffMessage | send message |
| 20 | EffOp | op/deop player |
| 21 | EffPlaySound | play sound |
| 22 | EffResetTitle | reset title |
| 23 | EffSendResourcePack | send resource pack |
| 24 | EffSendTitle | send title |
| 25 | EffStopSound | stop sound |

### Utility Effects

| # | Class | Pattern |
|---|-------|---------|
| 26 | EffBan | ban/unban |
| 27 | EffCancelItemUse | cancel item use |
| 28 | EffCommand | execute command |
| 29 | EffLidState | open/close lid |
| 30 | EffLook | make entity look at |
| 31 | EffOpenBook | open book |
| 32 | EffOpenInventory | open/close inventory |
| 33 | EffPvP | enable/disable pvp |
| 34 | EffSendBlockChange | send block change |
| 35 | EffTooltip | show/hide tooltip |
| 36 | EffWardenDisturbance | warden disturbance |
| 37 | EffWorldLoad | load world |
| 38 | EffWorldSave | save world |
| 39 | EffStopServer | stop server |
| 40 | EffBlockUpdate | update block |
| 41 | EffBreakNaturally | break naturally |
| 42 | EffCancelCooldown | cancel cooldown |
| 43 | EffCancelDrops | cancel drops |
| 44 | EffCancelEvent | cancel event |
| 45 | EffHidePlayerFromServerList | hide from server list |
| 46 | EffLoadServerIcon | load server icon |
| 47 | EffPlayerInfoVisibility | player info visibility |
| 48 | EffRing | ring bell |

### Recovered Effects (Bundle)

| # | Class | Pattern |
|---|-------|---------|
| 49 | EffApplyBoneMeal | apply bone meal |
| 50 | EffEntityUnload | unload entity |
| 51 | EffForceEnchantmentGlint | force enchantment glint |
| 52 | EffMakeEggHatch | make egg hatch |
| 53 | EffReplace | replace in |
| 54 | EffDetonate | detonate |
| 55 | EffLog | log |
| 56 | EffRun | run |
| 57 | EffSuppressWarnings | suppress warnings |
| 58 | EffSuppressTypeHints | suppress type hints |
| 59 | EffWorldBorderExpand | expand world border |
| 60 | EffCopy | copy |
| 61 | EffSort | sort |
| 62 | EffToggle | toggle |
| 63 | EffExceptionDebug | exception debug |

### Additional Effects (SkriptFabricAdditionalSyntax)

| # | Class | Pattern |
|---|-------|---------|
| 64 | EffAllayCanDuplicate | allow/prevent allay duplicate |
| 65 | EffAllayDuplicate | duplicate allay |
| 66 | EffCharge | charge/discharge creeper |
| 67 | EffColorItems | color items |
| 68 | EffCommandBlockConditional | make command block conditional |
| 69 | EffContinue | continue loop |
| 70 | EffDancing | make dance/stop dancing |
| 71 | EffDoIf | do if |
| 72 | EffDropLeash | drop leash |
| 73 | EffEnchant | enchant/disenchant item |
| 74 | EffEndermanTeleport | make enderman teleport |
| 75 | EffEquip | equip entity |
| 76 | EffExit | exit loop/section |
| 77 | EffExplodeCreeper | explode creeper |
| 78 | EffExplosion | create explosion |
| 79 | EffFireResistant | make fire resistant |
| 80 | EffFireworkLaunch | launch firework |
| 81 | EffForceAttack | force attack |
| 82 | EffGlowingText | make text glowing |
| 83 | EffGoatHorns | add/remove goat horns |
| 84 | EffGoatRam | make goat ram |
| 85 | EffHealth | set/heal/damage health |
| 86 | EffIncendiary | make incendiary |
| 87 | EffItemDespawn | make item despawn |
| 88 | EffKeepInventory | keep inventory |
| 89 | EffKnockback | knockback entity |
| 90 | EffLightning | strike lightning |
| 91 | EffPandaOnBack | put panda on back |
| 92 | EffPandaRolling | make panda roll |
| 93 | EffPandaSneezing | make panda sneeze |
| 94 | EffPathfind | make pathfind |
| 95 | EffPersistent | make persistent |
| 96 | EffPush | push entity |
| 97 | EffScreaming | make scream |
| 98 | EffStriderShivering | make strider shiver |
| 99 | EffSwingHand | swing hand |
| 100 | EffToggleFlight | toggle flight |
| 101 | EffTransform | transform entity |
| 102 | EffVehicle | make ride/exit vehicle |
| 103 | EffZombify | zombify villager |
| 104 | EffEnforceWhitelist | enforce whitelist |
| 105 | EffRespawn | respawn player |
| 106 | EffClearEntityStorage | clear entity storage of blocks |
| 107 | EffInsertEntityStorage | insert entities into entity storage |
| 108 | EffReleaseEntityStorage | release entity storage of blocks |
| 109 | EffMakeSay | make player say/send message |
| 110 | EffScriptFile | enable/reload/disable/unload script |
| 111 | EffConnect | transfer player to server |
| 112 | EffElytraBoostConsume | prevent/allow firework consume on elytra boost |
| 113 | EffEntityVisibility | hide/reveal entities for players |

---

## Expressions

### Event Expressions

| # | Class | Description |
|---|-------|-------------|
| 1 | ExprEventBlock | event-block |
| 2 | ExprEventPlayer | event-player |
| 3 | ExprEventEntity | event-entity |
| 4 | ExprEventItem | event-item |
| 5 | ExprEventDamageSource | event-damage source |
| 6 | ExprEventPotionEffect | event-potion effect |
| 7 | ExprEventPotionEffectAction | event-potion effect action |

### Inventory Expressions

| # | Class | Description |
|---|-------|-------------|
| 8 | ExprChestInventory | chest inventory of |
| 9 | ExprEnderChest | ender chest of |
| 10 | ExprInventory | inventory of |
| 11 | ExprInventoryInfo | inventory name/rows/type |
| 12 | ExprInventorySlot | slot %n% of |
| 13 | ExprItemsIn | items in |
| 14 | ExprFirstEmptySlot | first empty slot of |
| 15 | ExprSlotIndex | slot index |
| 16 | ExprFurnaceSlot | furnace slot |
| 17 | ExprFurnaceEventItems | furnace event items |
| 18 | ExprFurnaceTime | furnace time |

### Enchantment Expressions

| # | Class | Description |
|---|-------|-------------|
| 19 | ExprAppliedEnchantments | applied enchantments |
| 20 | ExprEnchantingExpCost | displayed enchanting cost |
| 21 | ExprEnchantItem | enchant item |
| 22 | ExprEnchantmentBonus | enchantment bonus |
| 23 | ExprEnchantmentLevel | enchantment level of |
| 24 | ExprEnchantmentOffer | enchantment offers |
| 25 | ExprEnchantments | enchantments of |
| 26 | ExprMendingRepairAmount | mending repair amount |

### Damage Expressions

| # | Class | Description |
|---|-------|-------------|
| 27 | ExprCausingEntity | causing entity |
| 28 | ExprDirectEntity | direct entity |
| 29 | ExprDamageLocation | damage location |
| 30 | ExprSourceLocation | source location |
| 31 | ExprFoodExhaustion | food exhaustion |
| 32 | ExprDamage | damage |
| 33 | ExprDamageCause | damage cause |
| 34 | ExprFinalDamage | final damage |
| 35 | ExprLastDamageCause | last damage cause |
| 36 | ExprCreatedDamageSource | created damage source |
| 37 | ExprDamageType | damage type |
| 38 | ExprSecDamageSource | section damage source |

### Display Expressions

| # | Class | Description |
|---|-------|-------------|
| 39 | ExprDisplayBillboard | display billboard |
| 40 | ExprDisplayBrightness | display brightness |
| 41 | ExprDisplayHeightWidth | display height/width |
| 42 | ExprDisplayShadow | display shadow |
| 43 | ExprDisplayInterpolation | display interpolation |
| 44 | ExprDisplayTransformationRotation | display transformation rotation |
| 45 | ExprDisplayTransformationScaleTranslation | display transformation scale/translation |
| 46 | ExprDisplayTeleportDuration | display teleport duration |
| 47 | ExprDisplayViewRange | display view range |
| 48 | ExprItemDisplayTransform | item display transform |
| 49 | ExprDisplayGlowOverride | display glow override |
| 50 | ExprGlowing | glowing |
| 51 | ExprTextDisplayAlignment | text display alignment |
| 52 | ExprTextDisplayLineWidth | text display line width |
| 53 | ExprTextDisplayOpacity | text display opacity |

### Brewing Expressions

| # | Class | Description |
|---|-------|-------------|
| 54 | ExprBrewingFuelLevel | brewing fuel level |
| 55 | ExprBrewingSlot | brewing slot |
| 56 | ExprBrewingTime | brewing time |
| 57 | ExprBrewingResults | brewing results |

### Fishing Expressions

| # | Class | Description |
|---|-------|-------------|
| 58 | ExprFishingHook | fishing hook |
| 59 | ExprFishingApproachAngle | fishing approach angle |
| 60 | ExprFishingBiteTime | fishing bite time |
| 61 | ExprFishingHookEntity | fishing hook entity |
| 62 | ExprFishingWaitTime | fishing wait time |

### Breeding Expressions

| # | Class | Description |
|---|-------|-------------|
| 63 | ExprLoveTime | love time |
| 64 | ExprBreedingFamily | breeding family (mother/father/baby) |

### Potion Expressions

| # | Class | Description |
|---|-------|-------------|
| 65 | ExprPotionEffects | potion effects |
| 66 | ExprPotionDuration | potion duration |
| 67 | ExprPotionAmplifier | potion amplifier |
| 68 | ExprPotionEffect | potion effect type |
| 69 | ExprPotionEffectTypeCategory | potion effect type category |
| 70 | ExprSkriptPotionEffect | skript potion effect |
| 71 | ExprSecPotionEffect | section potion effect |

### Loot Table Expressions

| # | Class | Description |
|---|-------|-------------|
| 72 | ExprLootTable | loot table of |
| 73 | ExprLootTableSeed | loot table seed |
| 74 | ExprLootTableFromString | loot table from string |
| 75 | ExprSecCreateLootContext | section loot context |
| 76 | ExprLoot | loot |
| 77 | ExprLootContext | loot context |
| 78 | ExprLootContextEntity | loot context entity |
| 79 | ExprLootContextLocation | loot context location |
| 80 | ExprLootContextLooter | loot context looter |
| 81 | ExprLootContextLuck | loot context luck |
| 82 | ExprLootItems | loot items |

### Input Expressions

| # | Class | Description |
|---|-------|-------------|
| 83 | ExprCurrentInputKeys | current input keys |

### Interaction Expressions

| # | Class | Description |
|---|-------|-------------|
| 84 | ExprInteractionDimensions | interaction dimensions |
| 85 | ExprLastInteractionPlayer | last interaction player |
| 86 | ExprLastInteractionDate | last interaction date |

### Equippable Component Expressions

| # | Class | Description |
|---|-------|-------------|
| 87 | ExprEquippableComponent | equippable component of |
| 88 | ExprSecBlankEquipComp | blank equippable component |
| 89 | ExprEquipCompCameraOverlay | equip comp camera overlay |
| 90 | ExprEquipCompEntities | equip comp entities |
| 91 | ExprEquipCompEquipSound | equip comp equip sound |
| 92 | ExprEquipCompModel | equip comp model |
| 93 | ExprEquipCompShearSound | equip comp shear sound |
| 94 | ExprEquipCompSlot | equip comp slot |
| 95 | ExprItemCompCopy | item comp copy |

### Tag Expressions

| # | Class | Description |
|---|-------|-------------|
| 96 | ExprTag | tag |
| 97 | ExprTagKey | tag key |
| 98 | ExprTagContents | tag contents |
| 99 | ExprTagsOf | tags of |
| 100 | ExprTagsOfType | tags of type |

### Particle Expressions

| # | Class | Description |
|---|-------|-------------|
| 101 | ExprGameEffectWithData | particle with data |
| 102 | ExprParticleCount | particle count |
| 103 | ExprParticleDistribution | particle distribution |
| 104 | ExprParticleOffset | particle offset |
| 105 | ExprParticleSpeed | particle speed |
| 106 | ExprParticleWithData | particle with data |
| 107 | ExprParticleWithOffset | particle with offset |
| 108 | ExprParticleWithSpeed | particle with speed |

### Quaternion Expressions

| # | Class | Description |
|---|-------|-------------|
| 109 | ExprQuaternionAxisAngle | quaternion axis angle |
| 110 | ExprRotate | rotate |

### Entity Expressions

| # | Class | Description |
|---|-------|-------------|
| 111 | ExprAttacked | attacked entity |
| 112 | ExprAttacker | attacker |
| 113 | ExprEntity | entity |
| 114 | ExprEntities | entities |
| 115 | ExprNearestEntity | nearest entity |
| 116 | ExprTarget | target |
| 117 | ExprShooter | shooter |
| 118 | ExprPassenger | passenger |
| 119 | ExprVehicle | vehicle |
| 120 | ExprLastSpawnedEntity | last spawned entity |
| 121 | ExprItemOfEntity | item of entity |
| 122 | ExprEntitySnapshot | entity snapshot |
| 123 | ExprSpawnEggEntity | spawn egg entity |
| 124 | ExprWardenAngryAt | warden angry at |
| 125 | ExprWardenEntityAnger | warden entity anger |
| 126 | ExprEntitySound | entity sound |
| 127 | ExprSpectatorTarget | spectator target |

### Player Expressions

| # | Class | Description |
|---|-------|-------------|
| 128 | ExprMe | me |
| 129 | ExprIP | IP address |
| 130 | ExprPing | ping |
| 131 | ExprLanguage | language |
| 132 | ExprClientViewDistance | client view distance |
| 133 | ExprViewDistance | view distance |
| 134 | ExprSimulationDistance | simulation distance |
| 135 | ExprPlayerProtocolVersion | player protocol version |
| 136 | ExprHostname | hostname |
| 137 | ExprCompassTarget | compass target |
| 138 | ExprBed | bed |
| 139 | ExprSpeed | walk/fly speed |
| 140 | ExprGameMode | game mode |
| 141 | ExprTablistName | tablist name |
| 142 | ExprPlayerlistHeaderFooter | player list header/footer |
| 143 | ExprPlayerChatCompletions | player chat completions |
| 144 | ExprHiddenPlayers | hidden players |
| 145 | ExprTablistedPlayers | tablisted players |
| 146 | ExprOfflinePlayers | offline players |
| 147 | ExprOnlinePlayersCount | online players count |
| 148 | ExprOps | ops |
| 149 | ExprWhitelist | whitelist |
| 150 | ExprPermissions | permissions |
| 151 | ExprReadiedArrow | readied arrow |

### Location & World Expressions

| # | Class | Description |
|---|-------|-------------|
| 152 | ExprLocation | location |
| 153 | ExprLocationAt | location at |
| 154 | ExprLocationOf | location of |
| 155 | ExprLocationFromVector | location from vector |
| 156 | ExprLocationVectorOffset | location vector offset |
| 157 | ExprMiddleOfLocation | middle of location |
| 158 | ExprEyeLocation | eye location |
| 159 | ExprCoordinate | x/y/z coordinate |
| 160 | ExprYawPitch | yaw/pitch |
| 161 | ExprAltitude | altitude |
| 162 | ExprDirection | direction |
| 163 | ExprFacing | facing |
| 164 | ExprDistance | distance |
| 165 | ExprMidpoint | midpoint |
| 166 | ExprWorld | world |
| 167 | ExprWorlds | worlds |
| 168 | ExprWorldFromName | world from name |
| 169 | ExprSpawn | spawn |
| 170 | ExprBiome | biome |
| 171 | ExprTemperature | temperature |
| 172 | ExprHumidity | humidity |
| 173 | ExprSeaLevel | sea level |
| 174 | ExprSeed | world seed |
| 175 | ExprDifficulty | difficulty |
| 176 | ExprTime | time |
| 177 | ExprTimeState | time state (day/night) |
| 178 | ExprWeather | weather |
| 179 | ExprGameRule | game rule |
| 180 | ExprLowestHighestSolidBlock | lowest/highest solid block |

### Block Expressions

| # | Class | Description |
|---|-------|-------------|
| 181 | ExprBlock | block |
| 182 | ExprBlockSphere | block sphere |
| 183 | ExprBlockData | block data |
| 184 | ExprCarryingBlockData | carrying block data |
| 185 | ExprTargetedBlock | targeted block |
| 186 | ExprSourceBlock | source block |
| 187 | ExprRedstoneBlockPower | redstone block power |
| 188 | ExprLightLevel | light level |
| 189 | ExprSignText | sign text |
| 190 | ExprSeaPickles | sea pickles |
| 191 | ExprFertilizedBlocks | fertilized blocks |

### Item Expressions

| # | Class | Description |
|---|-------|-------------|
| 192 | ExprTool | tool |
| 193 | ExprClicked | clicked item/slot/type |
| 194 | ExprDrops | drops |
| 195 | ExprBarterDrops | barter drops |
| 196 | ExprBarterInput | barter input |
| 197 | ExprConsumedItem | consumed item |
| 198 | ExprHanging | hanging entity |
| 199 | ExprItemCooldown | item cooldown |
| 200 | ExprMaxDurability | max durability |
| 201 | ExprMaxItemUseTime | max item use time |
| 202 | ExprMaxStack | max stack size |
| 203 | ExprSkull | skull |
| 204 | ExprSkullOwner | skull owner |
| 205 | ExprDurability | durability |
| 206 | ExprItemAmount | item amount |
| 207 | ExprCustomModelData | custom model data |
| 208 | ExprExactItem | exact item |
| 209 | ExprRecursive | recursive |
| 210 | ExprLore | lore |
| 211 | ExprBookAuthor | book author |
| 212 | ExprBookTitle | book title |
| 213 | ExprBookPages | book pages |

### Living Entity Expressions

| # | Class | Description |
|---|-------|-------------|
| 214 | ExprAI | AI state |
| 215 | ExprAttackCooldown | attack cooldown |
| 216 | ExprHealth | health |
| 217 | ExprMaxHealth | max health |
| 218 | ExprFoodLevel | food level |
| 219 | ExprSaturation | saturation |
| 220 | ExprExhaustion | exhaustion |
| 221 | ExprRemainingAir | remaining air |
| 222 | ExprFallDistance | fall distance |
| 223 | ExprFireTicks | fire ticks |
| 224 | ExprFlightMode | flight mode |
| 225 | ExprFreezeTicks | freeze ticks |
| 226 | ExprMaxFreezeTicks | max freeze ticks |
| 227 | ExprGravity | gravity |
| 228 | ExprLastDamage | last damage |
| 229 | ExprLevelProgress | level progress |
| 230 | ExprNoDamageTicks | no damage ticks |
| 231 | ExprAge | age |
| 232 | ExprArrowsStuck | arrows stuck |
| 233 | ExprGlidingState | gliding state |
| 234 | ExprTimeLived | time lived |
| 235 | ExprPickupDelay | pickup delay |
| 236 | ExprPortalCooldown | portal cooldown |
| 237 | ExprBreakSpeed | break speed |
| 238 | ExprTotalExperience | total experience |
| 239 | ExprPandaGene | panda gene |
| 240 | ExprItemOwner | item owner |
| 241 | ExprItemThrower | item thrower |

### Arrow/Projectile Expressions

| # | Class | Description |
|---|-------|-------------|
| 242 | ExprArrowKnockbackStrength | arrow knockback strength |
| 243 | ExprArrowPierceLevel | arrow pierce level |
| 244 | ExprProjectileForce | projectile force |

### Explosion Expressions

| # | Class | Description |
|---|-------|-------------|
| 245 | ExprExplosionBlockYield | explosion block yield |
| 246 | ExprExplosionYield | explosion yield |
| 247 | ExprExplosiveYield | explosive yield |
| 248 | ExprExplodedBlocks | exploded blocks |

### Experience Expressions

| # | Class | Description |
|---|-------|-------------|
| 249 | ExprExperience | experience |
| 250 | ExprLevel | level |
| 251 | ExprExperienceCooldownChangeReason | experience cooldown change reason |

### Server Expressions

| # | Class | Description |
|---|-------|-------------|
| 252 | ExprMOTD | MOTD |
| 253 | ExprMaxPlayers | max players |
| 254 | ExprProtocolVersion | protocol version |
| 255 | ExprTPS | TPS |
| 256 | ExprVersion | version |
| 257 | ExprVersionString | version string |
| 258 | ExprLastLoadedServerIcon | last loaded server icon |
| 259 | ExprServerIcon | server icon |
| 260 | ExprHoverList | hover list |

### Vector Expressions

| # | Class | Description |
|---|-------|-------------|
| 261 | ExprVectorBetweenLocations | vector between locations |
| 262 | ExprVectorCrossProduct | vector cross product |
| 263 | ExprVectorDotProduct | vector dot product |
| 264 | ExprVectorLength | vector length |
| 265 | ExprVectorNormalize | vector normalize |
| 266 | ExprVectorFromYawAndPitch | vector from yaw and pitch |
| 267 | ExprVectorSpherical | vector from spherical |

### Utility Expressions

| # | Class | Description |
|---|-------|-------------|
| 268 | ExprLength | length of |
| 269 | ExprAlphabetList | alphabet list |
| 270 | ExprNow | now |
| 271 | ExprRandomNumber | random number |
| 272 | ExprRandomUUID | random UUID |
| 273 | ExprRandomCharacter | random character |
| 274 | ExprRound | round/floor/ceil |
| 275 | ExprInverse | inverse |
| 276 | ExprDifference | difference |
| 277 | ExprJoinSplit | join/split |
| 278 | ExprStringCase | upper/lower case |
| 279 | ExprSubstring | substring |
| 280 | ExprNumberOfCharacters | number of characters |
| 281 | ExprCharacters | characters |
| 282 | ExprDefaultValue | default value |
| 283 | ExprUUID | UUID of |
| 284 | ExprFromUUID | entity from UUID |
| 285 | ExprIndices | indices of |
| 286 | ExprIndicesOfValue | indices of value |
| 287 | ExprReversedList | reversed list |
| 288 | ExprSortedList | sorted list |
| 289 | ExprShuffledList | shuffled list |
| 290 | ExprExcept | except |
| 291 | ExprAnyOf | any of |
| 292 | ExprHash | hash |
| 293 | ExprPercent | percent |
| 294 | ExprRepeat | repeat |
| 295 | ExprFilter | filter |
| 296 | ExprTernary | ternary |
| 297 | ExprWhether | whether |
| 298 | ExprSets | sets (union/intersection/difference) |
| 299 | ExprFormatDate | format date |
| 300 | ExprDateAgoLater | date ago/later |
| 301 | ExprTimeSince | time since |
| 302 | ExprTimespanDetails | timespan details |
| 303 | ExprCodepoint | codepoint |
| 304 | ExprCharacterFromCodepoint | character from codepoint |
| 305 | ExprColoured | colored text |
| 306 | ExprRawString | raw string |
| 307 | ExprDebugInfo | debug info |
| 308 | ExprUnixDate | unix date |
| 309 | ExprUnixTicks | unix ticks |
| 310 | ExprDequeuedQueue | dequeued queue |
| 311 | ExprQueue | queue |
| 312 | ExprQueueStartEnd | queue start/end |
| 313 | ExprTimes | times |
| 314 | ExprArithmetic | arithmetic (+, -, *, /) |
| 315 | ExprLoopIteration | loop iteration |
| 316 | ExprLoopValue | loop value |

### Misc Expressions

| # | Class | Description |
|---|-------|-------------|
| 317 | ExprArgument | argument |
| 318 | ExprCommand | command |
| 319 | ExprCommandInfo | command info |
| 320 | ExprCmdCooldownInfo | command cooldown info |
| 321 | ExprCommandSender | command sender |
| 322 | ExprCommandBlockCommand | command block command |
| 323 | ExprName | name of |
| 324 | ExprNamed | named item |
| 325 | ExprRawName | raw name |
| 326 | ExprTextOf | text of |
| 327 | ExprTypeOf | type of |
| 328 | ExprValue | value |
| 329 | ExprElement | element |
| 330 | ExprNumbers | numbers |
| 331 | ExprPlain | plain text |
| 332 | ExprParse | parse |
| 333 | ExprParseError | parse error |
| 334 | ExprResult | result |
| 335 | ExprMetadata | metadata |
| 336 | ExprConfig | config |
| 337 | ExprNode | node |
| 338 | ExprScript | script |
| 339 | ExprScripts | scripts |
| 340 | ExprScriptsOld | scripts (old) |
| 341 | ExprMods | mods |
| 342 | ExprAffectedEntities | affected entities |
| 343 | ExprAppliedEffect | applied effect |
| 344 | ExprHealAmount | heal amount |
| 345 | ExprHealReason | heal reason |
| 346 | ExprHatchingNumber | hatching number |
| 347 | ExprHatchingType | hatching type |
| 348 | ExprLeashHolder | leash holder |
| 349 | ExprLastAttacker | last attacker |
| 350 | ExprSpawnReason | spawn reason |
| 351 | ExprSpawnerType | spawner type |
| 352 | ExprTeleportCause | teleport cause |
| 353 | ExprTamer | tamer |
| 354 | ExprPortal | portal |
| 355 | ExprQuitReason | quit reason |
| 356 | ExprResonatingTime | resonating time |
| 357 | ExprRingingTime | ringing time |
| 358 | ExprMessage | message |
| 359 | ExprChatFormat | chat format |
| 360 | ExprChatRecipients | chat recipients |
| 361 | ExprOnScreenKickMessage | on screen kick message |
| 362 | ExprSentCommands | sent commands |
| 363 | ExprCaughtErrors | caught errors |
| 364 | ExprMaxMinecartSpeed | max minecart speed |
| 365 | ExprMinecartDerailedFlyingVelocity | minecart derailed flying velocity |
| 366 | ExprTransform | transform |
| 367 | ExprValueWithin | value within |
| 368 | ExprRecursiveSize | recursive size |
| 369 | ExprWithFireResistance | with fire resistance |
| 370 | ExprWithItemFlags | with item flags |
| 371 | ExprXOf | x of |
| 372 | ExprXYZComponent | xyz component |
| 373 | ExprColorFromHexCode | color from hex code |
| 374 | ExprHexCode | hex code |
| 375 | ExprColorOf | color of |
| 376 | ExprEntityStorageEntityCount | entity storage entity count |
| 377 | ExprEvtInitiator | event initiator |
| 378 | ExprChunk | chunk |
| 379 | ExprChunkX | chunk x |
| 380 | ExprChunkZ | chunk z |
| 381 | ExprNewBannerPattern | new banner pattern |
| 382 | ExprBannerPatterns | banner patterns |
| 383 | ExprBannerItem | banner item |

### Property Expressions

| # | Class | Description |
|---|-------|-------------|
| 384 | PropExprName | name property |
| 385 | PropExprCustomName | custom name property |
| 386 | PropExprScale | scale property |
| 387 | PropExprAmount | amount property |
| 388 | PropExprNumber | number property |
| 389 | PropExprSize | size property |
| 390 | PropExprWXYZ | w/x/y/z component property |
| 391 | PropCondIsEmpty | is empty property condition |

---

## Summary

| Category | Count |
|----------|-------|
| Structures | 3 |
| Sections | 13 |
| ClassInfo Types | 26 |
| Events | 82 |
| Conditions | 162 |
| Effects | 113 |
| Expressions | 391 |
| **Total** | **790** |

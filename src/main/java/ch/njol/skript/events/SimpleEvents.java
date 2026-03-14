package ch.njol.skript.events;

public final class SimpleEvents {

    private SimpleEvents() {
    }

    public static synchronized void register() {
        EvtAreaCloudEffect.register();
        EvtBeaconEffect.register();
        EvtBeaconToggle.register();
        EvtBlockFertilize.register();
        EvtBlock.register();
        EvtBookEdit.register();
        EvtBookSign.register();
        EvtClick.register();
        EvtEntity.register();
        EvtExplode.register();
        EvtEntityShootBow.register();
        EvtEntityTransform.register();
        EvtExplosionPrime.register();
        EvtExperienceChange.register();
        EvtExperienceCooldownChange.register();
        EvtExperienceSpawn.register();
        EvtConnect.register();
        EvtFirstJoin.register();
        EvtJoin.register();
        EvtKick.register();
        EvtQuit.register();
        EvtFirework.register();
        EvtGameMode.register();
        EvtGrow.register();
        EvtHarvestBlock.register();
        EvtHealing.register();
        EvtItem.register();
        EvtLeash.register();
        EvtMove.register();
        EvtMoveOn.register();
        EvtPiglinBarter.register();
        EvtPlayerArmorChange.register();
        EvtPlayerChunkEnter.register();
        EvtPlayerEggThrow.register();
        EvtPlayerCommandSend.register();
        EvtPortal.register();
        EvtPressurePlate.register();
        EvtRespawn.register();
        EvtResourcePackResponse.register();
        EvtSpectate.register();
        EvtTeleport.register();
        EvtVehicleCollision.register();
        EvtWeatherChange.register();
        EvtWorld.register();
    }
}

package ch.njol.skript.events;

public final class SimpleEvents {

    private SimpleEvents() {
    }

    public static synchronized void register() {
        EvtBeaconEffect.register();
        EvtBeaconToggle.register();
        EvtBlock.register();
        EvtBookEdit.register();
        EvtBookSign.register();
        EvtClick.register();
        EvtEntity.register();
        EvtEntityShootBow.register();
        EvtEntityTransform.register();
        EvtExperienceChange.register();
        EvtExperienceSpawn.register();
        EvtFirstJoin.register();
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

package ch.njol.skript.effects;

final class FabricEffectEventHandles {

    private FabricEffectEventHandles() {
    }

    static final class PlayerRespawn {
    }

    static final class EntityDeath {
    }

    static final class ExplosionPrime {
    }

    static final class PlayerEggThrow {
    }

    static final class PlayerElytraBoost {

        private final boolean shouldConsume;

        PlayerElytraBoost(boolean shouldConsume) {
            this.shouldConsume = shouldConsume;
        }

        boolean shouldConsume() {
            return shouldConsume;
        }
    }

    static final class EntityUnleash {
    }
}

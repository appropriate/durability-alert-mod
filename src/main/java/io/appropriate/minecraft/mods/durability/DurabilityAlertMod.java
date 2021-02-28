package io.appropriate.minecraft.mods.durability;

import net.fabricmc.api.ClientModInitializer;

public class DurabilityAlertMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DurabilityAlertAttackBlockCallback.register();
    }
}

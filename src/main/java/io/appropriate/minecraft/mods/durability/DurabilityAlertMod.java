package io.appropriate.minecraft.mods.durability;

import net.fabricmc.api.ClientModInitializer;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class DurabilityAlertMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.register(DurabilityAlertConfig.class, GsonConfigSerializer::new);
        AutoConfig.getConfigHolder(DurabilityAlertConfig.class).getConfig();
        DurabilityAlertAttackBlockCallback.register();
    }
}

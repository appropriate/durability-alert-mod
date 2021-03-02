package io.appropriate.minecraft.mods.durability;

import net.minecraft.util.ActionResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ClientModInitializer;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Environment(EnvType.CLIENT)
public class DurabilityAlertMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.register(DurabilityAlertConfig.class, GsonConfigSerializer::new);

        ConfigHolder<DurabilityAlertConfig> configHolder =
            AutoConfig.getConfigHolder(DurabilityAlertConfig.class);

        final DurabilityAlertAttackBlockCallback callback =
            DurabilityAlertAttackBlockCallback.register(configHolder.getConfig());

        configHolder.registerSaveListener((manager, newData) -> {
            callback.updateConfig(newData);
            return ActionResult.SUCCESS;
        });
    }
}

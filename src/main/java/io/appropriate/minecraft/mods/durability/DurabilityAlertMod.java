package io.appropriate.minecraft.mods.durability;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

import java.util.Collections;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ClientModInitializer;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import io.appropriate.minecraft.clothconfig2.IntegerSliderListEntry;

@Environment(EnvType.CLIENT)
public class DurabilityAlertMod implements ClientModInitializer {
    private static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(DurabilityAlertConfig.class, GsonConfigSerializer::new);

        GuiRegistry registry = AutoConfig.getGuiRegistry(DurabilityAlertConfig.class);

        registry.registerPredicateProvider(
            (i13n, field, config, defaults, guiProvider) -> {
                return Collections.singletonList(
                    ENTRY_BUILDER.startEnumSelector(
                        new TranslatableText(i13n),
                        DurabilityAlertConfig.Material.class,
                        getUnsafely(field, config, getUnsafely(field, defaults))
                    )
                        .setDefaultValue(() -> getUnsafely(field, defaults))
                        .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                        .build()
                );
            },
            field -> field.getType() == DurabilityAlertConfig.Material.class && !field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class)
        );

        registry.registerAnnotationProvider(
            (i13n, field, config, defaults, guiProvider) -> {
                IntegerSliderListEntry entry = new IntegerSliderListEntry(
                    new TranslatableText(i13n), 0, 100,
                    getUnsafely(field, config, getUnsafely(field, defaults)),
                    false, null,
                    newValue -> setUnsafely(field, config, newValue),
                    () -> getUnsafely(field, defaults),
                    0, new TranslatableText("text.cloth-config.reset_value"),
                    false, true, false
                );

                entry.setTextGetter(number -> new LiteralText(number + "%"));

                return Collections.singletonList(entry);
            },
            DurabilityAlertConfig.IntSliderList.class
        );

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

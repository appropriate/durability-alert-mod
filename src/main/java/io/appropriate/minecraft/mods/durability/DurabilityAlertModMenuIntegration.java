package io.appropriate.minecraft.mods.durability;

import java.util.Collections;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class DurabilityAlertModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> { return buildScreen(parent); };
    }

    private Screen buildScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(new TranslatableText("title.durability-alert-mod.config"));

        /*
        builder.setSavingRunnable(() -> {
            // Serialise the config into the config file. This will be called last after all variables are updated.
        });
        */

        ConfigCategory alerts = builder.getOrCreateCategory(
            new TranslatableText("category.durability-alert-mod.alerts")
        );

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        alerts.addEntry(
            entryBuilder
                .startIntList(new TranslatableText("option.durability-alert-mod.alert-cutoffs"), Collections.<Integer>emptyList())
                .setDefaultValue(DurabilityAlertConfig.DEFAULT_ALERT_CUTOFFS)
                .setTooltip(new TranslatableText("tooltip.durability-alert-mod.alert-cutoffs"))
                .setMin(0)
                .setMax(100)
                //.setSaveConsumer(newValue -> newValue = newValue)
                .build()
        );

        // TODO: Let important materials to be modified; requires an EnumSetListEntry or something
        // See https://github.com/shedaniel/cloth-config/issues/52

        alerts.addEntry(
            entryBuilder
                .startBooleanToggle(new TranslatableText("option.durability-alert-mod.alert-all-named"), true)
                .setDefaultValue(DurabilityAlertConfig.DEFAULT_ALERT_ALL_NAMED)
                .setTooltip(new TranslatableText("tooltip.durability-alert-mod.alert-all-named"))
                //.setSaveConsumer(newValue -> newValue = newValue)
                .build()
        );

        alerts.addEntry(
            entryBuilder
                .startBooleanToggle(new TranslatableText("option.durability-alert-mod.alert-all-enchanted"), true)
                .setDefaultValue(DurabilityAlertConfig.DEFAULT_ALERT_ALL_ENCHANTED)
                .setTooltip(new TranslatableText("tooltip.durability-alert-mod.alert-all-enchanted"))
                //.setSaveConsumer(newValue -> newValue = newValue)
                .build()
        );

        return builder.build();
    }
}

package io.appropriate.minecraft.mods.durability;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;
import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

import io.appropriate.minecraft.clothconfig2.IntegerSliderListEntry;
import java.util.Collections;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.util.ActionResult;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

/**
 * A Minecraft client-side mod that alerts a user each time the durability of a held item falls
 * below certain durability thresholds.
 */
@ClientOnly
public class DurabilityAlertMod implements ClientModInitializer {
  private static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

  /**
   * Creates a new instance of the mod.
   */
  public DurabilityAlertMod() {
  }

  /**
   * Initializes the mod on client startup.
   *
   * <p>Registers handlers related to the mod's configuration screen and settings and installs a
   * {@link DurabilityAlertAttackCallback} to be notified each time a held item is used to attack a
   * block or entity.
   *
   * @param mod the mod which is initialized
   */
  @Override
  public void onInitializeClient(ModContainer mod) {
    AutoConfig.register(DurabilityAlertConfig.class, GsonConfigSerializer::new);

    var registry = AutoConfig.getGuiRegistry(DurabilityAlertConfig.class);

    registry.registerPredicateProvider(
        (i13n, field, config, defaults, guiProvider) -> {
          return Collections.singletonList(
            ENTRY_BUILDER.startEnumSelector(
              translatable(i13n),
              DurabilityAlertConfig.Material.class,
              getUnsafely(field, config, getUnsafely(field, defaults))
            )
              .setDefaultValue(() -> getUnsafely(field, defaults))
              .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
              .build()
          );
        },
        field -> {
          return field.getType() == DurabilityAlertConfig.Material.class
              && !field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class);
        }
    );

    registry.registerAnnotationProvider(
        (i13n, field, config, defaults, guiProvider) -> {
          IntegerSliderListEntry entry = new IntegerSliderListEntry(
              translatable(i13n), 0, 100,
              getUnsafely(field, config, getUnsafely(field, defaults)),
              false, null,
              newValue -> setUnsafely(field, config, newValue),
              () -> getUnsafely(field, defaults),
              0, translatable("text.cloth-config.reset_value"),
              false, true, false
          );

          entry.setTextGetter(number -> literal(number + "%"));

          return Collections.singletonList(entry);
        },
        DurabilityAlertConfig.IntSliderList.class
    );

    var configHolder = AutoConfig.getConfigHolder(DurabilityAlertConfig.class);

    final var callback = DurabilityAlertAttackCallback.register(configHolder.getConfig());

    configHolder.registerSaveListener((manager, newData) -> {
      callback.updateConfig(newData);
      return ActionResult.SUCCESS;
    });
  }
}

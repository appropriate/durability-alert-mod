package io.appropriate.minecraft.mods.durability;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

/**
 * An integration point for ModMenu, providing a factory function that builds a config screen for
 * this mod when called.
 */
@Environment(EnvType.CLIENT)
public class DurabilityAlertModMenuIntegration implements ModMenuApi {
  /**
   * Creates a new ModMenu integration point.
   */
  public DurabilityAlertModMenuIntegration() {
  }

  /**
   * Builds a factory function to create this mod's config screen, given a parent {@link Screen}.
   */
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> AutoConfig.getConfigScreen(DurabilityAlertConfig.class, parent).get();
  }
}

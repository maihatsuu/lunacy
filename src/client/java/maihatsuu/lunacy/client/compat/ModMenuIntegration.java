package maihatsuu.lunacy.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import maihatsuu.lunacy.client.config.LunacyConfigScreen;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Only loaded when Mod Menu is installed. Cloth Config is optional too, so the screen class
 * (which references Cloth) is only touched once we know Cloth is present.
 */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
			return parent -> null;
		}
		return LunacyConfigScreen::create;
	}
}

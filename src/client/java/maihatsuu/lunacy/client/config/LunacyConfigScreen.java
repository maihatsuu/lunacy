package maihatsuu.lunacy.client.config;

import maihatsuu.lunacy.config.LunacyConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class LunacyConfigScreen {
	private static final String KEY = "config.lunacy.";

	private LunacyConfigScreen() {
	}

	public static Screen create(Screen parent) {
		LunacyConfig config = LunacyConfig.get();
		LunacyConfig defaults = new LunacyConfig();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable(KEY + "title"))
				.setSavingRunnable(LunacyConfig::save);
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory fullMoon = builder.getOrCreateCategory(Component.translatable(KEY + "category.fullMoon"));
		fullMoon.addEntry(entries.startBooleanToggle(label("fullMoonEvents"), config.fullMoonEvents)
				.setDefaultValue(defaults.fullMoonEvents)
				.setTooltip(tooltip("fullMoonEvents"))
				.setSaveConsumer(v -> config.fullMoonEvents = v)
				.build());
		fullMoon.addEntry(entries.startDoubleField(label("fullMoonMonsterCapMultiplier"), config.fullMoonMonsterCapMultiplier)
				.setDefaultValue(defaults.fullMoonMonsterCapMultiplier)
				.setMin(1.0)
				.setMax(10.0)
				.setTooltip(tooltip("fullMoonMonsterCapMultiplier"))
				.setSaveConsumer(v -> config.fullMoonMonsterCapMultiplier = v)
				.build());
		fullMoon.addEntry(entries.startBooleanToggle(label("fullMoonSkyOnly"), config.fullMoonSkyOnly)
				.setDefaultValue(defaults.fullMoonSkyOnly)
				.setTooltip(tooltip("fullMoonSkyOnly"))
				.setSaveConsumer(v -> config.fullMoonSkyOnly = v)
				.build());
		fullMoon.addEntry(entries.startBooleanToggle(label("fullMoonMessage"), config.fullMoonMessage)
				.setDefaultValue(defaults.fullMoonMessage)
				.setTooltip(tooltip("fullMoonMessage"))
				.setSaveConsumer(v -> config.fullMoonMessage = v)
				.build());

		ConfigCategory newMoon = builder.getOrCreateCategory(Component.translatable(KEY + "category.newMoon"));
		newMoon.addEntry(entries.startBooleanToggle(label("newMoonEvents"), config.newMoonEvents)
				.setDefaultValue(defaults.newMoonEvents)
				.setTooltip(tooltip("newMoonEvents"))
				.setSaveConsumer(v -> config.newMoonEvents = v)
				.build());
		newMoon.addEntry(entries.startBooleanToggle(label("newMoonMessage"), config.newMoonMessage)
				.setDefaultValue(defaults.newMoonMessage)
				.setTooltip(tooltip("newMoonMessage"))
				.setSaveConsumer(v -> config.newMoonMessage = v)
				.build());
		newMoon.addEntry(entries.startDoubleField(label("starsPerNight"), config.starsPerNight)
				.setDefaultValue(defaults.starsPerNight)
				.setMin(0.0)
				.setMax(100.0)
				.setTooltip(tooltip("starsPerNight"))
				.setSaveConsumer(v -> config.starsPerNight = v)
				.build());
		newMoon.addEntry(entries.startIntField(label("starMinDistance"), config.starMinDistance)
				.setDefaultValue(defaults.starMinDistance)
				.setMin(0)
				.setMax(256)
				.setTooltip(tooltip("starMinDistance"))
				.setSaveConsumer(v -> config.starMinDistance = v)
				.build());
		newMoon.addEntry(entries.startIntField(label("starMaxDistance"), config.starMaxDistance)
				.setDefaultValue(defaults.starMaxDistance)
				.setMin(0)
				.setMax(256)
				.setTooltip(tooltip("starMaxDistance"))
				.setSaveConsumer(v -> config.starMaxDistance = v)
				.build());
		newMoon.addEntry(entries.startBooleanToggle(label("starDropsFadeAtDawn"), config.starDropsFadeAtDawn)
				.setDefaultValue(defaults.starDropsFadeAtDawn)
				.setTooltip(tooltip("starDropsFadeAtDawn"))
				.setSaveConsumer(v -> config.starDropsFadeAtDawn = v)
				.build());

		ConfigCategory shards = builder.getOrCreateCategory(Component.translatable(KEY + "category.echoShards"));
		shards.addEntry(entries.startIntField(label("echoShardXp"), config.echoShardXp)
				.setDefaultValue(defaults.echoShardXp)
				.setMin(0)
				.setTooltip(tooltip("echoShardXp"))
				.setSaveConsumer(v -> config.echoShardXp = v)
				.build());

		return builder.build();
	}

	private static Component label(String option) {
		return Component.translatable(KEY + "option." + option);
	}

	private static Component tooltip(String option) {
		return Component.translatable(KEY + "option." + option + ".tooltip");
	}
}

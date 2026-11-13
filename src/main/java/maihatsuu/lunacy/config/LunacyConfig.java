package maihatsuu.lunacy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import maihatsuu.lunacy.Lunacy;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class LunacyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(Lunacy.MOD_ID + ".json");
	private static LunacyConfig instance = new LunacyConfig();

	/** Raise the monster cap on full moon nights. */
	public boolean fullMoonEvents = true;
	/** Full moon events don't happen before this day. 8 skips the world's first night (a full moon). */
	public int fullMoonGraceDays = 8;
	/** Monster cap multiplier on full moon nights. */
	public double fullMoonMonsterCapMultiplier = 1.5;
	/** Spawns above the vanilla cap only happen where the sky is visible. */
	public boolean fullMoonSkyOnly = true;
	/** Mobs in #lunacy:full_moon_hunters under the open sky always know where nearby players are. */
	public boolean fullMoonHunt = true;
	/** How far from a player hunting mobs are drawn in, in blocks. */
	public int fullMoonHuntRadius = 32;
	/** Creepers never target other monsters (always, not only on full moons). */
	public boolean creepersIgnoreMonsters = true;
	/** Action bar message when a full moon rises. */
	public boolean fullMoonMessage = false;

	/** Shooting stars on new moon nights. */
	public boolean newMoonEvents = true;
	/** Action bar message when a new moon night starts. */
	public boolean newMoonMessage = false;
	/** Average shooting stars per player (or group of nearby players) per new moon night. */
	public double starsPerNight = 3.0;
	/** Landing distance range from the player. */
	public int starMinDistance = 32;
	public int starMaxDistance = 96;
	/** Unclaimed star drops vanish at sunrise. */
	public boolean starDropsFadeAtDawn = true;

	/** Experience points from using an echo shard. 0 turns this off. */
	public int echoShardXp = 25;

	public static LunacyConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				LunacyConfig loaded = GSON.fromJson(reader, LunacyConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | JsonParseException e) {
				Lunacy.LOGGER.warn("Failed to read {}, using defaults", PATH, e);
				return;
			}
		}
		save();
	}

	/** Sanitizes the current values and writes them to disk. Settings take effect immediately. */
	public static void save() {
		instance.sanitize();
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			Lunacy.LOGGER.warn("Failed to write {}", PATH, e);
		}
	}

	private void sanitize() {
		fullMoonGraceDays = Math.clamp(fullMoonGraceDays, 0, 800);
		fullMoonMonsterCapMultiplier = Math.clamp(fullMoonMonsterCapMultiplier, 1.0, 10.0);
		fullMoonHuntRadius = Math.clamp(fullMoonHuntRadius, 8, 128);
		starsPerNight = Math.clamp(starsPerNight, 0.0, 100.0);
		starMinDistance = Math.clamp(starMinDistance, 0, 256);
		starMaxDistance = Math.clamp(starMaxDistance, starMinDistance, 256);
		echoShardXp = Math.max(0, echoShardXp);
	}
}

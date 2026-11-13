package maihatsuu.lunacy.spawn;

import maihatsuu.lunacy.MoonPhase;
import maihatsuu.lunacy.config.LunacyConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;

/**
 * State shared by the spawning mixins. Natural spawning only runs on the server thread, one
 * chunk at a time, so plain static fields are enough.
 */
public final class FullMoonSurge {
	/** Multiplier applied to the monster cap while re-checking the cap for a chunk. */
	public static double capMultiplier = 1.0;
	/** The current chunk only passed the cap thanks to the surge: spawns there need sky access. */
	public static boolean skyOnly = false;

	private FullMoonSurge() {
	}

	public static boolean isActive(ServerLevel level) {
		LunacyConfig config = LunacyConfig.get();
		return config.fullMoonEvents && config.fullMoonMonsterCapMultiplier > 1.0 && MoonPhase.isFullMoonNight(level);
	}

	public static int scaleCap(int cap, MobCategory category) {
		if (category != MobCategory.MONSTER || capMultiplier == 1.0) {
			return cap;
		}
		return (int) Math.round(cap * capMultiplier);
	}
}

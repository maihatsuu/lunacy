package maihatsuu.lunacy;

import maihatsuu.lunacy.config.LunacyConfig;
import net.minecraft.world.level.Level;

/** Moon phase helpers. Lunacy only acts in the Overworld, the only dimension with a moon. */
public final class MoonPhase {
	public static final long DAY = 24000L;
	/** Time of day when night starts (matches Rebalanced Core's full-moon vault recharge). */
	public static final long NIGHT_START = 13000L;
	/** Time of day when night ends (sunrise). */
	public static final long NIGHT_END = 23000L;
	public static final int FULL = 0;
	public static final int NEW = 4;

	private MoonPhase() {
	}

	public static boolean isNight(Level level) {
		if (level.dimension() != Level.OVERWORLD) {
			return false;
		}
		long time = Math.floorMod(level.getDayTime(), DAY);
		return time >= NIGHT_START && time < NIGHT_END;
	}

	/**
	 * A full moon night, once the world is {@code fullMoonGraceDays} old. Vanilla worlds start on a
	 * full moon, so by default the first one (day 0) is skipped and the first is on day 8.
	 */
	public static boolean isFullMoonNight(Level level) {
		return isNight(level) && level.getMoonPhase() == FULL && nightIndex(level) >= LunacyConfig.get().fullMoonGraceDays;
	}

	public static boolean isNewMoonNight(Level level) {
		return isNight(level) && level.getMoonPhase() == NEW;
	}

	/** Day number; the same for a whole night, since nights don't cross a day boundary. */
	public static long nightIndex(Level level) {
		return Math.floorDiv(level.getDayTime(), DAY);
	}
}

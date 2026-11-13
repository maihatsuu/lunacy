package maihatsuu.lunacy;

import maihatsuu.lunacy.config.LunacyConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Action bar messages at moonrise on full and new moon nights. */
public final class LunarEvents {
	private static boolean wasFullMoonNight;
	private static boolean wasNewMoonNight;

	private LunarEvents() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(LunarEvents::tick);
	}

	private static void tick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD) {
			return;
		}
		boolean full = MoonPhase.isFullMoonNight(level);
		boolean fresh = MoonPhase.isNewMoonNight(level);
		LunacyConfig config = LunacyConfig.get();
		if (full && !wasFullMoonNight && config.fullMoonEvents && config.fullMoonMessage) {
			broadcast(level, Component.translatableWithFallback("lunacy.message.full_moon",
					"The full moon rises. Something stirs in the dark."));
		}
		if (fresh && !wasNewMoonNight && config.newMoonEvents && config.newMoonMessage) {
			broadcast(level, Component.translatableWithFallback("lunacy.message.new_moon",
					"The sky goes dark. Stars begin to fall."));
		}
		wasFullMoonNight = full;
		wasNewMoonNight = fresh;
	}

	private static void broadcast(ServerLevel level, Component message) {
		level.players().forEach(player -> player.displayClientMessage(message, true));
	}
}

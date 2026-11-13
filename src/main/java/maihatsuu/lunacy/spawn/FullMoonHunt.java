package maihatsuu.lunacy.spawn;

import maihatsuu.lunacy.Lunacy;
import maihatsuu.lunacy.MoonPhase;
import maihatsuu.lunacy.config.LunacyConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;

/**
 * Full moon nights: the hunt. Every mob in {@code #lunacy:full_moon_hunters} out under the open
 * sky within {@code fullMoonHuntRadius} of a player knows where that player is and comes for them,
 * with or without line of sight.
 * <p>
 * Vanilla target goals drop a target they can't see after a few seconds, so the hunt re-assigns
 * targets every second. Follow range is raised to at least the hunt radius (transient modifier,
 * never saved) so mobs keep chasing and pathfind across the whole distance.
 */
public final class FullMoonHunt {
	public static final TagKey<EntityType<?>> HUNTERS = TagKey.create(Registries.ENTITY_TYPE, Lunacy.id("full_moon_hunters"));
	private static final ResourceLocation MODIFIER_ID = Lunacy.id("full_moon_hunt");
	private static final int HUNT_INTERVAL = 20;

	private static boolean wasActive;

	private FullMoonHunt() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(FullMoonHunt::tick);
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Mob mob && isActive(level)) {
				setFollowRange(mob, true);
			}
		});
	}

	public static boolean isActive(Level level) {
		LunacyConfig config = LunacyConfig.get();
		return config.fullMoonEvents && config.fullMoonHunt && level.getDifficulty() != Difficulty.PEACEFUL
				&& MoonPhase.isFullMoonNight(level);
	}

	private static void tick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD) {
			return;
		}
		boolean active = isActive(level);
		if (active != wasActive) {
			// Moonrise or dawn: add or remove the follow range bonus on every loaded hunter.
			for (Mob mob : level.getEntities(EntityTypeTest.forClass(Mob.class), mob -> mob.getType().is(HUNTERS))) {
				setFollowRange(mob, active);
			}
			wasActive = active;
		}
		if (active && level.getGameTime() % HUNT_INTERVAL == 0) {
			hunt(level);
		}
	}

	private static void hunt(ServerLevel level) {
		double radius = LunacyConfig.get().fullMoonHuntRadius;
		for (ServerPlayer player : level.players()) {
			if (!isPrey(player)) {
				continue;
			}
			for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius), FullMoonHunt::isHunter)) {
				if (mob.distanceToSqr(player) > radius * radius) {
					continue;
				}
				setFollowRange(mob, true);
				LivingEntity current = mob.getTarget();
				// Keep a mob on the player it's already after; otherwise send it at the closest one.
				if (current instanceof Player other && isPrey(other) && current != player
						&& mob.distanceToSqr(other) <= mob.distanceToSqr(player)) {
					continue;
				}
				if (current != player) {
					mob.setTarget(player);
				}
			}
		}
	}

	private static boolean isHunter(Mob mob) {
		return mob.isAlive() && !mob.isNoAi() && mob.getType().is(HUNTERS)
				&& mob.level().canSeeSky(mob.blockPosition());
	}

	private static boolean isPrey(Player player) {
		return player.isAlive() && !player.isCreative() && !player.isSpectator();
	}

	private static void setFollowRange(Mob mob, boolean active) {
		if (!mob.getType().is(HUNTERS)) {
			return;
		}
		AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
		if (followRange == null) {
			return;
		}
		AttributeModifier current = followRange.getModifier(MODIFIER_ID);
		if (!active) {
			if (current != null) {
				followRange.removeModifier(MODIFIER_ID);
			}
			return;
		}
		double bonus = Math.max(0.0, LunacyConfig.get().fullMoonHuntRadius + 8.0 - followRange.getBaseValue());
		if (current == null || current.amount() != bonus) {
			followRange.removeModifier(MODIFIER_ID);
			followRange.addTransientModifier(new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
		}
	}
}

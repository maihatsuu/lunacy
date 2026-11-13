package maihatsuu.lunacy.star;

import maihatsuu.lunacy.Lunacy;
import maihatsuu.lunacy.MoonPhase;
import maihatsuu.lunacy.config.LunacyConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** New moon nights: shooting stars fall near players and leave a glowing drop that fades at sunrise. */
public final class ShootingStars {
	public static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, Lunacy.id("gameplay/shooting_star"));
	public static final String DROP_TAG = "lunacy.star_drop";
	private static final String NIGHT_TAG_PREFIX = "lunacy.star_night.";
	private static final double NIGHT_LENGTH = MoonPhase.NIGHT_END - MoonPhase.NIGHT_START;
	private static final int TRAIL_POINTS_PER_TICK = 6;
	private static final int LANDING_ATTEMPTS = 5;

	private static final List<FallingStar> FALLING = new ArrayList<>();
	private static final List<ItemEntity> TO_FADE = new ArrayList<>();

	private ShootingStars() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(ShootingStars::tick);
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof ItemEntity item && isStale(item, level)) {
				TO_FADE.add(item);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			FALLING.clear();
			TO_FADE.clear();
		});
	}

	private static void tick(ServerLevel level) {
		if (level.dimension() != Level.OVERWORLD) {
			return;
		}
		LunacyConfig config = LunacyConfig.get();
		if (config.newMoonEvents && MoonPhase.isNewMoonNight(level)) {
			rollNewStars(level, config);
		}
		tickFalling(level);
		fadeDrops(level);
	}

	// Scheduling

	private static void rollNewStars(ServerLevel level, LunacyConfig config) {
		if (config.starsPerNight <= 0) {
			return;
		}
		RandomSource random = level.random;
		double range = config.starMaxDistance;
		for (ServerPlayer player : level.players()) {
			if (player.isSpectator()) {
				continue;
			}
			// Nearby players share stars: split the chance across everyone within range.
			int nearby = 0;
			for (ServerPlayer other : level.players()) {
				if (!other.isSpectator() && other.distanceToSqr(player) <= range * range) {
					nearby++;
				}
			}
			double chance = config.starsPerNight / NIGHT_LENGTH / Math.max(1, nearby);
			if (random.nextDouble() < chance) {
				launch(level, player, config);
			}
		}
	}

	/** Starts a shooting star that lands near the player. Returns false if no landing spot was found. */
	public static boolean launch(ServerLevel level, ServerPlayer player, LunacyConfig config) {
		BlockPos landing = findLanding(level, player, config);
		if (landing == null) {
			return false;
		}
		RandomSource random = level.random;
		Vec3 end = Vec3.atBottomCenterOf(landing);
		double angle = random.nextDouble() * Math.PI * 2;
		double distance = Mth.nextDouble(random, 80, 120);
		double y = Math.min(end.y + 150, level.getMaxBuildHeight() - 1);
		Vec3 start = new Vec3(end.x + Math.cos(angle) * distance, y, end.z + Math.sin(angle) * distance);
		FALLING.add(new FallingStar(start, end));
		return true;
	}

	private static BlockPos findLanding(ServerLevel level, ServerPlayer player, LunacyConfig config) {
		RandomSource random = level.random;
		for (int i = 0; i < LANDING_ATTEMPTS; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double distance = Mth.nextDouble(random, config.starMinDistance, config.starMaxDistance);
			int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
			int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
			BlockPos column = new BlockPos(x, player.getBlockY(), z);
			if (!level.isLoaded(column)) {
				continue;
			}
			BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
			if (surface.getY() <= level.getMinBuildHeight()) {
				continue;
			}
			if (!level.getFluidState(surface).isEmpty() || !level.getFluidState(surface.below()).isEmpty()) {
				continue;
			}
			return surface;
		}
		return null;
	}

	// Flight and impact

	private static void tickFalling(ServerLevel level) {
		Iterator<FallingStar> it = FALLING.iterator();
		while (it.hasNext()) {
			FallingStar star = it.next();
			double from = (double) star.age / FallingStar.FALL_TICKS;
			star.age++;
			double to = (double) star.age / FallingStar.FALL_TICKS;
			for (int i = 1; i <= TRAIL_POINTS_PER_TICK; i++) {
				Vec3 p = star.at(Mth.lerp((double) i / TRAIL_POINTS_PER_TICK, from, to));
				sendForced(level, ParticleTypes.END_ROD, p, 1, 0.0, 0.0);
				if (i % 2 == 0) {
					sendForced(level, ParticleTypes.FIREWORK, p, 1, 0.15, 0.01);
				}
			}
			if (star.landed()) {
				it.remove();
				impact(level, star.end);
			}
		}
	}

	private static void impact(ServerLevel level, Vec3 pos) {
		Vec3 flash = pos.add(0, 0.5, 0);
		sendForced(level, ParticleTypes.FLASH, flash, 1, 0.0, 0.0);
		sendForced(level, ParticleTypes.FIREWORK, flash, 40, 0.4, 0.15);
		sendForced(level, ParticleTypes.END_ROD, flash, 20, 0.3, 0.08);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundSource.AMBIENT, 6.0F, 0.8F);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 3.0F, 0.6F);
		dropLoot(level, pos);
	}

	private static void dropLoot(ServerLevel level, Vec3 pos) {
		if (!level.isLoaded(BlockPos.containing(pos))) {
			return;
		}
		LootTable table = level.getServer().reloadableRegistries().getLootTable(LOOT_TABLE);
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.ORIGIN, pos)
				.create(LootContextParamSets.CHEST);
		String nightTag = NIGHT_TAG_PREFIX + MoonPhase.nightIndex(level);
		for (ItemStack stack : table.getRandomItems(params)) {
			ItemEntity item = new ItemEntity(level, pos.x, pos.y + 0.25, pos.z, stack, 0.0, 0.2, 0.0);
			item.setGlowingTag(true);
			item.setUnlimitedLifetime();
			item.addTag(DROP_TAG);
			item.addTag(nightTag);
			level.addFreshEntity(item);
		}
	}

	/** Sends particles with force = true, so every Overworld player within 512 blocks sees them. */
	private static void sendForced(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, double spread, double speed) {
		for (ServerPlayer player : level.players()) {
			level.sendParticles(player, particle, true, pos.x, pos.y, pos.z, count, spread, spread, spread, speed);
		}
	}

	// Fading at dawn

	private static void fadeDrops(ServerLevel level) {
		if (!TO_FADE.isEmpty()) {
			for (ItemEntity item : TO_FADE) {
				if (item.isAlive() && item.level() instanceof ServerLevel itemLevel) {
					fade(itemLevel, item);
				}
			}
			TO_FADE.clear();
		}
		if (level.getGameTime() % 20 == 0 && LunacyConfig.get().starDropsFadeAtDawn) {
			for (ItemEntity item : level.getEntities(EntityTypeTest.forClass(ItemEntity.class), item -> isStale(item, level))) {
				fade(level, item);
			}
		}
	}

	private static boolean isStale(ItemEntity item, Level level) {
		if (!item.getTags().contains(DROP_TAG) || !LunacyConfig.get().starDropsFadeAtDawn) {
			return false;
		}
		return !MoonPhase.isNight(level) || !item.getTags().contains(NIGHT_TAG_PREFIX + MoonPhase.nightIndex(level));
	}

	private static void fade(ServerLevel level, Entity item) {
		level.sendParticles(ParticleTypes.POOF, item.getX(), item.getY() + 0.2, item.getZ(), 8, 0.15, 0.15, 0.15, 0.02);
		item.discard();
	}
}

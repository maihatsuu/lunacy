package maihatsuu.lunacy.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import maihatsuu.lunacy.config.LunacyConfig;
import maihatsuu.lunacy.spawn.FullMoonSurge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Full moon surge: when a chunk fails the vanilla monster cap, re-check it with the cap
 * multiplied. Spawns that only pass thanks to the surge must be at sky-visible positions.
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
	@WrapOperation(
			method = "spawnForChunk",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategory(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z")
	)
	private static boolean lunacy$fullMoonCap(NaturalSpawner.SpawnState state, MobCategory category, ChunkPos chunkPos,
			Operation<Boolean> original, @Local(argsOnly = true) ServerLevel level) {
		FullMoonSurge.skyOnly = false;
		if (original.call(state, category, chunkPos)) {
			return true;
		}
		if (category != MobCategory.MONSTER || !FullMoonSurge.isActive(level)) {
			return false;
		}
		boolean allowed;
		FullMoonSurge.capMultiplier = LunacyConfig.get().fullMoonMonsterCapMultiplier;
		try {
			allowed = original.call(state, category, chunkPos);
		} finally {
			FullMoonSurge.capMultiplier = 1.0;
		}
		if (allowed) {
			FullMoonSurge.skyOnly = LunacyConfig.get().fullMoonSkyOnly;
		}
		return allowed;
	}

	@WrapOperation(
			method = "spawnForChunk",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/NaturalSpawner;spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V")
	)
	private static void lunacy$skyOnly(MobCategory category, ServerLevel level, LevelChunk chunk,
			NaturalSpawner.SpawnPredicate predicate, NaturalSpawner.AfterSpawnCallback callback, Operation<Void> original) {
		NaturalSpawner.SpawnPredicate actual = predicate;
		if (FullMoonSurge.skyOnly) {
			FullMoonSurge.skyOnly = false;
			actual = (type, pos, chunkAccess) -> level.canSeeSky(pos) && predicate.test(type, pos, chunkAccess);
		}
		original.call(category, level, chunk, actual, callback);
	}
}

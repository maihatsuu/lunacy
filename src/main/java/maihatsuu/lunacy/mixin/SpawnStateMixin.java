package maihatsuu.lunacy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import maihatsuu.lunacy.spawn.FullMoonSurge;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Scales the global monster cap while {@link FullMoonSurge#capMultiplier} is set. */
@Mixin(NaturalSpawner.SpawnState.class)
public abstract class SpawnStateMixin {
	@ModifyExpressionValue(
			method = "canSpawnForCategory",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/MobCategory;getMaxInstancesPerChunk()I")
	)
	private int lunacy$scaleGlobalCap(int cap, @Local(argsOnly = true) MobCategory category) {
		return FullMoonSurge.scaleCap(cap, category);
	}
}

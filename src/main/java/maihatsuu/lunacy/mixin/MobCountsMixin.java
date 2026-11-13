package maihatsuu.lunacy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import maihatsuu.lunacy.spawn.FullMoonSurge;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Scales the per-player monster cap while {@link FullMoonSurge#capMultiplier} is set. */
@Mixin(targets = "net.minecraft.world.level.LocalMobCapCalculator$MobCounts")
public abstract class MobCountsMixin {
	@ModifyExpressionValue(
			method = "canSpawn",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/MobCategory;getMaxInstancesPerChunk()I")
	)
	private int lunacy$scaleLocalCap(int cap, @Local(argsOnly = true) MobCategory category) {
		return FullMoonSurge.scaleCap(cap, category);
	}
}

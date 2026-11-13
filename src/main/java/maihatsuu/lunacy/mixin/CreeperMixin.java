package maihatsuu.lunacy.mixin;

import maihatsuu.lunacy.config.LunacyConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Creepers never target other monsters. Without this, a creeper hit by a stray skeleton arrow
 * retaliates (HurtByTargetGoal) and blows up the crowd, which gets out of hand during the hunt.
 */
@Mixin(Creeper.class)
public abstract class CreeperMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void lunacy$ignoreMonsters(LivingEntity target, CallbackInfo ci) {
		if (target instanceof Enemy && LunacyConfig.get().creepersIgnoreMonsters) {
			ci.cancel();
		}
	}
}

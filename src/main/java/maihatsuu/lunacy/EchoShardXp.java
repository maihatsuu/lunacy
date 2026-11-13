package maihatsuu.lunacy;

import maihatsuu.lunacy.config.LunacyConfig;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Right-clicking an echo shard uses it up for experience. */
public final class EchoShardXp {
	private EchoShardXp() {
	}

	public static void register() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			int xp = LunacyConfig.get().echoShardXp;
			if (!stack.is(Items.ECHO_SHARD) || xp <= 0 || player.isSpectator()) {
				return InteractionResultHolder.pass(stack);
			}
			if (level instanceof ServerLevel serverLevel) {
				player.giveExperiencePoints(xp);
				stack.consume(1, player);
				serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
						SoundSource.PLAYERS, 0.4F, 0.9F + serverLevel.random.nextFloat() * 0.3F);
				serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
						6, 0.3, 0.4, 0.3, 0.02);
			}
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		});
	}
}

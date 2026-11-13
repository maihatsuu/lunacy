package maihatsuu.lunacy;

import maihatsuu.lunacy.config.LunacyConfig;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

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
				ItemParticleOption shards = new ItemParticleOption(ParticleTypes.ITEM, stack.copyWithCount(1));
				player.giveExperiencePoints(xp);
				stack.consume(1, player);
				serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK,
						SoundSource.PLAYERS, 1.0F, 0.8F + serverLevel.random.nextFloat() * 0.2F);
				serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
						SoundSource.PLAYERS, 0.4F, 0.9F + serverLevel.random.nextFloat() * 0.3F);
				Vec3 burst = player.getEyePosition().add(player.getLookAngle().scale(0.5)).subtract(0.0, 0.3, 0.0);
				serverLevel.sendParticles(shards, burst.x, burst.y, burst.z, 10, 0.1, 0.1, 0.1, 0.08);
			}
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		});
	}
}

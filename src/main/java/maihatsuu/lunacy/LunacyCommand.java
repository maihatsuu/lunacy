package maihatsuu.lunacy;

import maihatsuu.lunacy.config.LunacyConfig;
import maihatsuu.lunacy.star.ShootingStars;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /lunacy star}: drops a shooting star near you right now (ops only, for testing). */
public final class LunacyCommand {
	private LunacyCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("lunacy")
						.requires(source -> source.hasPermission(2))
						.then(Commands.literal("star").executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							if (!ShootingStars.launch(player.serverLevel(), player, LunacyConfig.get())) {
								context.getSource().sendFailure(Component.literal("No landing spot found nearby."));
								return 0;
							}
							context.getSource().sendSuccess(() -> Component.literal("A star falls."), false);
							return 1;
						}))));
	}
}

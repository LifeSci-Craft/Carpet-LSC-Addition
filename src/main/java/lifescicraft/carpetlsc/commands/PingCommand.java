package lifescicraft.carpetlsc.commands;

import com.mojang.brigadier.CommandDispatcher;
import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /ping} - reports the caller's current latency to the server.
 *
 * <p>The command is registered for every command source so that it stays discoverable in
 * {@code /help}, and the "must be a player" requirement is enforced when it runs rather than
 * while it is parsed.
 */
public final class PingCommand
{
    private PingCommand()
    {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("ping")
                .requires(source -> CarpetLscSettings.commandPing)
                .executes(context ->
                {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();

                    if (player == null)
                    {
                        source.sendFailure(Component.translatable("carpet-lsc-addition.ping.notPlayer"));
                        return 0;
                    }

                    int latency = player.connection.latency();

                    source.sendSuccess(
                            () -> Component.translatable("carpet-lsc-addition.ping.result", latency), false);

                    return latency;
                }));
    }
}

package lifescicraft.carpetlsc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import lifescicraft.carpetlsc.CarpetLscSettings;
import lifescicraft.carpetlsc.helpers.McdrBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /!! <command>} - runs an MCDR command without typing it into chat.
 *
 * <p>Typing {@code !!ig check} as a chat message makes the vanilla client show the text locally and
 * sends it as ordinary chat; {@code /!!} is a real command instead, so nothing is echoed into the
 * chat box. {@link McdrBridge} then hands the text to MCDR.
 *
 * <p>Like {@code /ping}, the node is registered for every source so it stays discoverable, and the
 * "must be a player" rule is enforced at execution time rather than while parsing.
 */
public final class McdrBridgeCommand
{
    private McdrBridgeCommand()
    {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal(McdrBridge.COMMAND_PREFIX)
                .requires(source -> CarpetLscSettings.mcdrCommandBridge)
                .then(Commands.argument("command", StringArgumentType.greedyString())
                        .executes(context ->
                        {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayer();

                            if (player == null)
                            {
                                source.sendFailure(Component.translatable("carpet-lsc-addition.mcdr.notPlayer"));
                                return 0;
                            }

                            McdrBridge.forward(player, StringArgumentType.getString(context, "command"));
                            return 1;
                        })));
    }
}

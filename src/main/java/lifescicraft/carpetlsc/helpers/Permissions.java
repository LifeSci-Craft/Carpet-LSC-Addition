package lifescicraft.carpetlsc.helpers;

import carpet.CarpetServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Who may use the commands this mod adds.
 *
 * <p>Everything is operator only on a dedicated server; a singleplayer world has no operators to
 * speak of, so the player who owns the world is allowed.
 */
public final class Permissions
{
    private Permissions()
    {
    }

    public static boolean isOp(CommandSourceStack source)
    {
        MinecraftServer server = CarpetServer.minecraft_server;

        if (server == null)
        {
            return false;
        }

        if (server.isSingleplayer())
        {
            return true;
        }

        return source.getEntity() instanceof ServerPlayer player
                && server.getPlayerList().isOp(player.nameAndId());
    }
}

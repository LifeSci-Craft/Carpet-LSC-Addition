package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Announces where a player died.
 *
 * <p>{@code deathCoordinates} is a three-state rule rather than a switch: {@code off},
 * {@code self} or {@code broadcast}. {@code self} is the privacy-friendly default for a public
 * server, because it only tells the player who already knows they died.
 */
public final class DeathCoordinates
{
    public static final String OFF = "off";
    public static final String SELF = "self";
    public static final String BROADCAST = "broadcast";

    private DeathCoordinates()
    {
    }

    public static void announce(ServerPlayer player)
    {
        String mode = CarpetLscSettings.deathCoordinates;

        if (OFF.equals(mode))
        {
            return;
        }

        BlockPos pos = player.blockPosition();
        Component message = Component.translatable(
                "carpet-lsc-addition.death.coordinates",
                player.getDisplayName(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                player.level().dimension().identifier().toString());

        if (SELF.equals(mode))
        {
            player.sendSystemMessage(message);
        }
        else
        {
            player.level().getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }
}

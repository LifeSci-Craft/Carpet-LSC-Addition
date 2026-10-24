package lifescicraft.carpetlsc.commands;

import com.mojang.brigadier.CommandDispatcher;
import lifescicraft.carpetlsc.helpers.DeathCoordinates;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** {@code /lscwaypoint}: re-send the waypoint of this player's last death. */
public final class WaypointCommand
{
    private WaypointCommand()
    {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("lscwaypoint")
                .requires(lifescicraft.carpetlsc.helpers.Permissions::isOp)
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(context -> {
                    DeathCoordinates.resend((ServerPlayer) context.getSource().getEntity());
                    return 1;
                }));
    }
}

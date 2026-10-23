package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Announces where a player died.
 *
 * <p>{@code deathCoordinates} is a three-state rule rather than a switch: {@code off},
 * {@code self} or {@code broadcast}. {@code self} is the privacy-friendly default for a public
 * server, because it only tells the player who already knows they died.
 *
 * <p>The message carries a clickable "add waypoint" button. It runs Xaero's client-side
 * {@code /xaero_waypoint_add} command, which is the same command Xaero's own waypoint sharing uses;
 * a client without Xaero simply has no such command and the click does nothing.
 */
public final class DeathCoordinates
{
    public static final String OFF = "off";
    public static final String SELF = "self";
    public static final String BROADCAST = "broadcast";

    /** Xaero colour id 12, red. */
    private static final int WAYPOINT_COLOR = 12;

    private static final String WAYPOINT_SYMBOL = "D";

    private DeathCoordinates()
    {
    }

    public static void announce(ServerPlayer player)
    {
        String mode = CarpetLscSettings.DeathCoordinates;

        if (OFF.equals(mode))
        {
            return;
        }

        BlockPos pos = player.blockPosition();
        Level level = player.level();

        Component message = Component.translatable(
                "carpet-lsc-addition.death.coordinates",
                player.getDisplayName(),
                coordinates(pos),
                dimension(level),
                waypointButton(pos, level));

        if (SELF.equals(mode))
        {
            player.sendSystemMessage(message);
        }
        else
        {
            level.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private static Component coordinates(BlockPos pos)
    {
        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                .withStyle(ChatFormatting.YELLOW);
    }

    private static Component dimension(Level level)
    {
        Identifier id = level.dimension().identifier();
        String path = id.getPath();

        if (!isVanillaDimension(path))
        {
            // A datapack dimension has no translation of its own, so show the raw id.
            return Component.literal(id.toString()).withStyle(dimensionColor(path));
        }

        return Component.translatable("carpet-lsc-addition.dimension." + path)
                .withStyle(dimensionColor(path));
    }

    private static boolean isVanillaDimension(String path)
    {
        return "overworld".equals(path) || "the_nether".equals(path) || "the_end".equals(path);
    }

    private static ChatFormatting dimensionColor(String path)
    {
        return switch (path)
        {
            case "the_nether" -> ChatFormatting.RED;
            case "the_end" -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.GREEN;
        };
    }

    private static Component waypointButton(BlockPos pos, Level level)
    {
        String dimension = level.dimension().identifier().getPath();

        // The waypoint name is server-built, so it has to stay language-neutral and colon-free:
        // Xaero splits the command on ':'.
        String name = "Death " + pos.getX() + " " + pos.getY() + " " + pos.getZ();

        String command = "/xaero_waypoint_add:"
                + name + ":" + WAYPOINT_SYMBOL
                + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ()
                + ":" + WAYPOINT_COLOR + ":false:0"
                + ":Internal_" + dimension + "_waypoints";

        Style style = Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(
                        Component.translatable("carpet-lsc-addition.death.addWaypoint.hover")));

        return Component.translatable("carpet-lsc-addition.death.addWaypoint").withStyle(style);
    }
}

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

    /** Where each player died last, so the button can resend the waypoint later. */
    private static final java.util.Map<java.util.UUID, BlockPos> LAST_DEATH =
            new java.util.concurrent.ConcurrentHashMap<>();

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

        LAST_DEATH.put(player.getUUID(), pos.immutable());

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

    /**
     * The waypoint in Xero's own chat-share format.
     *
     * <p>This is the string Xaero's client intercepts when a player shares a waypoint; it turns it
     * into a clickable "add" prompt using the world the client is currently in. The click-command
     * route does not work: a {@code run_command} click is sent to the server, which has no such
     * command, so Xaero never sees it.
     */
    /**
     * Sends the share line for the player's last death.
     *
     * <p>Runs from the {@code [add waypoint]} button. The client cannot trigger Xaero's own add
     * prompt directly, so the click asks the server for the share message and Xaero turns that into
     * its clickable prompt.
     */
    public static void resend(ServerPlayer player)
    {
        BlockPos pos = LAST_DEATH.get(player.getUUID());

        if (pos != null)
        {
            player.sendSystemMessage(shareLine(pos, player.level()));
        }
    }

    private static Component shareLine(BlockPos pos, Level level)
    {
        String name = "Death(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";

        return Component.literal("xaero-waypoint:" + name + ":" + WAYPOINT_SYMBOL
                + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ()
                + ":" + WAYPOINT_COLOR + ":false:0:Internal");
    }

    private static Component waypointButton(BlockPos pos, Level level)
    {
        // The name is server-built, so it stays language-neutral and free of ':' (Xaero splits on it)
        // and of spaces, which the command parser would treat as separate arguments.
        String name = "Death(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";

        // Xaero splits the waypoint string on ':' and wants at least nine fields. The last field is
        // the container marker: the bare word "Internal" means "whatever world the client is in",
        // which is the only marker that is correct on a multiplayer server (the per-world set id is
        // the client's own business).
        String command = "/lscwaypoint";

        Style style = Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(
                        Component.translatable("carpet-lsc-addition.death.addWaypoint.hover")));

        return Component.translatable("carpet-lsc-addition.death.addWaypoint").withStyle(style);
    }
}

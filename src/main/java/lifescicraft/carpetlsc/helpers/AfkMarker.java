package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Prefixes idle players with {@code [AFK]}.
 *
 * <p>Idle means "has not moved or turned for five minutes". Position and rotation are tracked
 * instead of any vanilla counter because {@code ServerPlayer#getLastActionTime} is only bumped by
 * interactions, so a player who walks around but never clicks would look idle.
 *
 * <p>The marker is a custom name: {@code Player#getDisplayName} prefers it, so vanilla chat, the
 * tab list and death messages all pick up the prefix without touching the player's real name. Any
 * name that was already set is remembered and restored when the player comes back.
 */
public final class AfkMarker
{
    /** Five minutes at 20 ticks per second. */
    private static final int AFK_TICKS = 6000;

    private static final Map<UUID, Track> TRACKS = new HashMap<>();

    private AfkMarker()
    {
    }

    public static void tick(ServerPlayer player)
    {
        if (!CarpetLscSettings.afkMarker)
        {
            clear(player);
            return;
        }

        Track track = TRACKS.computeIfAbsent(player.getUUID(), key -> new Track());

        // A reconnecting player gets a fresh entity and a tickCount that starts over, so the
        // previous session's numbers must not be carried across.
        if (track.entityId != player.getId())
        {
            track.entityId = player.getId();
            track.pos = null;
            track.afk = false;
            track.savedName = null;
        }

        BlockPos pos = player.blockPosition();
        float yRot = player.getYRot();
        float xRot = player.getXRot();

        if (track.pos == null)
        {
            track.pos = pos;
            track.yRot = yRot;
            track.xRot = xRot;
            track.lastActive = player.tickCount;
        }

        boolean moved = !pos.equals(track.pos)
                || Math.abs(yRot - track.yRot) > 0.01F
                || Math.abs(xRot - track.xRot) > 0.01F;

        track.pos = pos;
        track.yRot = yRot;
        track.xRot = xRot;

        if (moved)
        {
            track.lastActive = player.tickCount;
        }

        boolean shouldBeAfk = player.tickCount - track.lastActive > AFK_TICKS;

        if (shouldBeAfk && !track.afk)
        {
            track.afk = true;
            track.savedName = player.getCustomName();
            player.setCustomName(Component.literal("[AFK] ").append(player.getName()));
        }
        else if (!shouldBeAfk && track.afk)
        {
            track.afk = false;
            player.setCustomName(track.savedName);
            track.savedName = null;
        }
    }

    /** Restores the name when the rule is switched off or the player leaves. */
    public static void clear(ServerPlayer player)
    {
        Track track = TRACKS.remove(player.getUUID());

        if (track != null && track.afk)
        {
            player.setCustomName(track.savedName);
        }
    }

    private static final class Track
    {
        int entityId;
        BlockPos pos;
        float yRot;
        float xRot;
        int lastActive;
        boolean afk;
        Component savedName;
    }
}

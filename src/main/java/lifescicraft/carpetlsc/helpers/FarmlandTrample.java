package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Decides whether a landing may still turn farmland into dirt.
 *
 * <p>{@code noFarmlandTrample} is {@code false} (vanilla), {@code true} (the block never breaks) or
 * a number: that many tramples of the same farmland block are tolerated, and the next one breaks it.
 *
 * <p>The streak is counted per block and expires after {@link #STREAK_TICKS}, so landings spread
 * across a session are never added up the way repeated hops are.
 */
public final class FarmlandTrample
{
    /** 60 seconds: long enough for repeated jumping, short enough not to remember yesterday. */
    private static final int STREAK_TICKS = 1200;

    private static final String SCOPE_ALL = "all";

    private static final Map<Level, Map<BlockPos, Streak>> STREAKS = new WeakHashMap<>();

    private FarmlandTrample()
    {
    }

    /**
     * @return true when vanilla should break the farmland, false when the landing is absorbed.
     */
    public static boolean shouldTrample(Level level, BlockPos pos, Entity entity)
    {
        // The scope only narrows down who is protected; on its own it decides nothing.
        if (!SCOPE_ALL.equals(CarpetLscSettings.NoFarmlandTrampleScope) && !(entity instanceof Player))
        {
            return true;
        }

        String mode = CarpetLscSettings.NoFarmlandTrample;

        if ("true".equals(mode))
        {
            return false;
        }

        int allowed = allowedTramples(mode);

        if (allowed <= 0)
        {
            return true;
        }

        long now = level.getGameTime();
        Map<BlockPos, Streak> blockStreaks = STREAKS.computeIfAbsent(level, key -> new HashMap<>());
        Streak streak = blockStreaks.get(pos);

        if (streak == null || now - streak.lastTick > STREAK_TICKS)
        {
            streak = new Streak();
        }

        streak.count++;
        streak.lastTick = now;

        if (streak.count > allowed)
        {
            blockStreaks.remove(pos);
            return true;
        }

        blockStreaks.put(pos, streak);
        return false;
    }

    private static int allowedTramples(String mode)
    {
        try
        {
            return Integer.parseInt(mode);
        }
        catch (NumberFormatException ignored)
        {
            // "false" and anything unexpected mean "no protection".
            return 0;
        }
    }

    private static final class Streak
    {
        private int count;
        private long lastTick;
    }
}

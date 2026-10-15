package lifescicraft.carpetlsc.helpers;

import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.Level;

/** Shared logic for the daytime sleeping rule. */
public final class SleepRules
{
    /** {@code data/minecraft/timeline/day.json} -> {@code minecraft:night.ticks}. */
    private static final long NIGHTFALL_TICKS = 13000L;

    private static final long DAY_LENGTH = 24000L;

    private SleepRules()
    {
    }

    /**
     * Whether the "you may only sleep when it is dark" restriction should be lifted for this bed
     * in this dimension.
     *
     * <p>Only that one restriction is relaxed. A bed explicitly configured as never sleepable
     * (for example the beds of dimensions with a fixed time) stays refused, and a bed that
     * already allows sleeping is left alone.
     */
    public static boolean isRelaxableDayRule(BedRule bedRule, Level level)
    {
        return bedRule.canSleep() == BedRule.Rule.WHEN_DARK && !level.dimensionType().hasFixedTime();
    }

    /**
     * Whether the dimension's clock currently reads as daytime.
     *
     * <p>The comparison uses the raw clock value rather than {@code isBrightOutside()} on
     * purpose: sky brightness already reads as "dark" a few hundred ticks before nightfall, and
     * a player who lies down in that dusk window should still skip forward to night, not all the
     * way to the next morning.
     */
    public static boolean isDaytime(Level level)
    {
        return Math.floorMod(level.getDefaultClockTime(), DAY_LENGTH) < NIGHTFALL_TICKS;
    }
}

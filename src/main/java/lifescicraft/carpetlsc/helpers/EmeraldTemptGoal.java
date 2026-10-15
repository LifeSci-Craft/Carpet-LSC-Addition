package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Items;

/**
 * Makes a mob follow players who hold an emerald, the same way cows and sheep follow wheat.
 *
 * <p>{@link TemptGoal} is registered permanently on the mob's goal selector, so the rule is
 * checked here on every evaluation instead. That way {@code /carpet emeraldTemptsVillagers}
 * takes effect immediately, without needing a respawn or a reload.
 */
public class EmeraldTemptGoal extends TemptGoal
{
    private static final double SPEED_MODIFIER = 1.0D;

    public EmeraldTemptGoal(PathfinderMob mob)
    {
        super(mob, SPEED_MODIFIER, stack -> stack.is(Items.EMERALD), false);
    }

    @Override
    public boolean canUse()
    {
        return CarpetLscSettings.emeraldTemptsVillagers && super.canUse();
    }

    @Override
    public boolean canContinueToUse()
    {
        return CarpetLscSettings.emeraldTemptsVillagers && super.canContinueToUse();
    }
}

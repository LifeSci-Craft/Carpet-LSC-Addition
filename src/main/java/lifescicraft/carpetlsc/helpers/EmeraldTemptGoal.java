package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;

/**
 * Makes a mob follow players who hold an emerald, the same way cows and sheep follow wheat.
 *
 * <p>{@link TemptGoal} is registered permanently on the mob's goal selector, so the rule is
 * checked here on every evaluation instead. That way {@code /carpet emeraldTemptsVillagers}
 * takes effect immediately, without needing a respawn or a reload.
 *
 * <p>Running away always wins over being tempted. Villagers flee through the {@code Brain}'s
 * {@code PANIC} activity rather than a goal, so while panicking the brain drives the navigator and
 * a goal-selector entry that keeps calling {@code moveTo(player)} would fight it and leave the
 * villager walking towards whoever just hit it. Both signals are checked here: the brain activity,
 * and a cooldown on "recently hurt" for the moment before the activity has started.
 */
public class EmeraldTemptGoal extends TemptGoal
{
    /**
     * Villagers move at attribute speed 0.5, twice a cow's 0.25, so vanilla's 1.0 for animals made
     * a tempted villager sprint at roughly three times a tempted cow's pace. Measured on a live
     * server: 0.7 gave 5.3 blocks/s against a cow's 2.6, and 0.45 overshot the other way at
     * 2.2 blocks/s. 0.55 lands around 3.4 blocks/s, a little brisker than a cow but no longer a
     * sprint.
     */
    private static final double SPEED_MODIFIER = 0.55D;

    /** How long after being hurt a villager ignores emeralds, in ticks. */
    private static final int HURT_COOLDOWN_TICKS = 100;

    public EmeraldTemptGoal(PathfinderMob mob)
    {
        super(mob, SPEED_MODIFIER, stack -> stack.is(Items.EMERALD), false);
    }

    @Override
    public boolean canUse()
    {
        return CarpetLscSettings.emeraldTemptsVillagers && !carpetLsc$isFleeing() && super.canUse();
    }

    @Override
    public boolean canContinueToUse()
    {
        return CarpetLscSettings.emeraldTemptsVillagers && !carpetLsc$isFleeing() && super.canContinueToUse();
    }

    private boolean carpetLsc$isFleeing()
    {
        LivingEntity mob = this.mob;

        if (mob.getLastHurtByMob() != null
                && mob.tickCount - mob.getLastHurtByMobTimestamp() < HURT_COOLDOWN_TICKS)
        {
            return true;
        }

        return mob instanceof Villager villager && villager.getBrain().isActive(Activity.PANIC);
    }
}

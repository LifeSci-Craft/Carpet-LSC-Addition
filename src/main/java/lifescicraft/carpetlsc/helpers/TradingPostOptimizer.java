package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/**
 * Stops a boxed-in trader from running its movement code every tick (rule
 * {@code VillagerTradeOptimization}).
 *
 * <p>A trading hall is usually a grid of one block cells, and every villager in it keeps looking for
 * a path it can never take. Once a villager has a profession and is past novice it will not change
 * trades any more, so freezing its navigation costs nothing and takes the pathfinding off the tick.
 */
public final class TradingPostOptimizer
{
    private TradingPostOptimizer()
    {
    }

    public static void tick(Villager villager)
    {
        if (!CarpetLscSettings.VillagerTradeOptimization || !isWorker(villager))
        {
            return;
        }

        if (!VillagerBackpack.isConfined(villager))
        {
            return;
        }

        villager.getNavigation().stop();
        villager.setDeltaMovement(0.0, Math.min(villager.getDeltaMovement().y, 0.0), 0.0);
    }

    private static boolean isWorker(Villager villager)
    {
        VillagerProfession profession = villager.getVillagerData().profession().value();

        return !"none".equals(profession.name().getString().toLowerCase(java.util.Locale.ROOT))
                && villager.getVillagerData().level() > 1;
    }
}

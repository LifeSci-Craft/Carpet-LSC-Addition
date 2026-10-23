package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

import java.util.ArrayList;
import java.util.List;

/**
 * Silently eats from a bundle of food when that is enough to top the hunger bar off
 * (rule {@code bundleAutoEat}).
 *
 * <p>A bundle only counts when <em>everything</em> in it is food, so a mixed bundle is left alone
 * rather than guessed at.
 *
 * <p>Two conditions must hold before anything is eaten:
 *
 * <ul>
 *     <li>hunger is not full yet, and</li>
 *     <li>one single item would fill it completely ({@code hunger + nutrition >= 20}).</li>
 * </ul>
 *
 * <p>The second condition is what keeps the rule from burning a stack of bread when the player is
 * nearly starving: it only fires when one item finishes the job.
 *
 * <p>The item is consumed through {@code ItemStack.finishUsingItem}, so nutrition, saturation and
 * any food effects behave exactly as if the player had eaten it, minus the sound and animation.
 */
public final class BundleAutoEat
{
    /** Run at most once per second; hunger does not change faster than that. */
    private static final int CHECK_INTERVAL = 20;

    private static final int FULL_FOOD = 20;

    /** Eat the item with the lowest saturation value first. */
    public static final String ORDER_SATURATION = "saturation";

    private BundleAutoEat()
    {
    }

    public static void tick(ServerPlayer player)
    {
        if (!CarpetLscSettings.bundleAutoEat || player.tickCount % CHECK_INTERVAL != 0)
        {
            return;
        }

        FoodData food = player.getFoodData();
        int hunger = food.getFoodLevel();

        if (hunger >= FULL_FOOD)
        {
            return;
        }

        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            ItemStack bundle = inventory.getItem(slot);
            BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);

            if (contents != null && !contents.isEmpty() && tryEat(player, bundle, contents, hunger))
            {
                return;
            }
        }
    }

    private static boolean tryEat(ServerPlayer player, ItemStack bundle, BundleContents contents, int hunger)
    {
        List<ItemStack> items = contents.itemCopies().toList();

        for (ItemStack item : items)
        {
            if (item.get(DataComponents.FOOD) == null)
            {
                // Mixed bundle: not treated as a lunch box.
                return false;
            }
        }

        int chosen = -1;
        FoodProperties chosenFood = null;

        for (int i = 0; i < items.size(); i++)
        {
            FoodProperties candidate = items.get(i).get(DataComponents.FOOD);

            if (hunger + candidate.nutrition() < FULL_FOOD)
            {
                // Would not fill the bar on its own.
                continue;
            }

            if (chosen < 0 || prefers(candidate, chosenFood))
            {
                chosen = i;
                chosenFood = candidate;
            }
        }

        if (chosen < 0)
        {
            return false;
        }

        ItemStack eaten = items.get(chosen).copyWithCount(1);
        removeOne(bundle, contents, items, chosen);
        eaten.finishUsingItem(player.level(), player);
        return true;
    }

    private static boolean prefers(FoodProperties candidate, FoodProperties current)
    {
        if (!ORDER_SATURATION.equals(CarpetLscSettings.bundleAutoEatOrder))
        {
            // Bundle order: keep whichever came first.
            return false;
        }

        return saturationValue(candidate) < saturationValue(current);
    }

    /** The saturation a single item actually grants. */
    private static float saturationValue(FoodProperties food)
    {
        return food.nutrition() * food.saturation();
    }

    private static void removeOne(ItemStack bundle, BundleContents contents, List<ItemStack> items, int index)
    {
        List<ItemStack> remaining = new ArrayList<>(items);
        ItemStack stack = remaining.get(index);

        if (stack.getCount() > 1)
        {
            remaining.set(index, stack.copyWithCount(stack.getCount() - 1));
        }
        else
        {
            remaining.remove(index);
        }

        bundle.set(DataComponents.BUNDLE_CONTENTS, contents.copyWithContents(remaining.stream()));
    }
}

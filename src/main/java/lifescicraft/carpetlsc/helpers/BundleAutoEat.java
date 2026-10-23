package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.level.GameType;

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

    private static final String OFF = "off";

    private BundleAutoEat()
    {
    }

    public static void tick(ServerPlayer player)
    {
        if (OFF.equals(CarpetLscSettings.BundleAutoEat) || player.tickCount % CHECK_INTERVAL != 0)
        {
            return;
        }

        if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL)
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
        boolean chosenSpecial = false;

        for (int i = 0; i < items.size(); i++)
        {
            ItemStack item = items.get(i);
            FoodProperties candidate = item.get(DataComponents.FOOD);

            if (hunger + candidate.nutrition() < FULL_FOOD)
            {
                // Would not fill the bar on its own.
                continue;
            }

            boolean special = hasExtraEffects(item);

            if (chosen < 0 || prefers(candidate, special, chosenFood, chosenSpecial))
            {
                chosen = i;
                chosenFood = candidate;
                chosenSpecial = special;
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

    private static boolean prefers(FoodProperties candidate, boolean candidateSpecial,
                                   FoodProperties current, boolean currentSpecial)
    {
        if (candidateSpecial != currentSpecial)
        {
            // Food that carries its own effects (golden apple, rotten flesh, chorus fruit...) is
            // kept for last, so a plain meal is never traded for one with side effects.
            return !candidateSpecial;
        }

        if (!ORDER_SATURATION.equals(CarpetLscSettings.BundleAutoEat))
        {
            // Bundle order: keep whichever came first.
            return false;
        }

        return saturationValue(candidate) < saturationValue(current);
    }

    /**
     * Whether eating this item also applies something beyond nutrition.
     *
     * <p>Every consumable carries a {@code PlaySoundConsumeEffect}; anything else in that list
     * (status effects, effect removal, teleport) counts as an extra effect, positive or negative.
     */
    private static boolean hasExtraEffects(ItemStack stack)
    {
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);

        if (consumable == null)
        {
            return false;
        }

        for (ConsumeEffect effect : consumable.onConsumeEffects())
        {
            if (!(effect instanceof PlaySoundConsumeEffect))
            {
                return true;
            }
        }

        return false;
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

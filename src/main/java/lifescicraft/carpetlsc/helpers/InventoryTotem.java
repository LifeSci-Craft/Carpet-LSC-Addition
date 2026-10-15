package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Totem of undying used from anywhere in the player's inventory (rule {@code inventoryTotem}).
 *
 * <p>Vanilla only looks at the two held items when it checks for death protection. With this
 * rule enabled, a totem carried anywhere in the inventory is used instead, so a player who keeps
 * a totem in a backpack slot still survives a lethal hit.
 *
 * <p>Only the <em>source</em> of the stack is widened. Everything else - the damage type filter,
 * the shrinking of the stack, the death-protection component's effects, statistics and
 * advancements - stays with vanilla, because the redirected value is handed straight back to the
 * vanilla logic.
 */
public final class InventoryTotem
{
    private InventoryTotem()
    {
    }

    /**
     * Returns the stack that should be treated as held in {@code hand} for death protection.
     *
     * <p>When the rule is off, the held stack is returned unchanged. When it is on, a held totem
     * still wins (so the normal behaviour is preserved exactly), otherwise the first totem found
     * in the inventory is returned.
     */
    public static ItemStack findTotem(LivingEntity entity, InteractionHand hand, ItemStack held)
    {
        if (!CarpetLscSettings.inventoryTotem || !(entity instanceof Player player))
        {
            return held;
        }

        // A totem in either hand behaves exactly like vanilla.
        if (held.get(DataComponents.DEATH_PROTECTION) != null)
        {
            return held;
        }

        return findInInventory(player);
    }

    private static ItemStack findInInventory(Player player)
    {
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            ItemStack stack = inventory.getItem(slot);

            if (!stack.isEmpty() && stack.get(DataComponents.DEATH_PROTECTION) != null)
            {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}

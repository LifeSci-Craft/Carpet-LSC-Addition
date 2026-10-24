package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.FletchingTable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes our re-use of the anvil menu behave like a fletching bench.
 *
 * <p>For menus we opened ourselves, the two input slots only take arrow materials and the result
 * comes from the vanilla crafting recipe lookup instead of the anvil's repair logic. Real anvils are
 * untouched because the flag is only set while our own menu is constructed.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuFletchingMixin
{
    @Unique
    private boolean carpetLsc$fletching;

    @Unique
    private net.minecraft.world.level.Level carpetLsc$level;

    @Unique
    private boolean carpetLsc$computing;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void carpetLsc$init(int id, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci)
    {
        if (!FletchingTable.isOurs())
        {
            return;
        }

        carpetLsc$fletching = true;
        carpetLsc$level = FletchingTable.openingLevel();

        AnvilMenu menu = (AnvilMenu) (Object) this;

        for (int i = 0; i < 2; i++)
        {
            Slot slot = menu.slots.get(i);
            menu.slots.set(i, new MaterialSlot(slot.container, slot.getContainerSlot(), slot.x, slot.y));
        }

        // The anvil only lets a result be taken while its repair cost is above zero, and it empties
        // whole input stacks on take. Neither fits a crafting screen, so the result slot is ours.
        Slot result = menu.slots.get(2);
        menu.slots.set(2, new FletchingResultSlot(menu, result.container, result.getContainerSlot(), result.x, result.y));
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void carpetLsc$createResult(CallbackInfo ci)
    {
        if (!carpetLsc$fletching || carpetLsc$computing)
        {
            return;
        }

        carpetLsc$computing = true;

        try
        {
            AnvilMenu menu = (AnvilMenu) (Object) this;
            ItemStack first = menu.slots.get(0).getItem();
            ItemStack second = menu.slots.get(1).getItem();

            ItemStack result = ItemStack.EMPTY;

            if (!first.isEmpty() && !second.isEmpty())
            {
                result = FletchingTable.craft(carpetLsc$level, first, second);
            }

            menu.slots.get(2).set(result);
        }
        finally
        {
            carpetLsc$computing = false;
        }
    }

    /**
     * Result slot: always takeable, and it consumes exactly what the craft used.
     *
     * <p>Eight arrows with one lingering potion make eight tipped arrows, and one arrow with four
     * glowstone dust makes one spectral arrow, so taking a stack has to scale the inputs by the same
     * ratio instead of emptying them.
     */
    private static final class FletchingResultSlot extends Slot
    {
        private final AnvilMenu menu;

        private FletchingResultSlot(AnvilMenu menu, Container container, int index, int x, int y)
        {
            super(container, index, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPickup(Player player)
        {
            return !getItem().isEmpty();
        }

        @Override
        public void onTake(Player player, ItemStack taken)
        {
            ItemStack first = menu.slots.get(0).getItem();
            ItemStack second = menu.slots.get(1).getItem();
            int crafted = taken.getCount();

            if (taken.is(net.minecraft.world.item.Items.SPECTRAL_ARROW))
            {
                consume(first, crafted);
                consume(second, crafted * 4);
            }
            else if (taken.is(net.minecraft.world.item.Items.TIPPED_ARROW))
            {
                // One lingering potion covers a whole batch of eight arrows.
                consume(first, crafted);
                consume(second, 1);
            }
            else
            {
                consume(first, 1);
                consume(second, 1);
            }

            set(ItemStack.EMPTY);
        }

        private static void consume(ItemStack stack, int amount)
        {
            if (!stack.isEmpty())
            {
                stack.shrink(amount);
            }
        }
    }

    /** Input slot: only arrows and what a vanilla arrow recipe consumes. */
    private static final class MaterialSlot extends Slot
    {
        private MaterialSlot(Container container, int index, int x, int y)
        {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return FletchingTable.isArrowMaterial(stack);
        }
    }
}

package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.FletchingTable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
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
    private ContainerLevelAccess carpetLsc$access;

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
        carpetLsc$access = access;

        AnvilMenu menu = (AnvilMenu) (Object) this;

        for (int i = 0; i < 2; i++)
        {
            Slot slot = menu.slots.get(i);
            menu.slots.set(i, new MaterialSlot(slot.container, slot.index, slot.x, slot.y));
        }
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
                result = carpetLsc$access
                        .evaluate((level, pos) -> FletchingTable.craft(level, FletchingTable.grid(first, second)))
                        .orElse(ItemStack.EMPTY);
            }

            menu.slots.get(2).set(result);
        }
        finally
        {
            carpetLsc$computing = false;
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

package lifescicraft.carpetlsc.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Caps the time needed to eat or drink anything at {@code fastEatingTicks} (rule {@code fastEating}).
 *
 * <p>{@code ItemStack#getUseDuration} is what both sides use to decide how long a use lasts, so the
 * cap only touches items that carry a {@code CONSUMABLE} component: bows, shields, spyglasses and
 * every other usable item skip the check untouched.
 *
 * <p>Items that are already quicker than the cap keep their own duration, and the rest of the
 * consume path is left alone — the hunger gate ({@code FoodData#needsFood}) and the item's own
 * effects still run as usual.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackFastEatingMixin
{
    private static final int DEFAULT_TICKS = 8;
    @ModifyReturnValue(method = "getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I", at = @At("RETURN"))
    private int carpetLsc$fastEating(int original, LivingEntity user)
    {
        int cap = capTicks();

        if (cap <= 0)
        {
            return original;
        }

        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.has(DataComponents.CONSUMABLE))
        {
            return original;
        }

        return Math.min(original, cap);
    }

    /** {@code true} selects the default cap; a bare number is used as-is. */
    private static int capTicks()
    {
        return switch (CarpetLscSettings.FastEating)
        {
            case "true" -> DEFAULT_TICKS;
            case "4" -> 4;
            case "8" -> 8;
            case "16" -> 16;
            default -> 0;
        };
    }
}

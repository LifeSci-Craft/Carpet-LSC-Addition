package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.InventoryTotem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Inventory totem (rule {@code inventoryTotem}).
 *
 * <p>{@code LivingEntity.checkTotemDeathProtection} walks the two hands and asks for the stack in
 * each. Redirecting that single lookup lets a totem be taken from the player's inventory instead,
 * while the rest of the method - the damage-type filter, shrinking the stack, applying the
 * death-protection effects, the statistic and the advancement - keeps running exactly as vanilla.
 *
 * <p>The method contains exactly one {@code getItemInHand} call and it is identical in 26.1, 26.2
 * and 26.3, so this is a version-stable injection point.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityInventoryTotemMixin
{
    @Redirect(
            method = "checkTotemDeathProtection",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack carpetLsc$useInventoryTotem(LivingEntity entity, InteractionHand hand)
    {
        return InventoryTotem.findTotem(entity, hand, entity.getItemInHand(hand));
    }
}

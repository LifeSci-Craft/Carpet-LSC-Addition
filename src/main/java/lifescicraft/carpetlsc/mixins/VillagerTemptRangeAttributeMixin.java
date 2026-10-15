package lifescicraft.carpetlsc.mixins;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives villagers the {@code TEMPT_RANGE} attribute.
 *
 * <p>This is required by {@code emeraldTemptsVillagers}: {@code TemptGoal.canUse()} reads
 * {@code mob.getAttributeValue(Attributes.TEMPT_RANGE)}, and {@code AttributeSupplier} throws
 * an {@code IllegalArgumentException} for attributes the entity type never registered.
 * Vanilla only adds {@code TEMPT_RANGE} on {@code Animal}, while {@code Villager} registers
 * just {@code MOVEMENT_SPEED} (plus {@code FOLLOW_RANGE} from {@code Mob}), so without this
 * the temptation goal would throw as soon as it is evaluated.
 *
 * <p>The value mirrors vanilla's attribute default (10.0).
 */
@Mixin(Villager.class)
public abstract class VillagerTemptRangeAttributeMixin
{
    private static final double CARPET_LSC_TEMPT_RANGE = 10.0D;

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void carpetLsc$addTemptRange(CallbackInfoReturnable<AttributeSupplier.Builder> cir)
    {
        cir.setReturnValue(cir.getReturnValue().add(Attributes.TEMPT_RANGE, CARPET_LSC_TEMPT_RANGE));
    }
}

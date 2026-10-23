package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.VillagerBackpack;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Holds a villager in place while its backpack screen is open. */
@Mixin(Villager.class)
public abstract class VillagerSoftFreezeMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    private void carpetLsc$softFreeze(CallbackInfo ci)
    {
        VillagerBackpack.tick((Villager) (Object) this);
    }
}

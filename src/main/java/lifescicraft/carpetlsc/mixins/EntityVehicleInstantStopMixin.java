package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.VehicleInstantStop;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Brings a boat or minecart to a stop as soon as the rider leaves it.
 *
 * <p>The vehicle is read at {@code HEAD}, because {@code stopRiding} clears the link before it
 * returns; zeroing the motion there survives the rest of the dismount.
 */
@Mixin(Entity.class)
public abstract class EntityVehicleInstantStopMixin
{
    @Inject(method = "stopRiding", at = @At("HEAD"))
    private void carpetLsc$stopVehicle(CallbackInfo ci)
    {
        Entity passenger = (Entity) (Object) this;
        Entity vehicle = passenger.getVehicle();

        if (vehicle != null && VehicleInstantStop.applies(vehicle))
        {
            VehicleInstantStop.stop(vehicle);
        }
    }
}

package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.VehicleBoost;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Drives the per-tick part of the firework boost: one burst per player at most. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerVehicleBoostMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    private void carpetLsc$vehicleBoostTick(CallbackInfo ci)
    {
        VehicleBoost.tick((ServerPlayer) (Object) this);
    }
}

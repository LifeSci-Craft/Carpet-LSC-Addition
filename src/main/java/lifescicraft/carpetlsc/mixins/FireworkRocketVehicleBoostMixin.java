package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.VehicleBoost;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spends a firework rocket on the vehicle the player rides, instead of doing nothing.
 *
 * <p>Only the server side runs the boost; the client keeps vanilla behaviour and simply sees the
 * vehicle speed up.
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketVehicleBoostMixin
{
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void carpetLsc$vehicleBoost(Level level, Player player, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!(level instanceof ServerLevel) || !(player instanceof ServerPlayer serverPlayer))
        {
            return;
        }

        if (VehicleBoost.tryBoost(serverPlayer, player.getItemInHand(hand)))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.BundleAutoEat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives bundle auto-eating.
 *
 * <p>{@code FoodData.tick(ServerPlayer)} is the server-side hunger tick, so it already only runs
 * for real players. It is called every tick, so the helper throttles itself to once per second.
 */
@Mixin(FoodData.class)
public abstract class FoodDataBundleAutoEatMixin
{
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"))
    private void carpetLsc$bundleAutoEat(ServerPlayer player, CallbackInfo ci)
    {
        BundleAutoEat.tick(player);
    }
}

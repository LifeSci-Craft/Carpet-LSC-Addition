package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.PaintingSwap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Turns a sneak right-click on a painting into a variant swap.
 *
 * <p>{@code Player#interactOn} is the single method the server runs for an entity right-click
 * ({@code ServerGamePacketListenerImpl#handleInteract} ends up here), and {@code ServerPlayer}
 * does not override it, so hooking it here catches every server-side interaction exactly once.
 *
 * <p>{@link PaintingSwap} itself checks the server level, so the same hook is harmless if the mod
 * ever runs on a client.
 */
@Mixin(Player.class)
public abstract class PlayerPaintingSwapMixin
{
    @Inject(
            method = "interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void carpetLsc$quickPaintingSwap(Entity target, InteractionHand hand, Vec3 location,
                                             CallbackInfoReturnable<InteractionResult> cir)
    {
        if (hand != InteractionHand.MAIN_HAND)
        {
            return;
        }

        if (PaintingSwap.tryCycle((Player) (Object) this, target))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

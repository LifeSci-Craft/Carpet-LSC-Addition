package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.RightClickHarvest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Turns an empty-handed right-click on a grown crop into a harvest.
 *
 * <p>Hooked at the same place as the totem wrench: {@code useItemOn} runs before
 * {@code BlockState.useItemOn} and {@code ItemStack.useOn}, so returning a value here also cancels
 * placement. The two rules never fight because the wrench needs a totem in hand and this one needs
 * an empty hand.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeHarvestMixin
{
    @Shadow
    protected ServerLevel level;

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void carpetLsc$rightClickHarvest(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
                                             BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (hand != InteractionHand.MAIN_HAND)
        {
            return;
        }

        if (RightClickHarvest.tryHarvest(player, this.level, hitResult.getBlockPos()))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.TotemWrench;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Main-hand totem wrench: right-clicking a block with a totem of undying rotates it instead
 * of using the block / placing a block.
 *
 * <p>Injected before the target position and its state are loaded, which is ahead of both
 * {@code BlockState.useItemOn} and {@code ItemStack.useOn} (the actual placement). Returning
 * a value here therefore also cancels placement. Carpet itself redirects
 * {@code BlockState.useItemOn} for its cactus wrench; injecting earlier avoids clashing with
 * that redirect.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeTotemWrenchMixin
{
    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void carpetLsc$totemWrench(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
                                       BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!TotemWrench.isActiveWrench(player, hand))
        {
            return;
        }

        BlockState state = level.getBlockState(hitResult.getBlockPos());

        if (TotemWrench.tryRotate(state, level, player, hand, hitResult))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

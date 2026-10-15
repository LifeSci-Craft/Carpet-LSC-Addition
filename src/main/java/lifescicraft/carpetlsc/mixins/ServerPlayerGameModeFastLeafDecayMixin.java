package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.FastLeafDecay;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Triggers fast leaf decay when a player chops a log.
 *
 * <p>{@code destroyBlock(BlockPos)} is the single choke point that both survival breaking and
 * creative instant-breaking go through, so hooking it covers every game mode. Hooking
 * {@code Block.playerDestroy} instead would miss creative mode and any break done without the
 * correct tool, because vanilla guards that call with {@code preventsBlockDrops()} and
 * {@code hasCorrectToolForDrops(state)}.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeFastLeafDecayMixin
{
    @Shadow
    protected ServerLevel level;

    @Unique
    private BlockState carpetLsc$preBreakState;

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"))
    private void carpetLsc$captureBrokenState(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        this.carpetLsc$preBreakState = this.level.getBlockState(pos);
    }

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;)Z", at = @At("RETURN"))
    private void carpetLsc$fastLeafDecay(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        BlockState broken = this.carpetLsc$preBreakState;
        this.carpetLsc$preBreakState = null;

        if (broken != null && broken.is(BlockTags.LOGS) && cir.getReturnValueZ())
        {
            FastLeafDecay.onLogBroken(this.level, pos);
        }
    }
}

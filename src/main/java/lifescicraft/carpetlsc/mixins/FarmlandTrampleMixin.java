package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.FarmlandTrample;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops players from turning farmland back into dirt by falling on it.
 *
 * <p>Vanilla does that in {@code FarmlandBlock.fallOn}, which is also where mobs trample crops.
 * Cancelling only for {@link Player} keeps mob behaviour untouched, so {@code mobGriefing} still
 * means what it says.
 *
 * <p>Whether this landing is absorbed is decided by {@link FarmlandTrample}, which also implements
 * the optional "this many tramples are fine, the next one breaks it" mode.
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandTrampleMixin
{
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void carpetLsc$noFarmlandTrample(Level level, BlockState state, BlockPos pos, Entity entity,
                                             double fallDistance, CallbackInfo ci)
    {
        if (!FarmlandTrample.shouldTrample(level, pos, entity))
        {
            ci.cancel();
        }
    }
}

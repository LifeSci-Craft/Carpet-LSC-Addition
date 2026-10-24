package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.FletchingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Right-clicking a fletching table opens the arrow crafting screen. */
@Mixin(BlockBehaviour.class)
public abstract class FletchingTableUseMixin
{
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void carpetLsc$openFletchingTable(BlockState state, Level level, BlockPos pos, Player player,
                                              BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir)
    {
        if ((Object) this != Blocks.FLETCHING_TABLE || !(player instanceof ServerPlayer serverPlayer))
        {
            return;
        }

        if (FletchingTable.tryOpen(serverPlayer))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

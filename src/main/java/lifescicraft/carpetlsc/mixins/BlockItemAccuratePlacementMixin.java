package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.BlockPlacement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Placement-state hook shared by two rules:
 * <ul>
 *     <li>{@code accurateBlockPlacement} - decodes the accurate block placement protocol
 *     (ported from Carpet Extra) that Litematica's easy place encodes into the hit vector.</li>
 *     <li>{@code totemWrench} - inverts the placement facing while a totem of undying is held
 *     in the off hand, mirroring Carpet's cactus wrench.</li>
 * </ul>
 *
 * <p>The vanilla server also rejects hit positions that are far from the targeted block, which
 * the accurate placement protocol relies on; see
 * {@link ServerGamePacketListenerImplAccuratePlacementMixin}.
 */
@Mixin(BlockItem.class)
public abstract class BlockItemAccuratePlacementMixin
{
    @Redirect(
            method = "getPlacementState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState carpetLsc$computePlacementState(Block block, BlockPlaceContext context)
    {
        return BlockPlacement.computeState(block, context);
    }
}

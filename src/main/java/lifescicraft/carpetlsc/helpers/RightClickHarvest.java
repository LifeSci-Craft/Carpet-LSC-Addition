package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Right-click harvesting: an empty-handed right-click on a fully grown crop harvests it and leaves
 * a fresh seedling behind, so a farm can be replanted without re-sowing by hand.
 *
 * <p>The hand must be empty on purpose. A non-empty hand is how a player places blocks, uses a hoe
 * or eats, and stealing that interaction would be far more disruptive than the convenience is
 * worth.
 *
 * <p>The harvest itself goes through {@code Block.dropResources} with the player as the breaking
 * entity, so drops, fortune-style bonuses and statistics behave as if the crop had been broken
 * normally.
 */
public final class RightClickHarvest
{
    private RightClickHarvest()
    {
    }

    /**
     * @return true when a crop was harvested, so the caller can report the interaction as handled.
     */
    public static boolean tryHarvest(ServerPlayer player, ServerLevel level, BlockPos pos)
    {
        if (!CarpetLscSettings.rightClickHarvest)
        {
            return false;
        }

        // Sneaking is left alone so it keeps working as "do not interact" for the blocks below.
        if (player.isShiftKeyDown() || !player.getMainHandItem().isEmpty())
        {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        BlockState replanted;

        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state))
        {
            replanted = crop.getStateForAge(0);
        }
        else if (state.getBlock() instanceof NetherWartBlock && state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE)
        {
            replanted = state.setValue(NetherWartBlock.AGE, 0);
        }
        else
        {
            return false;
        }

        Block.dropResources(state, level, pos, null, player, ItemStack.EMPTY);
        level.setBlockAndUpdate(pos, replanted);
        level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}

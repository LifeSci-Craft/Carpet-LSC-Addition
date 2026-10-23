package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Right-click harvesting: an empty-handed right-click on a fully grown crop harvests it and leaves
 * a fresh seedling behind, so a farm can be replanted without re-sowing by hand.
 *
 * <p>The hand may be empty or hold a crop/seed — see {@link #handAllowsHarvest}. A placeable block
 * still keeps its own right-click, so building next to a farm is not hijacked.
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
        if (!CarpetLscSettings.RightClickHarvest)
        {
            return false;
        }

        // Sneaking is left alone so it keeps working as "do not interact" for the blocks below.
        if (player.isShiftKeyDown() || !handAllowsHarvest(player.getMainHandItem()))
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

    /**
     * Whether the held item still allows harvesting.
     *
     * <p>Harvesting drops the produce straight into the hand, so demanding an empty hand would make
     * the player clear the hand after every single crop. Seeds and crops in hand are therefore fine.
     *
     * <p>The exception is a movable block: placing it is the player's call, so a block that would be
     * placed on the farmland (anything that is not a seed/stem/wart) keeps vanilla behaviour.
     */
    private static boolean handAllowsHarvest(ItemStack held)
    {
        if (held.isEmpty())
        {
            return true;
        }

        if (held.getItem() instanceof BlockItem blockItem)
        {
            Block block = blockItem.getBlock();
            return block instanceof CropBlock
                    || block instanceof NetherWartBlock
                    || block instanceof StemBlock;
        }

        // Tools, food and the harvested produce itself do nothing on a ripe crop.
        return true;
    }
}

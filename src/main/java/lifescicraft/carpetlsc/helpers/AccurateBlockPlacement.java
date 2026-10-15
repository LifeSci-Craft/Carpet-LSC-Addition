package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

/**
 * Accurate block placement protocol, ported from Carpet Extra.
 *
 * <p>The client (Litematica's easy place, or Tweakeroo's flexible placement) encodes the
 * desired block state into the X coordinate of the hit vector. The encoded X is
 * {@code blockX + relativeHitX}, where {@code relativeHitX = (value * 2) + 2}. This class
 * decodes that value and applies the requested facing/axis/half/slab type/delay/mode.
 *
 * <p>The encoder lives in Litematica's {@code WorldUtils.applyCarpetProtocolHitVec}; this is
 * the matching decoder.
 */
public final class AccurateBlockPlacement
{
    private AccurateBlockPlacement()
    {
    }

    public static BlockState alternativeBlockPlacement(Block block, BlockPlaceContext context)
    {
        if (!CarpetLscSettings.accurateBlockPlacement)
        {
            return null;
        }

        Vec3 hitPos = context.getClickLocation();
        BlockPos blockPos = context.getClickedPos();
        double relativeHitX = hitPos.x - blockPos.getX();
        BlockState state = block.getStateForPlacement(context);

        // Nothing encoded in the hit vector: leave vanilla behaviour untouched.
        if (relativeHitX < 2.0 || state == null)
        {
            return null;
        }

        int protocolValue = ((int) relativeHitX - 2) / 2;

        EnumProperty<Direction> directionProp = getFirstDirectionProperty(state);

        if (directionProp != null)
        {
            Direction origFacing = state.getValue(directionProp);
            Direction facing = origFacing;
            int facingIndex = protocolValue & 0xF;

            if (facingIndex == 6)
            {
                // Special case: the opposite of the normal facing was requested.
                facing = facing.getOpposite();
            }
            else if (facingIndex >= 0 && facingIndex <= 5)
            {
                facing = Direction.from3DDataValue(facingIndex);
            }

            if (!directionProp.getPossibleValues().contains(facing))
            {
                facing = context.getPlayer() == null
                        ? origFacing
                        : context.getPlayer().getDirection().getOpposite();
            }

            if (facing != origFacing && directionProp.getPossibleValues().contains(facing))
            {
                if (block instanceof BedBlock)
                {
                    // Refuse the change when the bed's head would not fit.
                    BlockPos headPos = blockPos.relative(facing);
                    if (!context.getLevel().getBlockState(headPos).canBeReplaced(context))
                    {
                        return null;
                    }
                }

                state = state.setValue(directionProp, facing);
            }
        }
        else if (state.hasProperty(BlockStateProperties.AXIS))
        {
            Direction.Axis axis = Direction.Axis.VALUES[protocolValue % 3];
            state = state.setValue(BlockStateProperties.AXIS, axis);
        }

        protocolValue &= 0xFFFFFFF0;

        if (protocolValue >= 16)
        {
            if (block instanceof RepeaterBlock)
            {
                Integer delay = protocolValue / 16;

                if (RepeaterBlock.DELAY.getPossibleValues().contains(delay))
                {
                    state = state.setValue(RepeaterBlock.DELAY, delay);
                }
            }
            else if (protocolValue == 16)
            {
                if (block instanceof ComparatorBlock)
                {
                    state = state.setValue(ComparatorBlock.MODE, ComparatorMode.SUBTRACT);
                }
                else if (state.hasProperty(BlockStateProperties.HALF)
                        && state.getValue(BlockStateProperties.HALF) == Half.BOTTOM)
                {
                    state = state.setValue(BlockStateProperties.HALF, Half.TOP);
                }
                else if (state.hasProperty(BlockStateProperties.SLAB_TYPE)
                        && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM)
                {
                    state = state.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
                }
            }
        }

        return state;
    }

    @SuppressWarnings("unchecked")
    private static EnumProperty<Direction> getFirstDirectionProperty(BlockState state)
    {
        for (Property<?> property : state.getProperties())
        {
            if (property instanceof EnumProperty<?> enumProperty
                    && enumProperty.getValueClass() == Direction.class)
            {
                return (EnumProperty<Direction>) enumProperty;
            }
        }

        return null;
    }
}

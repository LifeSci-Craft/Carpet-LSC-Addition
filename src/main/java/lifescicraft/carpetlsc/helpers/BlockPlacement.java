package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Computes the final placement state for a block item, combining the two placement-related
 * rules of this extension.
 *
 * <p>Order of precedence:
 * <ol>
 *     <li>Accurate block placement protocol ({@code accurateBlockPlacement}), when the client
 *     actually encoded a state into the hit vector.</li>
 *     <li>Off-hand totem inversion ({@code totemWrench}), which mirrors Carpet's cactus
 *     wrench for blocks placed while a totem of undying is held in the off hand.</li>
 * </ol>
 *
 * <p>Note on the off-hand inversion: Carpet implements its cactus facing inversion by
 * redirecting the player-facing / clicked-face / view-rotation calls that vanilla placement
 * reads. Those exact call sites are already redirected by Carpet itself, and redirecting them
 * a second time would conflict and break Carpet's cactus wrench. This extension therefore
 * reaches the same user-visible result by inverting the computed state instead of the input
 * ray, which composes safely with Carpet.
 */
public final class BlockPlacement
{
    private BlockPlacement()
    {
    }

    public static BlockState computeState(Block block, BlockPlaceContext context)
    {
        BlockState vanilla = block.getStateForPlacement(context);

        BlockState accelerated = AccurateBlockPlacement.alternativeBlockPlacement(block, context);
        if (accelerated != null)
        {
            // The client asked for a specific state; honour it as-is.
            return accelerated;
        }

        if (vanilla != null && CarpetLscSettings.totemWrench && holdsOffhandTotem(context))
        {
            return invertFacing(vanilla);
        }

        return vanilla;
    }

    private static boolean holdsOffhandTotem(BlockPlaceContext context)
    {
        Player player = context.getPlayer();

        return player != null && TotemWrench.isOffhandTotem(player);
    }

    /**
     * Inverts the orientation of a placement state: direction properties are flipped, and
     * pillar axes are swapped between X and Z (Y is left alone).
     */
    private static BlockState invertFacing(BlockState state)
    {
        EnumProperty<Direction> directionProperty = firstDirectionProperty(state);

        if (directionProperty != null)
        {
            Direction current = state.getValue(directionProperty);
            Direction inverted = current.getOpposite();

            if (directionProperty.getPossibleValues().contains(inverted))
            {
                return state.setValue(directionProperty, inverted);
            }

            return state;
        }

        if (state.hasProperty(BlockStateProperties.AXIS))
        {
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);

            if (axis != Direction.Axis.Y)
            {
                return state.setValue(BlockStateProperties.AXIS,
                        axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
            }
        }

        return state;
    }

    @SuppressWarnings("unchecked")
    private static EnumProperty<Direction> firstDirectionProperty(BlockState state)
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

package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fast leaf decay: once a tree's trunk is chopped down, the leaves that belonged to it decay
 * right away instead of waiting for random ticks.
 *
 * <p>Rather than guessing which leaves belong together geometrically, this recomputes
 * vanilla's own leaf {@code distance} value for the affected canopy:
 *
 * <ol>
 *     <li>Flood fill the connected mass of non-persistent leaves that touches the broken log.</li>
 *     <li>Collect every log adjacent to that mass. Those are the "sources" vanilla counts from
 *     (a log contributes distance 0 to its neighbours).</li>
 *     <li>Propagate distance outwards through the leaf mass, exactly like
 *     {@code LeavesBlock.updateDistance}: a leaf's distance is one more than its closest
 *     neighbour's, capped at {@link LeavesBlock#DECAY_DISTANCE}.</li>
 *     <li>Leaves left without any log within range get the decay distance and are decayed
 *     through vanilla's own leaf random tick, so drops and loot use vanilla behaviour.</li>
 * </ol>
 *
 * <p>This is inherently safe around other trees: a leaf that still has a path to a log in the
 * same mass keeps a small distance and is only re-synchronised, never destroyed. That is what
 * makes it work in dense forests where canopies touch.
 *
 * <p>Persistent leaves (placed by players) are never part of the flood fill and are never
 * touched.
 */
public final class FastLeafDecay
{
    /**
     * Upper bound on the canopy size handled in one go. Larger canopies are left to vanilla
     * rather than risking a partial, inconsistent update.
     */
    private static final int MAX_CANOPY = 4096;

    private FastLeafDecay()
    {
    }

    public static void onLogBroken(ServerLevel level, BlockPos logPos)
    {
        if (!CarpetLscSettings.fastLeafDecay)
        {
            return;
        }

        Set<BlockPos> canopy = new HashSet<>();
        List<BlockPos> logSources = new ArrayList<>();
        Set<BlockPos> logSeen = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        // Seed with the leaves directly around the log that was just removed.
        for (Direction direction : Direction.values())
        {
            BlockPos neighbour = logPos.relative(direction);

            if (isDecayableLeaf(level.getBlockState(neighbour)) && canopy.add(neighbour))
            {
                queue.add(neighbour);
            }
        }

        // Flood fill the connected leaf mass and collect the logs bordering it.
        while (!queue.isEmpty())
        {
            if (canopy.size() > MAX_CANOPY)
            {
                // Too large to reason about safely: leave everything to vanilla.
                return;
            }

            BlockPos current = queue.poll();

            for (Direction direction : Direction.values())
            {
                BlockPos neighbour = current.relative(direction);
                BlockState state = level.getBlockState(neighbour);

                if (state.is(BlockTags.LOGS))
                {
                    if (logSeen.add(neighbour))
                    {
                        logSources.add(neighbour);
                    }
                }
                else if (isDecayableLeaf(state) && canopy.add(neighbour))
                {
                    queue.add(neighbour);
                }
            }
        }

        if (canopy.isEmpty())
        {
            return;
        }

        // Propagate vanilla's distance value through the canopy, starting from the logs.
        Map<BlockPos, Integer> distances = new HashMap<>();
        Deque<BlockPos> propagate = new ArrayDeque<>();

        for (BlockPos log : logSources)
        {
            for (Direction direction : Direction.values())
            {
                BlockPos neighbour = log.relative(direction);

                if (canopy.contains(neighbour) && distances.getOrDefault(neighbour, Integer.MAX_VALUE) > 1)
                {
                    distances.put(neighbour, 1);
                    propagate.add(neighbour);
                }
            }
        }

        while (!propagate.isEmpty())
        {
            BlockPos current = propagate.poll();
            int distance = distances.get(current);

            if (distance >= LeavesBlock.DECAY_DISTANCE)
            {
                continue;
            }

            for (Direction direction : Direction.values())
            {
                BlockPos neighbour = current.relative(direction);

                if (canopy.contains(neighbour) && distances.getOrDefault(neighbour, Integer.MAX_VALUE) > distance + 1)
                {
                    distances.put(neighbour, distance + 1);
                    propagate.add(neighbour);
                }
            }
        }

        // Apply: unreachable leaves are orphaned and decay; the rest just get resynchronised.
        for (BlockPos pos : canopy)
        {
            BlockState state = level.getBlockState(pos);

            if (!isDecayableLeaf(state))
            {
                continue;
            }

            int distance = distances.getOrDefault(pos, LeavesBlock.DECAY_DISTANCE);
            int current = state.getValue(LeavesBlock.DISTANCE);

            if (distance >= LeavesBlock.DECAY_DISTANCE)
            {
                BlockState decaying = state.setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
                level.setBlockAndUpdate(pos, decaying);

                // Vanilla leaf random tick drops the resources and removes the block.
                decaying.randomTick(level, pos, level.getRandom());
            }
            else if (current != distance)
            {
                level.setBlockAndUpdate(pos, state.setValue(LeavesBlock.DISTANCE, distance));
            }
        }
    }

    private static boolean isDecayableLeaf(BlockState state)
    {
        return state.is(BlockTags.LEAVES)
                && state.hasProperty(LeavesBlock.PERSISTENT)
                && !state.getValue(LeavesBlock.PERSISTENT)
                && state.hasProperty(LeavesBlock.DISTANCE);
    }
}

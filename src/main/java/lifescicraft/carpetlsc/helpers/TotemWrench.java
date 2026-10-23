package lifescicraft.carpetlsc.helpers;

import carpet.CarpetSettings;
import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Totem wrench.
 *
 * <p>Main hand: right-clicking a block while holding a totem of undying rotates/flips the
 * targeted block, without placing anything.
 *
 * <p>Off hand: holding a totem of undying mirrors Carpet's cactus behaviour, i.e. the
 * placement facing of a block being placed is inverted.
 */
public final class TotemWrench
{
    private TotemWrench()
    {
    }

    private static boolean holdsTotem(Player player, InteractionHand hand)
    {
        return player.getItemInHand(hand).getItem() == Items.TOTEM_OF_UNDYING;
    }

    /**
     * True when the player is actively using a totem as a wrench in the given hand.
     * Used by the right-click hook.
     */
    public static boolean isActiveWrench(Player player, InteractionHand hand)
    {
        return CarpetLscSettings.TotemWrench && player.getAbilities().mayBuild && holdsTotem(player, hand);
    }

    /**
     * Rotates the targeted block. Returns true when the state actually changed, in which
     * case the caller must not run the normal "use item on block" behaviour.
     *
     * <p>Reuses Carpet's public {@code flipBlock}, and sets the same
     * {@code impendingFillSkipUpdates} flag Carpet's cactus logic does so that rotating
     * does not cause block updates.
     */
    public static boolean tryRotate(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hit)
    {
        CarpetSettings.impendingFillSkipUpdates.set(true);
        boolean rotated;
        try
        {
            rotated = carpet.helpers.BlockRotator.flipBlock(state, level, player, hand, hit);
        }
        finally
        {
            CarpetSettings.impendingFillSkipUpdates.set(false);
        }

        return rotated;
    }

    /**
     * Mirrors Carpet's {@code flippinEligibility} but for a totem of undying carried in the
     * off hand. Used to invert placement facing while a totem is held off-hand.
     */
    public static boolean isOffhandTotem(Entity entity)
    {
        return CarpetLscSettings.TotemWrench
                && entity instanceof Player player
                && player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING;
    }
}

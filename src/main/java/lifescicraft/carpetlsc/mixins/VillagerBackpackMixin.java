package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.VillagerBackpack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Turns sneaking + right-click on a named villager into opening its backpack.
 *
 * <p>The injection sits at {@code HEAD} and only answers for a server-side main-hand interaction, so
 * the ordinary trade screen is untouched when the player is not sneaking.
 */
@Mixin(Villager.class)
public abstract class VillagerBackpackMixin
{
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void carpetLsc$villagerBackpack(Player player, InteractionHand hand,
                                            CallbackInfoReturnable<InteractionResult> cir)
    {
        if (hand == InteractionHand.MAIN_HAND
                && player instanceof ServerPlayer serverPlayer
                && VillagerBackpack.tryOpen(serverPlayer, (Villager) (Object) this))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}

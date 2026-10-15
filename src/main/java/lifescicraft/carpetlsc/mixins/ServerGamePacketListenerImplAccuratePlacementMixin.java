package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Part of the accurate block placement protocol (ported from Carpet Extra).
 *
 * <p>Vanilla validates the hit position sent by the client and rejects placements whose
 * encoded offset puts the hit vector far outside the targeted block ("Location ... too far
 * away from hit block"). The accurate placement protocol deliberately encodes data there, so
 * that check has to be bypassed while the rule is enabled.
 *
 * <p>Carpet does not redirect this call, and there is exactly one
 * {@code Vec3.subtract(Vec3)} invocation in {@code handleUseItemOn}.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplAccuratePlacementMixin
{
    @Redirect(
            method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
            ),
            require = 0
    )
    private Vec3 carpetLsc$skipHitPosCheck(Vec3 hitVec, Vec3 blockCenter)
    {
        if (CarpetLscSettings.accurateBlockPlacement)
        {
            return Vec3.ZERO;
        }

        return hitVec.subtract(blockCenter);
    }
}

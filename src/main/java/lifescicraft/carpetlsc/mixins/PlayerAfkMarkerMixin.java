package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.AfkMarker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the AFK marker.
 *
 * <p>{@code Player#tick} runs on both sides, so the body is guarded to server players only. The
 * hook is a no-op unless {@code afkMarker} is on, in which case {@link AfkMarker} restores any name
 * it changed.
 */
@Mixin(Player.class)
public abstract class PlayerAfkMarkerMixin
{
    @Inject(method = "tick", at = @At("TAIL"))
    private void carpetLsc$afkMarker(CallbackInfo ci)
    {
        if ((Object) this instanceof ServerPlayer serverPlayer)
        {
            AfkMarker.tick(serverPlayer);
        }
    }
}

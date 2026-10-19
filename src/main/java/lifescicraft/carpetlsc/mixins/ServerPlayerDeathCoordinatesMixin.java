package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.DeathCoordinates;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Announces the death position once the death itself has been processed.
 *
 * <p>Injected at {@code RETURN} so the vanilla death message has already been sent and the
 * coordinates line lands right after it.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathCoordinatesMixin
{
    @Inject(method = "die", at = @At("RETURN"))
    private void carpetLsc$deathCoordinates(DamageSource source, CallbackInfo ci)
    {
        DeathCoordinates.announce((ServerPlayer) (Object) this);
    }
}

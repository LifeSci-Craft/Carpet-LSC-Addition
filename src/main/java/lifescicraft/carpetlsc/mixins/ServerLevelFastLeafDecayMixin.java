package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.FastLeafDecay;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * Drives the delayed half of fast leaf decay.
 *
 * <p>Leaves are only ever removed by {@code LeavesBlock.randomTick}; {@code LeavesBlock.tick}
 * merely recomputes the distance property, so {@code ServerLevel.scheduleTick} cannot be used to
 * stagger the removal. Vanilla's other delayed-execution queue
 * ({@code MinecraftServer.schedule(new TickTask(...))}) is not usable either: its
 * {@code shouldRun} returns true as soon as the server has spare time in the tick, which would
 * collapse the whole canopy back into an instant decay on an idle server.
 *
 * <p>So the pending decays are kept in {@link FastLeafDecay} and drained here, once per level per
 * tick, at the end of the level tick.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelFastLeafDecayMixin
{
    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    private void carpetLsc$runDelayedLeafDecay(BooleanSupplier hasTimeLeft, CallbackInfo ci)
    {
        FastLeafDecay.tickDelayedDecay((ServerLevel) (Object) this);
    }
}

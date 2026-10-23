package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.CarpetLscSettings;
import lifescicraft.carpetlsc.helpers.SleepRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Daytime sleeping, part 3 of 3: advances the clock to nightfall instead of to morning.
 *
 * <p>When enough players are sleeping, {@code ServerLevel.tick} pulls the dimension's clock
 * forward to the {@code WAKE_UP_FROM_SLEEP} time marker (the next morning). Sleeping during the
 * day should instead skip to sunset, which is the {@code NIGHT} time marker.
 *
 * <p>This is done by redirecting the <em>marker field read</em> rather than the
 * {@code moveToTimeMarker} call itself. The return type of {@code moveToTimeMarker} differs
 * between Minecraft versions ({@code boolean} up to 26.2, a {@code MoveResult} enum in 26.3),
 * so redirecting the call would tie this class to one version, while the marker field is a plain
 * {@code ResourceKey<ClockTimeMarker>} in every supported version. The vanilla call discards the
 * return value either way.
 *
 * <p>The gamerule check and the {@code areEnoughSleeping} / {@code areEnoughDeepSleeping}
 * conditions happen before this call and are untouched, so {@code playersSleepingPercentage}
 * keeps its vanilla meaning. A value of 0 naturally means "one player is enough", because
 * vanilla computes {@code sleepersNeeded = max(1, ceil(activePlayers * pct / 100))}.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelDaytimeSleepMixin
{
    @Redirect(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/clock/ClockTimeMarkers;WAKE_UP_FROM_SLEEP:Lnet/minecraft/resources/ResourceKey;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            )
    )
    private ResourceKey<ClockTimeMarker> carpetLsc$skipToNightfall()
    {
        if (CarpetLscSettings.DaytimeSleep)
        {
            ServerLevel self = (ServerLevel) (Object) this;

            // Dimensions without a day/night cycle keep vanilla behaviour.
            if (!self.dimensionType().hasFixedTime() && SleepRules.isDaytime(self))
            {
                return ClockTimeMarkers.NIGHT;
            }
        }

        return ClockTimeMarkers.WAKE_UP_FROM_SLEEP;
    }
}

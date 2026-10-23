package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.CarpetLscSettings;
import lifescicraft.carpetlsc.helpers.SleepRules;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Daytime sleeping, part 2 of 3: keeps the player in bed while the sun is up.
 *
 * <p>{@code Player.tick} re-evaluates the bed rule every tick and, when it fails, calls
 * {@code stopSleepInBed(false, true)} - which throws the player straight back out of bed.
 * Allowing only the initial entry (see {@link ServerPlayerDaytimeSleepMixin}) is therefore not
 * enough: sleeping by day would be undone on the very next tick unless this second call site is
 * handled too.
 */
@Mixin(Player.class)
public abstract class PlayerDaytimeSleepMixin
{
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"
            )
    )
    private boolean carpetLsc$keepSleepingByDay(BedRule bedRule, Level level)
    {
        if (CarpetLscSettings.DaytimeSleep && SleepRules.isRelaxableDayRule(bedRule, level))
        {
            return true;
        }

        return bedRule.canSleep(level);
    }
}

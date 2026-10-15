package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.CarpetLscSettings;
import lifescicraft.carpetlsc.helpers.SleepRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Daytime sleeping, part 1 of 3: lets players get into a bed while the sun is still up.
 *
 * <p>In 26.3 the "you can only sleep at night" restriction lives in the bed's {@link BedRule}
 * environment attribute. {@code ServerPlayer.startSleepInBed} evaluates
 * {@code BedRule.canSleep(Level)}, which for the default bed rule ends up testing
 * {@code Level.isDarkOutside()}, and refuses daytime sleep. This redirect forces only that
 * dark-time requirement to pass, leaving every other check (distance, obstruction, monsters,
 * already sleeping, ...) intact.
 *
 * <p>The per-tick re-check in {@code Player.tick} is handled by {@link PlayerDaytimeSleepMixin}
 * and the time skip by {@link ServerLevelDaytimeSleepMixin}.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDaytimeSleepMixin
{
    @Redirect(
            method = "startSleepInBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"
            )
    )
    private boolean carpetLsc$allowDaytimeSleep(BedRule bedRule, Level level)
    {
        if (CarpetLscSettings.daytimeSleep && SleepRules.isRelaxableDayRule(bedRule, level))
        {
            return true;
        }

        return bedRule.canSleep(level);
    }
}

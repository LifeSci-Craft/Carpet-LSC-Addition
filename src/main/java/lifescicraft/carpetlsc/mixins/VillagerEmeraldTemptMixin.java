package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.EmeraldTemptGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Emerald temptation for villagers (rule {@code emeraldTemptsVillagers}).
 *
 * <p>Villagers are driven by a {@code Brain}, but {@code Mob.serverAiStep()} is final and ticks
 * the regular goal selector for every mob regardless, so a {@link EmeraldTemptGoal} registered
 * here is evaluated normally.
 *
 * <p>The goal is added in the constructor rather than in {@code registerGoals()} because
 * {@code Villager} does not override {@code registerGoals()} - the inherited method runs during
 * the superclass constructor, before subclass state exists.
 *
 * <p>Two details are version sensitive and both are handled here:
 * <ul>
 *     <li>The constructor is matched by its full descriptor. Minecraft 26.1 declares three
 *     {@code Villager} constructors while 26.2 and 26.3 declare only this one, so a bare
 *     {@code method = "<init>"} would inject into all three on 26.1 and register the goal up to
 *     three times.</li>
 *     <li>The goal selector is reached through {@link MobGoalSelectorAccessor}, because the
 *     public {@code getGoalSelector()} accessor only exists from 26.2 onwards.</li>
 * </ul>
 */
@Mixin(Villager.class)
public abstract class VillagerEmeraldTemptMixin
{
    private static final int CARPET_LSC_TEMPT_PRIORITY = 0;

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void carpetLsc$addEmeraldTemptGoal(EntityType<Villager> entityType, Level level, CallbackInfo ci)
    {
        Mob self = (Mob) (Object) this;

        ((MobGoalSelectorAccessor) self).carpetLsc$getGoalSelector()
                .addGoal(CARPET_LSC_TEMPT_PRIORITY, new EmeraldTemptGoal((Villager) self));
    }
}

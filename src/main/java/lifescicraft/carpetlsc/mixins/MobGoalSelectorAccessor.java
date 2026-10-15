package lifescicraft.carpetlsc.mixins;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the protected {@code goalSelector} of {@link Mob} as a public accessor.
 *
 * <p>This exists because the way the goal selector is reached differs between the supported
 * Minecraft versions: 26.2 and 26.3 declare a public {@code getGoalSelector()} method, but 26.1
 * does not. The {@code goalSelector} field itself is present in 26.1, 26.2 and 26.3, so an
 * accessor on it works everywhere.
 *
 * <p>It is applied to {@code Mob} rather than relying on a {@code @Shadow} of the inherited
 * field from a subclass mixin, which Mixin does not resolve.
 */
@Mixin(Mob.class)
public interface MobGoalSelectorAccessor
{
    @Accessor("goalSelector")
    GoalSelector carpetLsc$getGoalSelector();
}

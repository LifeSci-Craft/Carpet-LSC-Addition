package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Zombie villagers cure in three seconds, and a weakness potion can be used by hand (rule
 * {@code FastZombieCure}).
 *
 * <p>Vanilla picks a random three to five minute timer and only accepts a splash potion. The timer
 * is replaced here, and right-clicking with a weakness potion applies the effect and leaves an empty
 * bottle, the same way drinking it would.
 */
@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerCureMixin
{
    /** Three seconds. */
    private static final int CURE_TICKS = 60;

    /** Sixty seconds of weakness, long enough to hand over the golden apple. */
    private static final int WEAKNESS_TICKS = 1200;

    @ModifyVariable(method = "startConverting", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int carpetLsc$fastCure(int conversionTime)
    {
        return CarpetLscSettings.FastZombieCure ? CURE_TICKS : conversionTime;
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void carpetLsc$weaknessByHand(Player player, InteractionHand hand,
                                          CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!CarpetLscSettings.FastZombieCure)
        {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);

        if (!isWeaknessPotion(stack))
        {
            return;
        }

        ZombieVillager self = (ZombieVillager) (Object) this;
        self.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS, 0));

        stack.shrink(1);

        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.hasInfiniteMaterials())
        {
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    private static boolean isWeaknessPotion(ItemStack stack)
    {
        if (!stack.is(Items.POTION))
        {
            return false;
        }

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

        return contents != null
                && contents.potion().flatMap(Holder::unwrapKey)
                        .map(key -> key.identifier().equals(Identifier.withDefaultNamespace("weakness")))
                        .orElse(false);
    }
}

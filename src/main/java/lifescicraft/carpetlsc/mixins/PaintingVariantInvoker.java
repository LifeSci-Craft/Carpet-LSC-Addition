package lifescicraft.carpetlsc.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@code Painting#setVariant}, which is private in vanilla.
 *
 * <p>The method only writes the {@code DATA_PAINTING_VARIANT_ID} synched data entry, so calling it
 * changes the artwork, marks the entity data dirty and lets vanilla broadcast the change to every
 * tracking client — no packet needs to be built by hand.
 */
@Mixin(Painting.class)
public interface PaintingVariantInvoker
{
    @Invoker("setVariant")
    void carpetLsc$setVariant(Holder<PaintingVariant> variant);
}

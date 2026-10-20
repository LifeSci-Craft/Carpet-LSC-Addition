package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import lifescicraft.carpetlsc.mixins.PaintingVariantInvoker;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;

/**
 * Quick painting swap: sneaking while right-clicking a placed painting cycles it to the next
 * painting variant of the same size.
 *
 * <p>Restricting the swap to variants of identical {@code width}×{@code height} is what makes this
 * safe: a painting's bounding box is derived from its variant, so keeping the size means the
 * entity never has to move or resize — only the artwork changes.
 *
 * <p>{@code Painting#setVariant} is private, so the write goes through
 * {@link PaintingVariantInvoker}. That method only writes the synched data entry, which the game
 * then broadcasts to every tracking client.
 */
public final class PaintingSwap
{
    private PaintingSwap()
    {
    }

    /**
     * @return true when the painting was actually switched, so the caller can report the
     *         interaction as handled.
     */
    public static boolean tryCycle(Player player, Entity target)
    {
        if (!CarpetLscSettings.quickPaintingSwap)
        {
            return false;
        }

        if (!player.isShiftKeyDown() || !(target instanceof Painting painting))
        {
            return false;
        }

        if (!(painting.level() instanceof ServerLevel level))
        {
            return false;
        }

        Holder<PaintingVariant> current = painting.getVariant();
        PaintingVariant size = current.value();

        Registry<PaintingVariant> registry =
                level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT);

        // Sorted by id so the cycle is deterministic across worlds and reloads.
        List<Holder.Reference<PaintingVariant>> sameSize = registry.listElements()
                .filter(holder ->
                {
                    PaintingVariant variant = holder.value();
                    return variant.width() == size.width() && variant.height() == size.height();
                })
                .sorted(Comparator.comparing(
                        (Holder.Reference<PaintingVariant> holder) -> holder.key().identifier().toString()))
                .toList();

        if (sameSize.size() < 2)
        {
            return false;
        }

        int index = -1;

        for (int i = 0; i < sameSize.size(); i++)
        {
            if (sameSize.get(i).is(current))
            {
                index = i;
                break;
            }
        }

        if (index < 0)
        {
            return false;
        }

        Holder<PaintingVariant> next = sameSize.get((index + 1) % sameSize.size());
        ((PaintingVariantInvoker) painting).carpetLsc$setVariant(next);
        painting.playPlacementSound();
        return true;
    }
}

package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Unlocks every recipe for the joining player (rule {@code alwaysUnlockedRecipes}).
 *
 * <p>Runs just before the initial recipe book is sent, so the client's first sync already contains
 * the full set and no follow-up packet is needed.
 *
 * <p>The book is only ever added to: whatever the player had already unlocked in this world stays
 * exactly as it was, and nothing is written back on its behalf. Turning the rule off simply stops
 * adding more; recipes unlocked while it was on remain unlocked, which is the same behaviour as any
 * other recipe the player has learned.
 */
public final class RecipeUnlocker
{
    private RecipeUnlocker()
    {
    }

    public static void unlockAll(ServerRecipeBook book, ServerPlayer player)
    {
        if (!CarpetLscSettings.alwaysUnlockedRecipes)
        {
            return;
        }

        for (RecipeHolder<?> recipe : player.level().getServer().getRecipeManager().getRecipes())
        {
            book.add(recipe.id());
        }
    }
}

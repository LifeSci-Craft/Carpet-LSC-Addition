package lifescicraft.carpetlsc.helpers;

import carpet.CarpetServer;
import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;

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
        if (!CarpetLscSettings.AlwaysUnlockedRecipes)
        {
            return;
        }

        for (RecipeHolder<?> recipe : player.level().getServer().getRecipeManager().getRecipes())
        {
            book.add(recipe.id());
        }
    }

    /**
     * Unlocks everything for the players who are already online, at the moment the rule is turned on.
     *
     * <p>{@code ServerRecipeBook#addRecipes} also sends the additions to that player's client, so a
     * relog is not needed.
     */
    public static void applyToOnline()
    {
        if (!CarpetLscSettings.AlwaysUnlockedRecipes)
        {
            return;
        }

        MinecraftServer server = CarpetServer.minecraft_server;
        Collection<RecipeHolder<?>> recipes = server.getRecipeManager().getRecipes();

        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            player.getRecipeBook().addRecipes(recipes, player);
        }
    }
}

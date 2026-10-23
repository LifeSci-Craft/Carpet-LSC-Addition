package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.RecipeUnlocker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Unlocks every recipe right before the initial recipe book is sent on join.
 *
 * <p>{@code sendInitialRecipeBook} is called once from {@code PlayerList#placeNewPlayer}, and it
 * serialises whatever the book already holds. Adding the recipes at {@code HEAD} therefore lands
 * them in that first sync, with no extra packet and no client-side work.
 */
@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookUnlockAllMixin
{
    @Inject(method = "sendInitialRecipeBook(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"))
    private void carpetLsc$unlockAllRecipes(ServerPlayer player, CallbackInfo ci)
    {
        RecipeUnlocker.unlockAll((ServerRecipeBook) (Object) this, player);
    }
}

package lifescicraft.carpetlsc.mixins;

import lifescicraft.carpetlsc.helpers.HeldItemChat;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@link HeldItemChat} to player chat.
 *
 * <p>{@code PlayerList#broadcastChatMessage(PlayerChatMessage, ServerPlayer, ChatType.Bound)} is the
 * point where a player's message becomes visible to everyone, and it still has the sender at hand,
 * which is what the held-item lookup needs.
 *
 * <p>The original broadcast is cancelled and re-run with
 * {@code PlayerChatMessage#withUnsignedContent}. That is deliberate: the signed body cannot be
 * edited, but the unsigned content is the supported decoration slot, so clients render the rewrite
 * while signature verification still passes. Messages without {@code #} are left completely alone.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListChatItemMixin
{
    @Inject(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void carpetLsc$showHeldItem(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound bound,
                                        CallbackInfo ci)
    {
        if (HeldItemChat.isRewriting())
        {
            return;
        }

        Component decorated = HeldItemChat.decorate(sender, message);

        if (decorated == null)
        {
            return;
        }

        ci.cancel();

        HeldItemChat.setRewriting(true);

        try
        {
            ((PlayerList) (Object) this).broadcastChatMessage(message.withUnsignedContent(decorated), sender, bound);
        }
        finally
        {
            HeldItemChat.setRewriting(false);
        }
    }
}

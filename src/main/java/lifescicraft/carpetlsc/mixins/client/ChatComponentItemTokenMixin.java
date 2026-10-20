package lifescicraft.carpetlsc.mixins.client;

import lifescicraft.carpetlsc.client.ChatItemIcons;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Swaps bracketed {@code show_item} tokens for the icon marker before the message is wrapped.
 *
 * <p>Rewriting here, rather than at render time, is what lets the chat's own wrapping reserve the
 * icon's width: the marker glyph is 16px wide, so the text after the item starts after the icon.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentItemTokenMixin
{
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private Component carpetLsc$replaceItemTokens(Component message)
    {
        return ChatItemIcons.decorate(message);
    }
}

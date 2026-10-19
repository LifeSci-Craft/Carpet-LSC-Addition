package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Replaces {@code #} in a chat message with the sender's held item.
 *
 * <p>Server-side only: the rewritten line is attached as the message's <em>unsigned content</em>
 * ({@code PlayerChatMessage#withUnsignedContent}), which is exactly the slot vanilla reserves for
 * server-side decoration. The signed body is left untouched, so chat signing and validation keep
 * working; clients simply render the unsigned content instead.
 *
 * <p>The item itself is offered through a {@code show_item} hover event, which every vanilla client
 * renders as the full item tooltip. An inline item sprite is not something the vanilla chat
 * renderer can draw, so a hover tooltip is the limit of a server-only implementation.
 */
public final class HeldItemChat
{
    private static final char TOKEN = '#';

    /** Guards the re-broadcast from re-entering the mixin. */
    private static boolean rewriting = false;

    private HeldItemChat()
    {
    }

    public static boolean isRewriting()
    {
        return rewriting;
    }

    public static void setRewriting(boolean value)
    {
        rewriting = value;
    }

    /**
     * @return the replacement component, or {@code null} when the rule is off or the message has
     *         nothing to substitute.
     */
    public static Component decorate(ServerPlayer sender, PlayerChatMessage message)
    {
        if (!CarpetLscSettings.chatHeldItem || sender == null || message == null)
        {
            return null;
        }

        String raw = message.signedContent();

        if (raw == null || raw.indexOf(TOKEN) < 0)
        {
            return null;
        }

        ItemStack held = sender.getMainHandItem();

        if (held.isEmpty())
        {
            held = sender.getOffhandItem();
        }

        Component token = held.isEmpty()
                ? Component.translatable("carpet-lsc-addition.chat.emptyHand")
                : itemLabel(held);

        MutableComponent result = Component.empty();
        String[] parts = raw.split(String.valueOf(TOKEN), -1);

        for (int i = 0; i < parts.length; i++)
        {
            if (i > 0)
            {
                result.append(token.copy());
            }

            if (!parts[i].isEmpty())
            {
                result.append(Component.literal(parts[i]));
            }
        }

        return result;
    }

    private static Component itemLabel(ItemStack stack)
    {
        HoverEvent hover = new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack));

        // The hover sits on the whole label and again on the name, because a child component only
        // shows the tooltip when its own resolved style carries the event.
        return Component.literal("[")
                .append(stack.getHoverName().copy().withStyle(style -> style.withHoverEvent(hover)))
                .append(Component.literal("]"))
                .withStyle(Style.EMPTY.withHoverEvent(hover));
    }
}

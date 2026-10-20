package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.ChatFormatting;
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
 * renders as the full item tooltip.
 *
 * <p>The whole replacement is one component whose text is the bracketed item name
 * ({@code [Diamond Sword]}) and whose style carries the hover. Clients with this mod installed
 * recognise that shape and swap the text for the item's icon; every other client keeps the text
 * and the tooltip. Keeping the name in the message is what makes the fallback work, so the token
 * must stay a single component: the client replaces it as a whole.
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
        if (!CarpetLscSettings.chatHeldItem)
        {
            return null;
        }

        String raw = message.signedContent();

        if (raw.indexOf(TOKEN) < 0)
        {
            return null;
        }

        ItemStack held = sender.getMainHandItem();

        if (held.isEmpty())
        {
            held = sender.getOffhandItem();
        }

        Component token = held.isEmpty()
                ? emptyHandToken()
                : itemToken(held);

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

    /**
     * Builds the {@code [Item Name]} token, painted light purple with the full vanilla
     * {@code show_item} hover on it.
     *
     * <p>Everything lives in one component on purpose: the client-side icon swap replaces the
     * token whole, so the bracket, the name and the closing bracket must not be siblings. The
     * name stays a child component (instead of a flattened string) so that a translatable item
     * name is still localised by each client.
     */
    private static Component itemToken(ItemStack stack)
    {
        Style style = Style.EMPTY
                .withColor(ChatFormatting.LIGHT_PURPLE)
                .withHoverEvent(new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack)));

        return Component.empty()
                .withStyle(style)
                .append(Component.literal("["))
                .append(stack.getHoverName().copy())
                .append(Component.literal("]"));
    }

    /**
     * The empty-hand token.
     *
     * <p>It carries no hover, which is also what tells the client to leave it alone: there is no
     * item to draw, so the text stays text. The translatable is resolved client-side, like any
     * other message text.
     */
    private static Component emptyHandToken()
    {
        return Component.empty()
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal("["))
                .append(Component.translatable("carpet-lsc-addition.chat.emptyHand"))
                .append(Component.literal("]"));
    }
}

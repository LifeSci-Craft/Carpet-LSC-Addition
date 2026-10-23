package lifescicraft.carpetlsc.client;

import lifescicraft.carpetlsc.CarpetLscAddition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client half of {@code chatHeldItem}: draws the icon of a chat item token.
 *
 * <p>The server sends the token as one component holding the bracketed item name and a
 * {@code show_item} hover. On this client the name is swapped for a single marker character that
 * only exists in {@code assets/carpet-lsc-addition/font/chat_icon.json}; that glyph is invisible
 * but is exactly {@link #ICON_SIZE} pixels wide, so the normal chat layout reserves the room and
 * {@link #renderLine} only has to paint the item over it. Clients without the mod never see the
 * marker and keep the text.
 */
public final class ChatItemIcons
{
    /**
     * The marker codepoint. It lives in the private use area, appears in no chat message sent by
     * a client, and is only given a width by this mod's own font.
     */
    public static final int MARKER = '\uE000';

    private static final String MARKER_TEXT = Character.toString(MARKER);

    /** Item icons are authored at 16x16; the marker glyph advances by the same amount. */
    public static final int ICON_SIZE = 16;

    /** Lifts the 16px icon so it is centred on the 9px text line. */
    public static final int ICON_Y_OFFSET = -4;

    private static final FontDescription MARKER_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("carpet-lsc-addition", "chat_icon"));

    /** Cached answer of {@link #isMarkerAvailable()}; {@code null} until the first message. */
    private static Boolean markerAvailable;

    private ChatItemIcons()
    {
    }

    /**
     * Replaces every bracketed {@code show_item} token in the message with the marker glyph.
     *
     * <p>Only the token itself is touched: an item hover carried by a {@code [Name]} component is
     * how the server marks it, and requiring both keeps unrelated item hovers intact.
     */
    public static Component decorate(Component message)
    {
        if (!isMarkerAvailable() || !hasItemToken(message))
        {
            return message;
        }

        return rewrite(message);
    }

    /**
     * Whether the marker glyph resolves to a {@link #ICON_SIZE} pixel advance.
     *
     * <p>The font is a mod asset, and mod assets are only visible to the client when Fabric API
     * (specifically its resource loader module) is installed. Without it the marker would render as
     * the missing glyph, so the text is left alone instead and the server-side fallback is used.
     */
    private static boolean isMarkerAvailable()
    {
        // Only a positive answer is cached: a probe that runs before the resource reload would
        // otherwise keep the text fallback for the whole session (singleplayer hits that window).
        if (markerAvailable == null || !markerAvailable)
        {
            int width = Minecraft.getInstance().font.width(markerGlyph());
            markerAvailable = width == ICON_SIZE;

            if (!markerAvailable)
            {
                CarpetLscAddition.LOGGER.warn(
                        "Chat item icons are disabled: the {} font is not available (width {} instead of {}). "
                                + "Install Fabric API on the client to enable them.",
                        MARKER_FONT, width, ICON_SIZE);
            }
        }

        return markerAvailable;
    }

    private static Component markerGlyph()
    {
        return Component.literal(MARKER_TEXT).withStyle(Style.EMPTY.withFont(MARKER_FONT));
    }

    /** Whether the line is still opaque enough to draw icons for (chat fades out over 200 ticks). */
    public static boolean isVisible(int ticks, int addedTime)
    {
        return alpha(ticks, addedTime) > 0.05F;
    }

    /** Re-implements {@code ChatComponent.AlphaCalculator#timeBased} for the background pass. */
    public static float alpha(int ticks, int addedTime)
    {
        double remaining = 1.0 - (ticks - addedTime) / 200.0;
        double clamped = Math.max(0.0, Math.min(1.0, remaining * 10.0));
        return (float) (clamped * clamped);
    }

    /**
     * Draws the icon of every marker glyph in a laid-out chat line.
     *
     * <p>The line is already wrapped and left-aligned at x=0, so a marker's position is simply the
     * width of everything before it. The marker itself is measured through the same
     * {@link Font#width(FormattedCharSequence)} used by the renderer, which keeps the icon exactly
     * on the reserved glyph.
     *
     * @param localMouseX mouse x in chat coordinates, for the hover tooltip
     * @param localMouseY mouse y in chat coordinates, for the hover tooltip
     * @param foreground  whether chat is open; only then is a tooltip allowed
     */
    public static void renderLine(GuiGraphicsExtractor graphics, Font font, int textTop, FormattedCharSequence line,
                                  float localMouseX, float localMouseY, boolean foreground,
                                  int screenMouseX, int screenMouseY)
    {
        List<Integer> codepoints = new ArrayList<>();
        List<Style> styles = new ArrayList<>();

        line.accept((index, style, codepoint) ->
        {
            codepoints.add(codepoint);
            styles.add(style);
            return true;
        });

        if (!codepoints.contains(MARKER))
        {
            return;
        }

        List<FormattedCharSequence> prefix = new ArrayList<>(codepoints.size());

        for (int i = 0; i < codepoints.size(); i++)
        {
            int codepoint = codepoints.get(i);
            Style style = styles.get(i);

            if (codepoint == MARKER)
            {
                int x = font.width(FormattedCharSequence.fromList(prefix));
                ItemStack stack = itemOf(style);

                if (stack != null)
                {
                    drawIcon(graphics, stack, x, textTop + ICON_Y_OFFSET);

                    if (foreground
                            && localMouseX >= x && localMouseX < x + ICON_SIZE
                            && localMouseY >= textTop + ICON_Y_OFFSET
                            && localMouseY < textTop + ICON_Y_OFFSET + ICON_SIZE)
                    {
                        graphics.setTooltipForNextFrame(font, stack, screenMouseX, screenMouseY);
                    }
                }
            }

            prefix.add(FormattedCharSequence.codepoint(codepoint, style));
        }
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y)
    {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.item(stack, 0, 0);
        graphics.pose().popMatrix();
    }

    private static ItemStack itemOf(Style style)
    {
        return style.getHoverEvent() instanceof HoverEvent.ShowItem showItem
                ? showItem.item().create()
                : null;
    }

    private static boolean hasItemToken(Component component)
    {
        if (isItemToken(component))
        {
            return true;
        }

        for (Component sibling : component.getSiblings())
        {
            if (hasItemToken(sibling))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isItemToken(Component component)
    {
        // The hover is what carries the item; the brackets are the shape the server sends.
        if (!(component.getStyle().getHoverEvent() instanceof HoverEvent.ShowItem))
        {
            return false;
        }

        String text = component.getString();
        return text.length() >= 2 && text.charAt(0) == '[' && text.charAt(text.length() - 1) == ']';
    }

    private static Component rewrite(Component component)
    {
        if (isItemToken(component))
        {
            // Keep the hover and the colour; adding the marker font is what turns the token into
            // a 16px-wide invisible glyph.
            return markerGlyph().copy().withStyle(component.getStyle());
        }

        if (component.getSiblings().isEmpty())
        {
            return component;
        }

        MutableComponent copy = MutableComponent.create(component.getContents()).setStyle(component.getStyle());

        for (Component sibling : component.getSiblings())
        {
            copy.append(rewrite(sibling));
        }

        return copy;
    }
}

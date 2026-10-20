package lifescicraft.carpetlsc.mixins.client;

import lifescicraft.carpetlsc.client.ChatItemIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Paints the item icons of chat lines that carry a marker glyph.
 *
 * <p>The hook sits at the tail of {@code ChatComponent#extractRenderState}, once all chat text has
 * been recorded. Doing it afterwards means an icon is never covered by the line below it; the
 * price is that a 16px icon on a 9px line overlaps the neighbouring lines, the same trade-off the
 * other chat item mods make.
 *
 * <p>The line geometry is recalculated here from the same options
 * ({@code chatScale}, {@code chatWidth}, {@code chatLineSpacing}) and the same constants the chat
 * itself uses, because the per-line draw call is private and only receives screen-space values.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentItemIconMixin
{
    /** Height of one vanilla chat text line. */
    @Unique
    private static final int CARPET_LSC_LINE_HEIGHT = 9;

    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract int getLinesPerPage();

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
            at = @At("TAIL"))
    private void carpetLsc$drawItemIcons(GuiGraphicsExtractor graphics, Font font, int ticks, int mouseX, int mouseY,
                                         ChatComponent.DisplayMode mode, boolean changeCursor, CallbackInfo ci)
    {
        if (this.trimmedMessages.isEmpty()
                || this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN)
        {
            return;
        }

        float scale = this.minecraft.options.chatScale().get().floatValue();

        if (scale <= 0.0F)
        {
            return;
        }

        int chatBottom = Mth.floor((graphics.guiHeight() - 40) / scale);
        double lineSpacing = this.minecraft.options.chatLineSpacing().get();
        int entryHeight = (int) (CARPET_LSC_LINE_HEIGHT * (lineSpacing + 1.0));
        int entryBottomToMessageY = (int) Math.round(8.0 * (lineSpacing + 1.0) - 4.0 * lineSpacing);

        // The chat pose is chatScale + a 4px indent, so screen coordinates map back to chat
        // coordinates by inverting those two steps.
        float localMouseX = mouseX / scale - 4.0F;
        float localMouseY = mouseY / scale;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(4.0F, 0.0F);

        int total = this.trimmedMessages.size();
        int start = Math.min(total - this.chatScrollbarPos, this.getLinesPerPage()) - 1;

        for (int i = start; i >= 0; i--)
        {
            int lineIndex = i + this.chatScrollbarPos;

            if (lineIndex < 0 || lineIndex >= total)
            {
                continue;
            }

            GuiMessage.Line line = this.trimmedMessages.get(lineIndex);

            if (!mode.foreground && !ChatItemIcons.isVisible(ticks, line.addedTime()))
            {
                continue;
            }

            int textTop = chatBottom - i * entryHeight - entryBottomToMessageY;

            ChatItemIcons.renderLine(graphics, font, textTop, line.content(), localMouseX, localMouseY,
                    mode.foreground, mouseX, mouseY);
        }

        graphics.pose().popMatrix();
    }
}

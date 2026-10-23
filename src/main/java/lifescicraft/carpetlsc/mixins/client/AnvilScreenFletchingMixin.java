package lifescicraft.carpetlsc.mixins.client;

import lifescicraft.carpetlsc.helpers.FletchingTable;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the anvil's rename box on the fletching screen.
 *
 * <p>Our screen is the vanilla anvil, so the client cannot tell the two apart by menu type. The title
 * is the marker: we open it with {@code carpet-lsc-addition.fletching.title}, and comparing the
 * translation key (not the rendered text) means a renamed item containing the same words cannot
 * trigger this branch.
 */
@Mixin(AnvilScreen.class)
public abstract class AnvilScreenFletchingMixin
{
    @Shadow
    private EditBox name;

    @Inject(method = "subInit", at = @At("TAIL"))
    private void carpetLsc$hideRenameBox(CallbackInfo ci)
    {
        if (((AbstractContainerScreen<?>) (Object) this).getTitle().equals(FletchingTable.title()))
        {
            name.setVisible(false);
            name.setEditable(false);
        }
    }
}

package lifescicraft.carpetlsc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Removes the client-side "Confirm Command Execution" prompt for privileged commands.
 *
 * <h2>How the prompt is decided</h2>
 *
 * <p>Purely on the client. {@code ClientPacketListener#verifyCommand} parses the typed command
 * twice — once as the player, once as a {@code restrictedSuggestionsProvider} (permission level
 * {@code 0}). If the second parse fails while the first succeeds the result is
 * {@code PERMISSIONS_REQUIRED}, and the client opens a confirmation screen
 * ({@code multiplayer.confirm_command.permissions_required}) instead of sending the command.
 *
 * <p>The client only attaches a Brigadier {@code requires} predicate to a node when the server set
 * the restricted flag: {@code ClientPacketListener$1#configure(builder, executable, restricted)}
 * does {@code builder.requires(Commands.hasPermission(...))} for restricted nodes, and the flag
 * itself comes from {@code ClientboundCommandsPacket.createEntry}, which reads
 * {@code NodeInspector#isRestricted}. That method reports "the command's requirement fails for a
 * source with no permissions at all", i.e. it marks exactly the commands that need elevated
 * permissions — the ones the dialog is about.
 *
 * <p>Reporting {@code false} there means the flag is never set, so the client parses the command
 * normally, decides {@code NO_ISSUES} and sends it at once. Permissions themselves are untouched:
 * a player who lacks the level still gets the server's ordinary rejection instead of a dialog.
 *
 * <p>{@code createEntry} is hooked rather than the packet's {@code write}, because building the
 * packet happens on join while encoding only happens once it is actually flushed.
 */
@Mixin(ClientboundCommandsPacket.class)
public class CommandEntryRestrictedMixin
{
    @ModifyExpressionValue(
            method = "createEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket$NodeInspector;isRestricted(Lcom/mojang/brigadier/tree/CommandNode;)Z"))
    private static boolean carpetLsc$skipRestrictedFlag(boolean original)
    {
        return CarpetLscSettings.SkipCommandConfirm ? false : original;
    }
}

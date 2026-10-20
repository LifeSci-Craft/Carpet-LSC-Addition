package lifescicraft.carpetlsc;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.mojang.brigadier.CommandDispatcher;
import lifescicraft.carpetlsc.commands.McdrBridgeCommand;
import lifescicraft.carpetlsc.commands.PingCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.Map;

/**
 * The Carpet extension itself.
 *
 * <p>Kept in its own class so {@link CarpetLscAddition} can stay free of Carpet classes: the
 * mod jar is also used as a client-only add-on for the chat item icons, and such a client does
 * not necessarily have fabric-carpet installed.
 */
final class CarpetLscExtension implements CarpetExtension
{
    private static final CarpetLscExtension INSTANCE = new CarpetLscExtension();

    private CarpetLscExtension()
    {
    }

    static void register()
    {
        CarpetServer.manageExtension(INSTANCE);
    }

    @Override
    public String version()
    {
        return CarpetLscAddition.MOD_ID;
    }

    @Override
    public void onGameStarted()
    {
        CarpetServer.settingsManager.parseSettingsClass(CarpetLscSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext)
    {
        PingCommand.register(dispatcher);
        McdrBridgeCommand.register(dispatcher);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang)
    {
        return LscTranslations.get(lang);
    }
}

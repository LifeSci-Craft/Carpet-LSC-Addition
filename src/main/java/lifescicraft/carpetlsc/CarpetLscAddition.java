package lifescicraft.carpetlsc;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.mojang.brigadier.CommandDispatcher;
import lifescicraft.carpetlsc.commands.PingCommand;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.Map;

/**
 * Entry point of the mod.
 *
 * <p>Registers this mod as a Carpet extension and hooks the extra rules into Carpet's
 * own settings manager, so they are all controlled through {@code /carpet <rule> <value>}.
 */
public class CarpetLscAddition implements CarpetExtension, ModInitializer
{
    public static final String MOD_ID = "carpet-lsc-addition";

    private static final CarpetLscAddition INSTANCE = new CarpetLscAddition();

    @Override
    public void onInitialize()
    {
        CarpetServer.manageExtension(INSTANCE);
    }

    @Override
    public String version()
    {
        return MOD_ID;
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
    }

    @Override
    public Map<String, String> canHasTranslations(String lang)
    {
        return LscTranslations.get(lang);
    }
}

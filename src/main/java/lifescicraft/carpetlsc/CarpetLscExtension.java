package lifescicraft.carpetlsc;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import com.mojang.brigadier.CommandDispatcher;
import lifescicraft.carpetlsc.commands.McdrBridgeCommand;
import lifescicraft.carpetlsc.commands.LscCommand;
import lifescicraft.carpetlsc.commands.PingCommand;
import lifescicraft.carpetlsc.commands.WaypointCommand;
import lifescicraft.carpetlsc.helpers.RecipeUnlocker;
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

    /** Rule name as Carpet knows it; matches the field in {@link CarpetLscSettings}. */
    private static final String ALWAYS_UNLOCKED_RECIPES = "AlwaysUnlockedRecipes";

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

        // Turning the rule on unlocks everything for whoever is already connected, not just the
        // next player to join.
        SettingsManager.registerGlobalRuleObserver((source, rule, newValue) -> {
            if (ALWAYS_UNLOCKED_RECIPES.equals(rule.name()) && Boolean.parseBoolean(newValue))
            {
                RecipeUnlocker.applyToOnline();
            }
        });
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext)
    {
        PingCommand.register(dispatcher);
        McdrBridgeCommand.register(dispatcher);
        WaypointCommand.register(dispatcher);
        LscCommand.register(dispatcher);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang)
    {
        return LscTranslations.get(lang);
    }
}

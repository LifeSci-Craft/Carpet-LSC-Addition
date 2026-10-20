package lifescicraft.carpetlsc;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Entry point of the mod.
 *
 * <p>On a server the mod registers itself as a Carpet extension, so every rule lives in
 * Carpet's own settings manager and is controlled through {@code /carpet <rule> <value>}.
 *
 * <p>Carpet is only loaded through {@link CarpetLscExtension}, which is touched solely when
 * Carpet is present. That keeps the jar installable on a client that only wants the chat item
 * icons: without Carpet the extension stays inactive and nothing else changes.
 */
public class CarpetLscAddition implements ModInitializer
{
    public static final String MOD_ID = "carpet-lsc-addition";

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        if (!FabricLoader.getInstance().isModLoaded("carpet"))
        {
            LOGGER.info("fabric-carpet is not installed; the carpet extension stays inactive");
            return;
        }

        CarpetLscExtension.register();
    }
}

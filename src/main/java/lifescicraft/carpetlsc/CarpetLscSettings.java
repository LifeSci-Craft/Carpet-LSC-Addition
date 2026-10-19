package lifescicraft.carpetlsc;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.COMMAND;
import static carpet.api.settings.RuleCategory.FEATURE;
import static carpet.api.settings.RuleCategory.SURVIVAL;

/**
 * Rules added by this extension.
 *
 * <p>Descriptions are not part of the annotation; they come from the language files
 * under {@code assets/carpet-lsc-addition/lang/} using the {@code carpet.rule.<name>.desc} keys.
 */
public class CarpetLscSettings
{
    /** Custom category used to group every rule of this extension in the {@code /carpet} menu. */
    public static final String LSC = "lsc";

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean totemWrench = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean inventoryTotem = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean accurateBlockPlacement = false;

    @Rule(categories = {LSC, COMMAND, FEATURE})
    public static boolean commandPing = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean fastLeafDecay = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean emeraldTemptsVillagers = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean daytimeSleep = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean quickPaintingSwap = false;

    @Rule(categories = {LSC, COMMAND, FEATURE})
    public static boolean mcdrCommandBridge = false;

    @Rule(categories = {LSC, FEATURE})
    public static boolean chatHeldItem = false;

    @Rule(categories = {LSC, COMMAND, FEATURE})
    public static boolean skipCommandConfirm = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean rightClickHarvest = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean noFarmlandTrample = false;

    @Rule(categories = {LSC, FEATURE}, options = {"off", "self", "broadcast"})
    public static String deathCoordinates = "off";

    @Rule(categories = {LSC, FEATURE})
    public static boolean afkMarker = false;
}

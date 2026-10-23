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
    public static boolean TotemWrench = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean InventoryTotem = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean AccurateBlockPlacement = false;

    @Rule(categories = {LSC, COMMAND, FEATURE})
    public static boolean CommandPing = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean FastLeafDecay = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean EmeraldTemptsVillagers = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean DaytimeSleep = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean QuickPaintingSwap = false;

    @Rule(categories = {LSC, COMMAND, FEATURE})
    public static boolean McdrCommandBridge = false;

    @Rule(categories = {LSC, FEATURE})
    public static boolean ChatHeldItem = false;

    @Rule(categories = {LSC, COMMAND, FEATURE})
    public static boolean SkipCommandConfirm = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean RightClickHarvest = false;

    /** {@code off}, or the pick order for silent eating from a bundle. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"off", "saturation", "slot"})
    public static String BundleAutoEat = "off";

    /** {@code true} means the default 8 ticks; a bare number sets the cap directly. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"false", "true", "4", "8", "16"})
    public static String FastEating = "false";

    /** {@code true} never tramples; a number allows that many tramples of one block before it breaks. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"false", "true", "1", "3", "5", "10"})
    public static String NoFarmlandTrample = "false";

    /** Who the trample rule protects; only meaningful while {@code NoFarmlandTrample} is on. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"players", "all"})
    public static String NoFarmlandTrampleScope = "players";

    @Rule(categories = {LSC, FEATURE}, options = {"off", "self", "broadcast"})
    public static String DeathCoordinates = "off";

    @Rule(categories = {LSC, FEATURE})
    public static boolean AfkMarker = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean AlwaysUnlockedRecipes = false;

    @Rule(categories = {LSC, SURVIVAL, FEATURE})
    public static boolean VillagerBackpack = false;

    /** Firework rockets shove the boat or minecart the player rides; the number is the strength. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"off", "1", "2", "3"})
    public static String VehicleFireworkBoost = "off";

    /** Stops a vehicle when its rider gets off: {@code all}, {@code minecart} or {@code boat}. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"off", "all", "minecart", "boat"})
    public static String VehicleInstantStop = "off";

    /** The same firework boost, for the happy ghast only. */
    @Rule(categories = {LSC, SURVIVAL, FEATURE}, options = {"off", "1", "2", "3"})
    public static String HappyGhastBoost = "off";
}

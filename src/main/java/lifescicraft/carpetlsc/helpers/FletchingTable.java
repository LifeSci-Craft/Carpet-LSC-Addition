package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Turns the fletching table into a small crafting bench for arrows (rule {@code FletchingTable}).
 *
 * <p>The screen is the vanilla anvil, reused as-is so no client-only mod API is needed: the title is
 * passed when the menu opens, and the two input slots plus the result are taken over by our mixins.
 * The inputs only accept arrows and what vanilla arrow recipes consume, and the result comes from
 * the ordinary {@code CraftingRecipe} lookup, so every vanilla arrow recipe works.
 */
public final class FletchingTable
{
    /** Title key; the client uses it to recognise our re-use of the anvil screen. */
    public static final String TITLE_KEY = "carpet-lsc-addition.fletching.title";

    /** Set while our own anvil menu is being constructed, so the mixins know that menu is ours. */
    private static final ThreadLocal<Boolean> OURS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Level captured while our menu is built, because the menu cannot look it up later. */
    private static final ThreadLocal<Level> OPENING_LEVEL = new ThreadLocal<>();

    private FletchingTable()
    {
    }

    public static Component title()
    {
        return Component.translatable(TITLE_KEY);
    }

    public static void mark()
    {
        OURS.set(Boolean.TRUE);
    }

    public static void unmark()
    {
        OURS.remove();
    }

    public static Level openingLevel()
    {
        return OPENING_LEVEL.get();
    }

    public static boolean isOurs()
    {
        return OURS.get();
    }

    public static boolean tryOpen(ServerPlayer player, ContainerLevelAccess access)
    {
        if (!CarpetLscSettings.FletchingTable)
        {
            return false;
        }

        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> {
            mark();
            OPENING_LEVEL.set(player.level());

            try
            {
                // No container-level access on purpose: vanilla would keep checking for an anvil
                // near the player and close the screen on the very next tick.
                return new AnvilMenu(id, inventory);
            }
            finally
            {
                unmark();
                OPENING_LEVEL.remove();
            }
        }, title()));

        return true;
    }

    /** Arrows and everything a vanilla arrow recipe consumes. */
    public static boolean isArrowMaterial(ItemStack stack)
    {
        return stack.is(Items.ARROW)
                || stack.is(Items.TIPPED_ARROW)
                || stack.is(Items.SPECTRAL_ARROW)
                || stack.is(Items.POTION)
                || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.FEATHER)
                || stack.is(Items.FLINT)
                || stack.is(Items.STICK);
    }

    /**
     * Runs the vanilla recipe lookup over a 3x3 grid whose first two cells are our inputs.
     *
     * @return the crafted stack, or empty when the pair is not a recipe.
     */
    public static ItemStack craft(Level level, ItemStack first, ItemStack second)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return ItemStack.EMPTY;
        }

        // Eight arrows and one lingering potion make eight tipped arrows, whichever slot each one
        // landed in. Vanilla keeps that recipe as a special 3x3 ring, which two slots cannot
        // express, so the pair is recognised here and the arrows are built the same way vanilla does.
        ItemStack tipped = tippedArrows(first, second);

        if (!tipped.isEmpty())
        {
            return tipped;
        }

        ItemStack result = lookup(serverLevel, level, ring(first, second));

        return result.isEmpty() ? lookup(serverLevel, level, simple(first, second)) : result;
    }

    private static ItemStack tippedArrows(ItemStack first, ItemStack second)
    {
        ItemStack potion = first.is(Items.LINGERING_POTION) ? first
                : second.is(Items.LINGERING_POTION) ? second : ItemStack.EMPTY;

        if (potion.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        ItemStack arrows = first == potion ? second : first;

        if (!arrows.is(Items.ARROW) || arrows.getCount() < 8)
        {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(Items.TIPPED_ARROW, 8);
        result.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                potion.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS));
        return result;
    }

    private static ItemStack lookup(ServerLevel serverLevel, Level level, List<ItemStack> gridItems)
    {
        CraftingInput input = CraftingInput.of(3, 3, gridItems);

        return serverLevel.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(holder -> holder.value().assemble(input))
                .orElse(ItemStack.EMPTY);
    }

    /** Second input in the centre, first input in every other cell. */
    private static List<ItemStack> ring(ItemStack first, ItemStack second)
    {
        java.util.List<ItemStack> grid = new java.util.ArrayList<>(9);

        for (int cell = 0; cell < 9; cell++)
        {
            grid.add(cell == 4 ? second : first);
        }

        return grid;
    }

    /** The plain layout: the two inputs in the first two cells. */
    private static List<ItemStack> simple(ItemStack first, ItemStack second)
    {
        return grid(first, second);
    }

    /** Nine cells: our two inputs first, the rest empty. */
    public static List<ItemStack> grid(ItemStack first, ItemStack second)
    {
        return List.of(first, second,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY);
    }
}

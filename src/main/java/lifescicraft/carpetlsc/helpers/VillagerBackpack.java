package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens a chest-like view of a named villager's inventory (rule {@code VillagerBackpack}).
 *
 * <p>The screen is a vanilla large chest menu, so a client without this mod still sees it. Two
 * locked icons label the columns, the middle column carries the villager's profession, level and
 * price situation, the left block is the villager's own eight slots and the right block lists what
 * it currently sells. The barrier at the bottom is a close button.
 */
public final class VillagerBackpack
{
    private static final int SIZE = 54;

    private static final int ICON_BACKPACK = 1;
    private static final int ICON_TRADES = 7;

    private static final int ICON_PROFESSION = 13;
    private static final int ICON_LEVEL = 22;
    private static final int ICON_DISCOUNT = 31;
    private static final int ICON_CLOSE = 49;

    /** Left block: two columns of four, right under the backpack icon. */
    private static final int[] VILLAGER_SLOTS = {10, 11, 19, 20, 28, 29, 37, 38};

    /** Right block: what the villager sells, display only. */
    private static final int[] TRADE_SLOTS = {16, 17, 25, 26, 34, 35, 43, 44};

    private static final double MAX_DISTANCE_SQR = 64.0;

    private VillagerBackpack()
    {
    }

    public static boolean tryOpen(ServerPlayer player, Villager villager)
    {
        if (!CarpetLscSettings.VillagerBackpack || !player.isShiftKeyDown() || !villager.hasCustomName())
        {
            return false;
        }

        player.openMenu(new BackpackMenuProvider(villager));
        return true;
    }

    private static boolean isVillagerSlot(int slot)
    {
        for (int candidate : VILLAGER_SLOTS)
        {
            if (candidate == slot)
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isTradeSlot(int slot)
    {
        for (int candidate : TRADE_SLOTS)
        {
            if (candidate == slot)
            {
                return true;
            }
        }

        return false;
    }

    /** The data source behind the menu. */
    static final class BackpackContainer implements Container
    {
        private final Villager villager;

        BackpackContainer(Villager villager)
        {
            this.villager = villager;
        }

        @Override
        public int getContainerSize()
        {
            return SIZE;
        }

        @Override
        public boolean isEmpty()
        {
            for (int i = 0; i < VILLAGER_SLOTS.length; i++)
            {
                if (!getItem(VILLAGER_SLOTS[i]).isEmpty())
                {
                    return false;
                }
            }

            return true;
        }

        private int villagerIndex(int slot)
        {
            for (int i = 0; i < VILLAGER_SLOTS.length; i++)
            {
                if (VILLAGER_SLOTS[i] == slot)
                {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public ItemStack getItem(int slot)
        {
            int index = villagerIndex(slot);

            if (index >= 0)
            {
                return villager.getInventory().getItem(index);
            }

            return switch (slot)
            {
                case ICON_BACKPACK -> labelled(new ItemStack(Items.CHEST),
                        "carpet-lsc-addition.villager.backpack", ChatFormatting.GOLD);
                case ICON_TRADES -> labelled(new ItemStack(Items.ENDER_CHEST),
                        "carpet-lsc-addition.villager.trades", ChatFormatting.LIGHT_PURPLE);
                case ICON_PROFESSION -> professionIcon();
                case ICON_LEVEL -> levelIcon();
                case ICON_DISCOUNT -> discountIcon();
                case ICON_CLOSE -> labelled(new ItemStack(Items.BARRIER),
                        "carpet-lsc-addition.villager.close", ChatFormatting.RED);
                default -> tradeOrSeparator(slot);
            };
        }

        private ItemStack tradeOrSeparator(int slot)
        {
            if (!isTradeSlot(slot))
            {
                return separator();
            }

            int index = indexOf(TRADE_SLOTS, slot);
            List<MerchantOffer> offers = villager.getOffers();

            return index < offers.size() ? offers.get(index).getResult().copy() : separator();
        }

        private static int indexOf(int[] slots, int slot)
        {
            for (int i = 0; i < slots.length; i++)
            {
                if (slots[i] == slot)
                {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public ItemStack removeItem(int slot, int amount)
        {
            int index = villagerIndex(slot);
            return index >= 0 ? villager.getInventory().removeItem(index, amount) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot)
        {
            int index = villagerIndex(slot);
            return index >= 0 ? villager.getInventory().removeItemNoUpdate(index) : ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack)
        {
            int index = villagerIndex(slot);

            if (index >= 0)
            {
                villager.getInventory().setItem(index, stack);
            }
        }

        @Override
        public int getMaxStackSize()
        {
            return 64;
        }

        @Override
        public void setChanged()
        {
            villager.getInventory().setChanged();
        }

        @Override
        public boolean stillValid(Player player)
        {
            return !villager.isRemoved() && villager.distanceToSqr(player) <= MAX_DISTANCE_SQR;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack)
        {
            return isVillagerSlot(slot) && villager.wantsToPickUp((ServerLevel) villager.level(), stack);
        }

        @Override
        public boolean canTakeItem(Container target, int slot, ItemStack stack)
        {
            return isVillagerSlot(slot);
        }

        /** Never wipe the villager's belongings when the menu closes. */
        @Override
        public void clearContent()
        {
        }

        private ItemStack professionIcon()
        {
            VillagerData data = villager.getVillagerData();
            ItemStack stack = new ItemStack(professionBlock(data.profession()));

            return bold(stack, data.profession().value().name(), ChatFormatting.GOLD);
        }

        private ItemStack levelIcon()
        {
            int level = villager.getVillagerData().level();

            return bold(new ItemStack(Items.EXPERIENCE_BOTTLE),
                    Component.translatable("carpet-lsc-addition.villager.level." + level),
                    ChatFormatting.AQUA);
        }

        /**
         * Totem when prices are below base, ominous bottle when this player is marked up, emerald
         * otherwise.
         */
        private ItemStack discountIcon()
        {
            PriceSituation situation = priceSituation();

            return switch (situation)
            {
                case DISCOUNT -> bold(new ItemStack(Items.TOTEM_OF_UNDYING),
                        Component.translatable("carpet-lsc-addition.villager.discount.yes"),
                        ChatFormatting.GREEN);
                case MARKUP -> bold(new ItemStack(Items.OMINOUS_BOTTLE),
                        Component.translatable("carpet-lsc-addition.villager.discount.markup"),
                        ChatFormatting.DARK_RED);
                case NONE -> bold(new ItemStack(Items.EMERALD),
                        Component.translatable("carpet-lsc-addition.villager.discount.no"),
                        ChatFormatting.GRAY);
            };
        }

        private PriceSituation priceSituation()
        {
            boolean discounted = false;
            boolean markedUp = false;

            for (MerchantOffer offer : villager.getOffers())
            {
                int base = offer.getBaseCostA().getCount();
                int now = offer.getCostA().getCount();

                discounted |= now < base;
                markedUp |= now > base;
            }

            if (discounted)
            {
                return PriceSituation.DISCOUNT;
            }

            return markedUp ? PriceSituation.MARKUP : PriceSituation.NONE;
        }

        /** Separator slots must not show a tooltip at all. */
        private static ItemStack separator()
        {
            ItemStack stack = new ItemStack(Items.STAINED_GLASS_PANE.gray());
            stack.set(DataComponents.TOOLTIP_DISPLAY,
                    new TooltipDisplay(true, new java.util.LinkedHashSet<>()));
            return stack;
        }

        private static ItemStack labelled(ItemStack stack, String key, ChatFormatting colour)
        {
            return bold(stack, Component.translatable(key), colour);
        }

        /** Bold name only: no extra lore, so no dark purple explanation line in the tooltip. */
        private static ItemStack bold(ItemStack stack, Component name, ChatFormatting colour)
        {
            stack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(Style.EMPTY
                    .withColor(colour)
                    .withBold(true)
                    .withItalic(false)));
            return stack;
        }

        private static Item professionBlock(net.minecraft.core.Holder<VillagerProfession> profession)
        {
            String path = profession.unwrapKey()
                    .map(key -> key.identifier().getPath())
                    .orElse("none");

            return switch (path)
            {
                case "farmer" -> Items.COMPOSTER;
                case "librarian" -> Items.LECTERN;
                case "armorer" -> Items.BLAST_FURNACE;
                case "butcher" -> Items.SMOKER;
                case "cartographer" -> Items.CARTOGRAPHY_TABLE;
                case "cleric" -> Items.BREWING_STAND;
                case "fletcher" -> Items.FLETCHING_TABLE;
                case "leatherworker" -> Items.CAULDRON;
                case "mason" -> Items.STONECUTTER;
                case "shepherd" -> Items.LOOM;
                case "toolsmith" -> Items.SMITHING_TABLE;
                case "weaponsmith" -> Items.GRINDSTONE;
                default -> Items.BARRIER;
            };
        }
    }

    private enum PriceSituation
    {
        DISCOUNT,
        MARKUP,
        NONE
    }

    private record BackpackMenuProvider(Villager villager) implements net.minecraft.world.MenuProvider
    {
        @Override
        public Component getDisplayName()
        {
            return Component.translatable("carpet-lsc-addition.villager.title", villager.getDisplayName());
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player)
        {
            return new BackpackMenu(id, playerInventory, new BackpackContainer(villager));
        }
    }

    /**
     * A chest menu whose decorative slots are read-only and whose barrier closes the screen.
     *
     * <p>The slot layout stays the one {@link ChestMenu} builds, because the client reconstructs
     * exactly that from {@code MenuType.GENERIC_9x6}; only slot behaviour is replaced.
     */
    static final class BackpackMenu extends ChestMenu
    {
        BackpackMenu(int id, Inventory playerInventory, BackpackContainer container)
        {
            super(MenuType.GENERIC_9x6, id, playerInventory, container, 6);
            lockDecorativeSlots();
        }

        private void lockDecorativeSlots()
        {
            List<Slot> vanilla = new ArrayList<>(slots);
            List<Slot> rebuilt = new ArrayList<>(vanilla.size());

            for (Slot slot : vanilla)
            {
                rebuilt.add(slot.index < SIZE && !isVillagerSlot(slot.index)
                        ? new LockedSlot(slot.container, slot.index, slot.x, slot.y)
                        : slot);
            }

            slots.clear();
            slots.addAll(rebuilt);
        }

        @Override
        public void clicked(int slotId, int button, ContainerInput input, Player player)
        {
            if (slotId == ICON_CLOSE)
            {
                if (player instanceof ServerPlayer serverPlayer)
                {
                    serverPlayer.closeContainer();
                }

                return;
            }

            super.clicked(slotId, button, input, player);
        }

        private static final class LockedSlot extends Slot
        {
            LockedSlot(Container container, int index, int x, int y)
            {
                super(container, index, x, y);
            }

            @Override
            public boolean mayPickup(Player player)
            {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return false;
            }
        }
    }
}

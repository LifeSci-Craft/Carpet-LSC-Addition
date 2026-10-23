package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
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
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Opens a chest-like view of a named villager's inventory (rule {@code VillagerBackpack}).
 *
 * <p>Two locked icons label the columns, the middle column carries the villager's profession, level
 * and price situation, the left block is the villager's own eight slots and the right block lists
 * what it currently sells, eight per page. The barrier closes the screen.
 *
 * <p>The villager is put on {@code setNoAi} while the screen is open so it cannot wander out of
 * range and close the menu, and gets its AI back when the screen closes.
 */
public final class VillagerBackpack
{
    private static final int SIZE = 54;

    private static final int ICON_BACKPACK = 1;
    private static final int ICON_TRADES = 7;

    private static final int ICON_PROFESSION = 13;
    private static final int ICON_LEVEL = 22;
    private static final int ICON_DISCOUNT = 31;

    private static final int ICON_PREVIOUS = 48;
    private static final int ICON_CLOSE = 49;
    private static final int ICON_NEXT = 50;

    private static final int[] VILLAGER_SLOTS = {10, 11, 19, 20, 28, 29, 37, 38};
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

        // Stay put while the player is shopping, so the menu does not close by itself.
        villager.setNoAi(true);
        player.openMenu(new BackpackMenuProvider(villager));
        return true;
    }

    private static boolean contains(int[] slots, int slot)
    {
        for (int candidate : slots)
        {
            if (candidate == slot)
            {
                return true;
            }
        }

        return false;
    }

    static final class BackpackContainer implements Container
    {
        private final Villager villager;

        /** Offers with the same result are merged, so one item is listed once. */
        private final List<ItemStack> trades;

        private int page;

        BackpackContainer(Villager villager)
        {
            this.villager = villager;
            this.trades = collectTrades(villager);
        }

        private List<ItemStack> collectTrades(Villager source)
        {
            List<ItemStack> merged = new ArrayList<>();

            for (MerchantOffer offer : source.getOffers())
            {
                ItemStack result = offer.getResult();
                boolean seen = false;

                for (ItemStack existing : merged)
                {
                    if (ItemStack.isSameItemSameComponents(existing, result))
                    {
                        existing.grow(result.getCount());
                        seen = true;
                        break;
                    }
                }

                if (!seen)
                {
                    merged.add(result.copy());
                }
            }

            return merged;
        }

        private int pageCount()
        {
            return Math.max(1, (trades.size() + TRADE_SLOTS.length - 1) / TRADE_SLOTS.length);
        }

        private void turnPage(int delta)
        {
            page = Math.max(0, Math.min(pageCount() - 1, page + delta));
        }

        @Override
        public int getContainerSize()
        {
            return SIZE;
        }

        @Override
        public boolean isEmpty()
        {
            for (int slot : VILLAGER_SLOTS)
            {
                if (!getItem(slot).isEmpty())
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
                case ICON_BACKPACK -> bold(new ItemStack(Items.CHEST),
                        Component.translatable("carpet-lsc-addition.villager.backpack"),
                        ChatFormatting.GOLD);
                case ICON_TRADES -> bold(new ItemStack(Items.ENDER_CHEST),
                        Component.translatable("carpet-lsc-addition.villager.trades"),
                        ChatFormatting.LIGHT_PURPLE);
                case ICON_PROFESSION -> professionIcon();
                case ICON_LEVEL -> levelIcon();
                case ICON_DISCOUNT -> discountIcon();
                case ICON_CLOSE -> bold(new ItemStack(Items.BARRIER),
                        Component.translatable("carpet-lsc-addition.villager.close"),
                        ChatFormatting.RED);
                case ICON_PREVIOUS -> page > 0
                        ? bold(new ItemStack(Items.ARROW),
                                Component.translatable("carpet-lsc-addition.villager.previousPage"),
                                ChatFormatting.WHITE)
                        : separator();
                case ICON_NEXT -> page < pageCount() - 1
                        ? bold(new ItemStack(Items.ARROW),
                                Component.translatable("carpet-lsc-addition.villager.nextPage"),
                                ChatFormatting.WHITE)
                        : separator();
                default -> tradeOrSeparator(slot);
            };
        }

        private ItemStack tradeOrSeparator(int slot)
        {
            if (!contains(TRADE_SLOTS, slot))
            {
                return separator();
            }

            int index = page * TRADE_SLOTS.length + indexOf(TRADE_SLOTS, slot);

            return index < trades.size() ? trades.get(index).copy() : separator();
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
            return contains(VILLAGER_SLOTS, slot)
                    && villager.wantsToPickUp((ServerLevel) villager.level(), stack);
        }

        @Override
        public boolean canTakeItem(Container target, int slot, ItemStack stack)
        {
            return contains(VILLAGER_SLOTS, slot);
        }

        @Override
        public void clearContent()
        {
        }

        private ItemStack professionIcon()
        {
            VillagerData data = villager.getVillagerData();

            return bold(new ItemStack(professionBlock(data.profession())),
                    data.profession().value().name(), ChatFormatting.GOLD);
        }

        private ItemStack levelIcon()
        {
            int level = villager.getVillagerData().level();

            return bold(new ItemStack(Items.EXPERIENCE_BOTTLE),
                    Component.translatable("carpet-lsc-addition.villager.level." + level),
                    ChatFormatting.AQUA);
        }

        private ItemStack discountIcon()
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
                return bold(new ItemStack(Items.TOTEM_OF_UNDYING),
                        Component.translatable("carpet-lsc-addition.villager.discount.yes"),
                        ChatFormatting.GREEN);
            }

            if (markedUp)
            {
                return bold(new ItemStack(Items.OMINOUS_BOTTLE),
                        Component.translatable("carpet-lsc-addition.villager.discount.markup"),
                        ChatFormatting.DARK_RED);
            }

            return bold(new ItemStack(Items.EMERALD),
                    Component.translatable("carpet-lsc-addition.villager.discount.no"),
                    ChatFormatting.GRAY);
        }

        private static ItemStack separator()
        {
            return bold(new ItemStack(Items.STAINED_GLASS_PANE.gray()),
                    Component.translatable("carpet-lsc-addition.villager.separator"),
                    ChatFormatting.BLACK);
        }

        private static ItemStack bold(ItemStack stack, Component name, ChatFormatting colour)
        {
            stack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(Style.EMPTY
                    .withColor(colour)
                    .withBold(true)
                    .withItalic(false)));
            stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>()));
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
            return new BackpackMenu(id, playerInventory, new BackpackContainer(villager), villager);
        }
    }

    /**
     * A chest menu whose decorative slots are read-only, whose arrow icons turn the trade page and
     * whose barrier closes the screen.
     */
    static final class BackpackMenu extends ChestMenu
    {
        private final BackpackContainer container;
        private final Villager villager;

        BackpackMenu(int id, Inventory playerInventory, BackpackContainer container, Villager villager)
        {
            super(MenuType.GENERIC_9x6, id, playerInventory, container, 6);
            this.container = container;
            this.villager = villager;
            rebuildSlots();
        }

        private void rebuildSlots()
        {
            List<Slot> vanilla = new ArrayList<>(slots);
            List<Slot> rebuilt = new ArrayList<>(vanilla.size());

            for (Slot slot : vanilla)
            {
                if (slot.index >= SIZE)
                {
                    rebuilt.add(slot);
                }
                else if (contains(VILLAGER_SLOTS, slot.index))
                {
                    rebuilt.add(new VillagerSlot(slot.container, slot.index, slot.x, slot.y));
                }
                else
                {
                    rebuilt.add(new LockedSlot(slot.container, slot.index, slot.x, slot.y));
                }
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

            if (slotId == ICON_NEXT || slotId == ICON_PREVIOUS)
            {
                container.turnPage(slotId == ICON_NEXT ? 1 : -1);
                broadcastChanges();
                return;
            }

            super.clicked(slotId, button, input, player);
        }

        @Override
        public void removed(Player player)
        {
            super.removed(player);

            if (!villager.isRemoved())
            {
                villager.setNoAi(false);
            }
        }

        /** The villager's own slots: take freely, put back only what it would pick up itself. */
        private static final class VillagerSlot extends Slot
        {
            VillagerSlot(Container container, int index, int x, int y)
            {
                super(container, index, x, y);
            }

            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return container.canPlaceItem(index, stack);
            }
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

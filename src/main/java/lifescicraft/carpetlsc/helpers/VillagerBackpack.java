package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens a chest-like view of a named villager's inventory (rule {@code VillagerBackpack}).
 *
 * <p>The screen is a plain vanilla chest menu, so a client without this mod still sees it. The first
 * row carries three read-only icons — profession, level and whether the trades are discounted — and
 * dark panes as separators; the second row is the villager's own eight slots.
 *
 * <p>Insertion is gated on {@code Villager#wantsToPickUp}, the same check vanilla uses when a villager
 * decides whether to walk over and pick something up, so only food and the other items that villager
 * would actually collect can go in.
 */
public final class VillagerBackpack
{
    static final int ROWS = 2;
    static final int SIZE = ROWS * 9;

    /** Villager inventory size; the second row maps onto it one to one. */
    static final int VILLAGER_SLOTS = 8;

    static final int SLOT_FIRST = 9;

    private static final int ICON_PROFESSION = 0;
    private static final int ICON_LEVEL = 1;
    private static final int ICON_DISCOUNT = 2;

    private static final double MAX_DISTANCE_SQR = 64.0;

    private VillagerBackpack()
    {
    }

    /**
     * @return true when the interaction was turned into an opened backpack.
     */
    public static boolean tryOpen(ServerPlayer player, Villager villager)
    {
        if (!CarpetLscSettings.VillagerBackpack || !player.isShiftKeyDown() || !villager.hasCustomName())
        {
            return false;
        }

        player.openMenu(new BackpackMenuProvider(villager));
        return true;
    }

    /** The data source behind the menu: icons for the first row, the villager inventory below. */
    static final class BackpackContainer implements Container
    {
        private final Villager villager;

        BackpackContainer(Villager villager)
        {
            this.villager = villager;
        }

        private SimpleContainer inventory()
        {
            return villager.getInventory();
        }

        @Override
        public int getContainerSize()
        {
            return SIZE;
        }

        @Override
        public boolean isEmpty()
        {
            for (int i = SLOT_FIRST; i < SLOT_FIRST + VILLAGER_SLOTS; i++)
            {
                if (!getItem(i).isEmpty())
                {
                    return false;
                }
            }

            return true;
        }

        @Override
        public ItemStack getItem(int slot)
        {
            return switch (slot)
            {
                case ICON_PROFESSION -> professionIcon();
                case ICON_LEVEL -> levelIcon();
                case ICON_DISCOUNT -> discountIcon();
                default -> isVillagerSlot(slot) ? inventory().getItem(slot - SLOT_FIRST) : separator();
            };
        }

        @Override
        public ItemStack removeItem(int slot, int amount)
        {
            return isVillagerSlot(slot) ? inventory().removeItem(slot - SLOT_FIRST, amount) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot)
        {
            return isVillagerSlot(slot) ? inventory().removeItemNoUpdate(slot - SLOT_FIRST) : ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack)
        {
            if (isVillagerSlot(slot))
            {
                inventory().setItem(slot - SLOT_FIRST, stack);
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
            inventory().setChanged();
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

        private static boolean isVillagerSlot(int slot)
        {
            return slot >= SLOT_FIRST && slot < SLOT_FIRST + VILLAGER_SLOTS;
        }

        private ItemStack professionIcon()
        {
            VillagerData data = villager.getVillagerData();
            ItemStack stack = new ItemStack(professionBlock(data.profession()));

            return labelled(stack,
                    data.profession().value().name().copy().withStyle(ChatFormatting.GOLD),
                    Component.translatable("carpet-lsc-addition.villager.profession.hover"));
        }

        private ItemStack levelIcon()
        {
            VillagerData data = villager.getVillagerData();
            int level = data.level();
            ItemStack stack = new ItemStack(Items.EXPERIENCE_BOTTLE);

            return labelled(stack,
                    Component.translatable("carpet-lsc-addition.villager.level." + level)
                            .withStyle(ChatFormatting.AQUA),
                    Component.translatable("carpet-lsc-addition.villager.level.hover"));
        }

        private ItemStack discountIcon()
        {
            boolean discounted = isDiscounted();
            ItemStack stack = new ItemStack(discounted ? Items.TOTEM_OF_UNDYING : Items.BARRIER);

            return labelled(stack,
                    Component.translatable(discounted
                                    ? "carpet-lsc-addition.villager.discount.yes"
                                    : "carpet-lsc-addition.villager.discount.no")
                            .withStyle(discounted ? ChatFormatting.GREEN : ChatFormatting.RED),
                    Component.translatable("carpet-lsc-addition.villager.discount.hover"));
        }

        /** A trade counts as discounted when it costs less than the villager's base price. */
        private boolean isDiscounted()
        {
            for (MerchantOffer offer : villager.getOffers())
            {
                if (offer.getCostA().getCount() < offer.getBaseCostA().getCount())
                {
                    return true;
                }
            }

            return false;
        }

        private static ItemStack separator()
        {
            return new ItemStack(Items.STAINED_GLASS_PANE.gray());
        }

        private static ItemStack labelled(ItemStack stack, Component name, Component lore)
        {
            stack.set(DataComponents.CUSTOM_NAME, name);
            stack.set(DataComponents.LORE, new ItemLore(List.of(lore)));
            return stack;
        }

        /** The workstation the profession is known for, used as the icon. */
        private static net.minecraft.world.item.Item professionBlock(
                net.minecraft.core.Holder<VillagerProfession> profession)
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

    /** Builds the menu; only the server ever calls this, the client rebuilds a plain chest menu. */
    private record BackpackMenuProvider(Villager villager) implements net.minecraft.world.MenuProvider
    {
        @Override
        public Component getDisplayName()
        {
            return villager.getDisplayName();
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player)
        {
            return new BackpackMenu(id, playerInventory, new BackpackContainer(villager));
        }
    }

    /**
     * A chest menu whose icon slots are read-only.
     *
     * <p>The slot layout is the one {@link ChestMenu} builds, because the client reconstructs exactly
     * that from {@code MenuType.GENERIC_9x2}; only the slot behaviour for the first row is replaced.
     */
    static final class BackpackMenu extends ChestMenu
    {
        BackpackMenu(int id, Inventory playerInventory, BackpackContainer container)
        {
            super(MenuType.GENERIC_9x2, id, playerInventory, container, ROWS);
            lockIcons();
        }

        private void lockIcons()
        {
            List<Slot> vanilla = new ArrayList<>(slots);
            List<Slot> rebuilt = new ArrayList<>(vanilla.size());

            for (Slot slot : vanilla)
            {
                rebuilt.add(slot.index < SIZE && !isVillagerSlot(slot.index)
                        ? new IconSlot(slot.container, slot.index, slot.x, slot.y)
                        : slot);
            }

            slots.clear();
            slots.addAll(rebuilt);
        }

        private static boolean isVillagerSlot(int index)
        {
            return index >= SLOT_FIRST && index < SLOT_FIRST + VILLAGER_SLOTS;
        }

        /** Read-only: cannot be picked up and cannot be replaced. */
        private static final class IconSlot extends Slot
        {
            IconSlot(Container container, int index, int x, int y)
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

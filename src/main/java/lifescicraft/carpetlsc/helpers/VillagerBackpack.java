package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

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

    private static final int ICON_PREVIOUS = 51;
    private static final int ICON_CLOSE = 49;
    private static final int ICON_NEXT = 52;

    private static final int[] VILLAGER_SLOTS = {10, 11, 19, 20, 28, 29, 37, 38};
    private static final int[] TRADE_SLOTS = {15, 16, 24, 25, 33, 34, 42, 43};

    private static final double MAX_DISTANCE_SQR = 64.0;

    /** What a vanilla villager walks over and picks up, from Villager's own food points table. */
    private static final java.util.Set<Item> COLLECTABLE = java.util.Set.of(
            Items.BREAD,
            Items.POTATO,
            Items.BAKED_POTATO,
            Items.CARROT,
            Items.BEETROOT,
            Items.WHEAT,
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS);

    private VillagerBackpack()
    {
    }

    public static boolean tryOpen(ServerPlayer player, Villager villager)
    {
        if (!CarpetLscSettings.VillagerBackpack || !player.isShiftKeyDown())
        {
            return false;
        }

        // A name tag always works; a villager boxed into a single block is also fair game, which is
        // what makes trading posts usable without naming every villager.
        if (!villager.hasCustomName() && !isConfined(villager))
        {
            return false;
        }

        // Stay put while the player is shopping, so the menu does not close by itself.
        freeze(villager);
        player.openMenu(new BackpackMenuProvider(villager));
        return true;
    }

    /**
     * Whether the villager is boxed into a single block: standing on something solid with all four
     * sides blocked at both foot and head height, so it has nowhere to walk.
     */
    private static boolean isConfined(Villager villager)
    {
        Level level = villager.level();
        BlockPos feet = villager.blockPosition();

        if (!blocked(level, feet.below()))
        {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (!blocked(level, feet.relative(direction)) || !blocked(level, feet.above().relative(direction)))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean blocked(Level level, BlockPos pos)
    {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    /** Villagers whose backpack screen is open; kept out of NBT so a crash cannot leave them stuck. */
    private static final java.util.Set<java.util.UUID> FROZEN = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static void freeze(Villager villager)
    {
        FROZEN.add(villager.getUUID());
    }

    private static void unfreeze(Villager villager)
    {
        FROZEN.remove(villager.getUUID());
    }

    /** Called from the villager's own tick: hold it in place without touching its AI state. */
    public static void tick(Villager villager)
    {
        if (!FROZEN.contains(villager.getUUID()))
        {
            return;
        }

        villager.getNavigation().stop();
        villager.setDeltaMovement(0.0, Math.min(villager.getDeltaMovement().y, 0.0), 0.0);
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
            this.trades = CarpetLscSettings.TradePreview
                    ? futureTrades(villager)
                    : collectTrades(villager);
        }

        /**
         * Every item the villager could ever sell, levels one through five.
         *
         * <p>Vanilla keeps trades in the datapack registry: the profession names a trade set per
         * level ({@code VillagerProfession#getTrades}), and every {@code VillagerTrade} in that set
         * turns into an offer through a loot context built the same way {@code updateTrades} builds
         * it.
         */
        private static List<ItemStack> futureTrades(Villager villager)
        {
            List<ItemStack> merged = new ArrayList<>();

            if (!(villager.level() instanceof ServerLevel serverLevel))
            {
                return merged;
            }

            Registry<TradeSet> registry = serverLevel.registryAccess().lookupOrThrow(Registries.TRADE_SET);
            VillagerData data = villager.getVillagerData();
            var profession = data.profession().value();

            for (int level = 1; level <= 5; level++)
            {
                ResourceKey<TradeSet> key = profession.getTrades(level);

                if (key == null)
                {
                    continue;
                }

                TradeSet set = registry.getOptional(key).orElse(null);

                if (set == null)
                {
                    continue;
                }

                LootContext context = new LootContext.Builder(new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, villager.position())
                        .withParameter(LootContextParams.THIS_ENTITY, villager)
                        .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
                        .create(LootContextParamSets.VILLAGER_TRADE))
                        .create(set.randomSequence());

                for (net.minecraft.core.Holder<VillagerTrade> trade : set.trades())
                {
                    MerchantOffer offer = trade.value().getOffer(context);

                    if (offer != null)
                    {
                        mergeInto(merged, offer.getResult());
                    }
                }
            }

            return merged;
        }

        private static List<ItemStack> collectTrades(Villager source)
        {
            List<ItemStack> merged = new ArrayList<>();

            for (MerchantOffer offer : source.getOffers())
            {
                mergeInto(merged, offer.getResult());
            }

            return merged;
        }

        /** Same item and same components show up once, with the counts added together. */
        private static void mergeInto(List<ItemStack> merged, ItemStack result)
        {
            for (ItemStack existing : merged)
            {
                if (ItemStack.isSameItemSameComponents(existing, result))
                {
                    existing.grow(result.getCount());
                    return;
                }
            }

            merged.add(result.copy());
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
                        Component.translatable(CarpetLscSettings.TradePreview
                                ? "carpet-lsc-addition.villager.tradesAll"
                                : "carpet-lsc-addition.villager.trades"),
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
            // Vanilla villagers collect food and seeds, plus whatever their profession asks for.
            // wantsToPickUp alone is stricter than that (it only looks at requestedItems), so the
            // collectable set is spelled out here.
            return contains(VILLAGER_SLOTS, slot)
                    && (COLLECTABLE.contains(stack.getItem())
                            || villager.wantsToPickUp((ServerLevel) villager.level(), stack));
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
                    ChatFormatting.DARK_GRAY);
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

            // addSlot assigns Slot.index; writing into the list directly would leave every slot
            // reporting index 0, which the drag/quick-craft paths rely on.
            for (Slot slot : rebuilt)
            {
                addSlot(slot);
            }
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
            unfreeze(villager);
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

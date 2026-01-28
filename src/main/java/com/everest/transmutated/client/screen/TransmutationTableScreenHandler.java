package com.everest.transmutated.client.screen;

import com.everest.transmutated.block.entity.TransmutationTableBlockEntity;
import com.everest.transmutated.client.init.TransmutatedScreenHandlers;
import com.everest.transmutated.init.TransmutationRecipes;
import com.everest.transmutated.recipe.TransmutationRecipe;
import com.everest.transmutated.recipe.VirtualTransmutationRecipe;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TransmutationTableScreenHandler extends ScreenHandler {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int PLAYER_INVENTORY_START = 2;
    public static final int PLAYER_HOTBAR_START = 29;
    public static final int PLAYER_HOTBAR_END = 37;

    private final ScreenHandlerContext context;
    private final World world;
    private final Property selectedRecipe;

    private Runnable contentsChangedListener = () -> {
    };

    private final Inventory inventory;
    private final TransmutationTableBlockEntity blockEntity;

    private List<VirtualTransmutationRecipe> availableRecipes = Lists.newArrayList();
    private ItemStack lastInput = ItemStack.EMPTY;
    private long lastTakeTime;

    public TransmutationTableScreenHandler(int syncId, PlayerInventory inv, BlockPos pos) {
        this(syncId, inv, ScreenHandlerContext.create(inv.player.getWorld(), pos));
    }

    public TransmutationTableScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext context) {
        super(TransmutatedScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, syncId);
        this.context = context;
        this.world = inv.player.getWorld();
        this.selectedRecipe = Property.create();

        this.blockEntity = context.get(
                (world, pos) -> world.getBlockEntity(pos) instanceof TransmutationTableBlockEntity be ? be : null
        ).orElse(null);

        this.inventory = new SimpleInventory(2) {
            @Override
            public void markDirty() {
                super.markDirty();
                onContentChanged(this);
                contentsChangedListener.run();
            }
        };

        this.addSlot(new Slot(inventory, INPUT_SLOT, 20, 33) {
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                rebuildRecipes();
            }
        });

        this.addSlot(new Slot(inventory, OUTPUT_SLOT, 143, 33) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                return hasStack() && canCraft();
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                stack.onCraftByPlayer(player.getWorld(), player, stack.getCount());

                if (!world.isClient()) {
                    int selected = selectedRecipe.get();
                    if (selected >= 0 && selected < availableRecipes.size()) {
                        ItemStack input = inventory.getStack(INPUT_SLOT);
                        if (!input.isEmpty()) {
                            input.decrement(1);
                            if (input.isEmpty()) {
                                inventory.setStack(INPUT_SLOT, ItemStack.EMPTY);
                            }
                        }
                    }
                }

                context.run((world, pos) -> {
                    long time = world.getTime();
                    if (time != lastTakeTime) {
                        world.playSound(
                                null,
                                pos,
                                SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                                SoundCategory.BLOCKS,
                                1.0F,
                                1.0F
                        );
                        lastTakeTime = time;
                    }

                    if (blockEntity != null) {
                        blockEntity.setSelectedRecipe(-1);
                        blockEntity.markDirty();
                    }
                });

                inventory.setStack(OUTPUT_SLOT, ItemStack.EMPTY);
                selectedRecipe.set(-1);
                rebuildRecipes();

                sendContentUpdates();
                super.onTakeItem(player, stack);
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }

        this.addProperty(this.selectedRecipe);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity != null && !blockEntity.isRemoved();
    }

    public List<VirtualTransmutationRecipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public int getSelectedRecipe() {
        return selectedRecipe.get();
    }

    public void setContentsChangedListener(Runnable listener) {
        this.contentsChangedListener = listener;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (!player.isAlive() || player.isSpectator()) {
            player.dropItem(this.inventory.removeStack(INPUT_SLOT), false);
            player.dropItem(this.inventory.removeStack(OUTPUT_SLOT), false);
        } else {
            ItemStack inputStack = this.inventory.removeStack(INPUT_SLOT);
            if (!inputStack.isEmpty()) {
                if (!player.getInventory().insertStack(inputStack)) {
                    player.dropItem(inputStack, false);
                }
            }

            ItemStack outputStack = this.inventory.removeStack(OUTPUT_SLOT);
            if (!outputStack.isEmpty()) {
                if (!player.getInventory().insertStack(outputStack)) {
                    player.dropItem(outputStack, false);
                }
            }
        }

        if (blockEntity != null) {
            blockEntity.setSelectedRecipe(-1);
            blockEntity.markDirty();
        }

        sendContentUpdates();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0 || id >= availableRecipes.size()) return false;

        selectedRecipe.set(id);
        ItemStack result = getDisplayResult(id);
        inventory.setStack(OUTPUT_SLOT, result.copy());

        if (blockEntity != null) {
            blockEntity.setSelectedRecipe(id);
            blockEntity.markDirty();
        }

        sendContentUpdates();
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();

            if (slotIndex == OUTPUT_SLOT) {
                if (!this.insertItem(stackInSlot, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(stackInSlot, originalStack);
            } else if (slotIndex == INPUT_SLOT) {
                if (!this.insertItem(stackInSlot, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= PLAYER_INVENTORY_START && slotIndex <= PLAYER_HOTBAR_END) {
                if (world.getRecipeManager()
                        .getFirstMatch(TransmutationRecipes.TYPE,
                                new SingleStackRecipeInput(stackInSlot),
                                world).isPresent()) {
                    if (!this.insertItem(stackInSlot, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex < PLAYER_HOTBAR_START) {
                    if (!this.insertItem(stackInSlot, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.insertItem(stackInSlot, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stackInSlot.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, stackInSlot);
        }

        return originalStack;
    }

    public void onContentChanged(Inventory inventory) {
        ItemStack current = inventory.getStack(INPUT_SLOT);
        if (!ItemStack.areEqual(current, lastInput)) {
            lastInput = current.copy();
            rebuildRecipes();
        }
    }

    public boolean hasInput() {
        return !inventory.getStack(INPUT_SLOT).isEmpty()
                && !availableRecipes.isEmpty();
    }

    public boolean canCraft() {
        if (availableRecipes.isEmpty()) return false;

        int index = selectedRecipe.get();
        if (index < 0 || index >= availableRecipes.size()) return false;

        ItemStack input = inventory.getStack(INPUT_SLOT);
        if (input.isEmpty()) return false;

        VirtualTransmutationRecipe recipe = availableRecipes.get(index);
        return input.isIn(recipe.base().ingredientTag());
    }

    public int getAvailableRecipeCount() {
        return availableRecipes.size();
    }

    private void rebuildRecipes() {
        availableRecipes.clear();
        selectedRecipe.set(-1);
        inventory.setStack(OUTPUT_SLOT, ItemStack.EMPTY);

        ItemStack input = inventory.getStack(INPUT_SLOT);
        if (input.isEmpty()) {
            sendContentUpdates();
            return;
        }

        var recipes = world.getRecipeManager().getAllMatches(
                TransmutationRecipes.TYPE,
                new SingleStackRecipeInput(input),
                world
        );

        var items = world.getRegistryManager()
                .getWrapperOrThrow(RegistryKeys.ITEM);

        for (RecipeEntry<TransmutationRecipe> entry : recipes) {
            for (var itemEntry : items.streamEntries().toList()) {
                Item item = itemEntry.value();
                if (itemEntry.isIn(entry.value().resultTag())) {
                    availableRecipes.add(
                            new VirtualTransmutationRecipe(entry.value(), item)
                    );
                }
            }
        }

        sendContentUpdates();
    }

    public ItemStack getDisplayResult(int index) {
        if (index < 0 || index >= availableRecipes.size()) return ItemStack.EMPTY;
        return new ItemStack(availableRecipes.get(index).result());
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        if (blockEntity != null) {
            blockEntity.markDirty();
        }
    }
}
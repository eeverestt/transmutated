package com.everest.transmutated.block.entity;

import com.everest.transmutated.client.screen.TransmutationTableScreenHandler;
import com.everest.transmutated.init.TransmutatedBlockEntities;
import com.everest.transmutated.init.TransmutationRecipes;
import com.everest.transmutated.recipe.TransmutationRecipe;
import com.everest.transmutated.util.ImplementedInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransmutationTableBlockEntity extends BlockEntity
        implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final DefaultedList<ItemStack> inventory =
            DefaultedList.ofSize(2, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 72;
    private int selectedRecipe = -1;
    private List<RecipeEntry<TransmutationRecipe>> availableRecipes = new ArrayList<>();

    private final PropertyDelegate propertyDelegate;

    public TransmutationTableBlockEntity(BlockPos pos, BlockState state) {
        super(TransmutatedBlockEntities.TRANSMUTATION_TABLE_BE, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> selectedRecipe;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                    case 2 -> {
                        selectedRecipe = value;
                        updateOutputSlot();
                    }
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };
    }

    public static void tick(World world, BlockPos pos, BlockState state, TransmutationTableBlockEntity be) {
        if (world.isClient) return;

        be.updateAvailableRecipes();

        if (be.hasRecipe()) {
            be.progress++;

            if (be.progress >= be.maxProgress) {
                be.craftItem();
                be.progress = 0;
                be.updateAvailableRecipes();
            }
        } else {
            be.progress = 0;
        }

        be.markDirty();
    }

    private void updateAvailableRecipes() {
        if (world == null) return;

        ItemStack inputStack = getStack(INPUT_SLOT);
        if (inputStack.isEmpty()) {
            availableRecipes = new ArrayList<>();
            selectedRecipe = -1;
            updateOutputSlot();
            return;
        }

        availableRecipes = world.getRecipeManager().getAllMatches(
                TransmutationRecipes.TYPE,
                new SingleStackRecipeInput(inputStack),
                world
        );

        if (selectedRecipe >= availableRecipes.size()) {
            selectedRecipe = -1;
            updateOutputSlot();
        }
    }

    private void updateOutputSlot() {
        if (world == null || selectedRecipe < 0 || selectedRecipe >= availableRecipes.size()) {
            setStack(OUTPUT_SLOT, ItemStack.EMPTY);
            return;
        }

        RecipeEntry<TransmutationRecipe> recipe = availableRecipes.get(selectedRecipe);
        ItemStack result = recipe.value().getResult(world.getRegistryManager()).copy();
        setStack(OUTPUT_SLOT, result);
    }

    private boolean hasRecipe() {
        if (world == null || selectedRecipe < 0 || selectedRecipe >= availableRecipes.size()) {
            return false;
        }

        ItemStack input = getStack(INPUT_SLOT);
        if (input.isEmpty()) return false;

        RecipeEntry<TransmutationRecipe> recipe = availableRecipes.get(selectedRecipe);
        if (!recipe.value().matches(new SingleStackRecipeInput(input), world)) {
            return false;
        }

        ItemStack result = recipe.value().getResult(world.getRegistryManager());
        return canInsertItemIntoOutputSlot(result)
                && canInsertAmountIntoOutputSlot(result.getCount());
    }

    private void craftItem() {
        if (world == null || selectedRecipe < 0 || selectedRecipe >= availableRecipes.size()) {
            return;
        }

        RecipeEntry<TransmutationRecipe> recipe = availableRecipes.get(selectedRecipe);
        ItemStack result = recipe.value().getResult(world.getRegistryManager()).copy();

        removeStack(INPUT_SLOT, 1);

        ItemStack output = getStack(OUTPUT_SLOT);
        if (output.isEmpty()) {
            setStack(OUTPUT_SLOT, result);
        } else {
            output.increment(result.getCount());
        }
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack stack) {
        ItemStack output = getStack(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.areItemsAndComponentsEqual(output, stack);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack output = getStack(OUTPUT_SLOT);
        int max = output.isEmpty() ? 64 : output.getMaxCount();
        return output.getCount() + count <= max;
    }

    public List<RecipeEntry<TransmutationRecipe>> getAvailableRecipes() {
        return availableRecipes;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.transmutated.transmutation_table");
    }

    @Override
    public @Nullable ScreenHandler createMenu(
            int syncId,
            PlayerInventory playerInventory,
            PlayerEntity player
    ) {
        return new TransmutationTableScreenHandler(
                syncId,
                playerInventory,
                ScreenHandlerContext.create(world, pos)
        );
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        Inventories.writeNbt(nbt, inventory, lookup);
        nbt.putInt("Progress", progress);
        nbt.putInt("MaxProgress", maxProgress);
        nbt.putInt("SelectedRecipe", selectedRecipe);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        Inventories.readNbt(nbt, inventory, lookup);
        progress = nbt.getInt("Progress");
        maxProgress = nbt.getInt("MaxProgress");
        selectedRecipe = nbt.getInt("SelectedRecipe");
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
        return createNbt(lookup);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public int getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(int recipe) {
        this.selectedRecipe = recipe;
        this.updateOutputSlot();
        this.markDirty();
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }
}
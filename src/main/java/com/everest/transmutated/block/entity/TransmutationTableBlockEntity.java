package com.everest.transmutated.block.entity;

import com.everest.transmutated.client.screen.TransmutationTableScreenHandler;
import com.everest.transmutated.init.TransmutatedBlockEntities;
import com.everest.transmutated.init.TransmutationRecipes;
import com.everest.transmutated.recipe.TransmutationRecipe;
import com.everest.transmutated.recipe.VirtualTransmutationRecipe;
import com.everest.transmutated.util.ImplementedInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransmutationTableBlockEntity extends BlockEntity
        implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    public int ticks;
    public float nextPageAngle;
    public float pageAngle;
    public float flipRandom;
    public float flipTurn;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float bookRotation;
    public float lastBookRotation;
    public float targetBookRotation;
    private static final Random RANDOM = Random.create();

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final DefaultedList<ItemStack> inventory =
            DefaultedList.ofSize(2, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 72;
    private int selectedRecipe = -1;

    private List<RecipeEntry<TransmutationRecipe>> availableRecipes = new ArrayList<>();
    private List<VirtualTransmutationRecipe> virtualRecipes = new ArrayList<>();

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
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
                case 2 -> setSelectedRecipe(value);
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public TransmutationTableBlockEntity(BlockPos pos, BlockState state) {
        super(TransmutatedBlockEntities.TRANSMUTATION_TABLE_BE, pos, state);
    }

//    @Override
//    public void markDirty() {
//        super.markDirty();
//        if (world != null && !world.isClient) {
//            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
//        }
//    }

    public void onInputChanged() {
        ItemStack input = getStack(INPUT_SLOT);
        if (input.isEmpty()) {
            setSelectedRecipe(-1);
            markDirty();
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, TransmutationTableBlockEntity be, @Nullable PlayerEntity player) {
        be.pageTurningSpeed = be.nextPageTurningSpeed;
        be.lastBookRotation = be.bookRotation;
        PlayerEntity playerEntity = world.getClosestPlayer((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, (double)3.0F, false);
        if (playerEntity != null) {
            double d = playerEntity.getX() - ((double)pos.getX() + (double)0.5F);
            double e = playerEntity.getZ() - ((double)pos.getZ() + (double)0.5F);
            be.targetBookRotation = (float) MathHelper.atan2(e, d);
            be.nextPageTurningSpeed += 0.1F;
            if (be.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
                float f = be.flipRandom;

                do {
                    be.flipRandom += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(f == be.flipRandom);
            }
        } else {
            be.targetBookRotation += 0.02F;
            be.nextPageTurningSpeed -= 0.1F;
        }

        while(be.bookRotation >= (float)Math.PI) {
            be.bookRotation -= ((float)Math.PI * 2F);
        }

        while(be.bookRotation < -(float)Math.PI) {
            be.bookRotation += ((float)Math.PI * 2F);
        }

        while(be.targetBookRotation >= (float)Math.PI) {
            be.targetBookRotation -= ((float)Math.PI * 2F);
        }

        while(be.targetBookRotation < -(float)Math.PI) {
            be.targetBookRotation += ((float)Math.PI * 2F);
        }

        float g;
        for(g = be.targetBookRotation - be.bookRotation; g >= (float)Math.PI; g -= ((float)Math.PI * 2F)) {
        }

        while(g < -(float)Math.PI) {
            g += ((float)Math.PI * 2F);
        }

        be.bookRotation += g * 0.4F;
        be.nextPageTurningSpeed = MathHelper.clamp(be.nextPageTurningSpeed, 0.0F, 1.0F);
        ++be.ticks;
        be.pageAngle = be.nextPageAngle;
        float h = (be.flipRandom - be.nextPageAngle) * 0.4F;
        float i = 0.2F;
        h = MathHelper.clamp(h, -0.2F, 0.2F);
        be.flipTurn += (h - be.flipTurn) * 0.9F;
        be.nextPageAngle += be.flipTurn;

        if (world.isClient) return;

        be.updateAvailableRecipes();

        if (be.hasRecipe()) {
            be.updateAvailableRecipes();
        }

        be.markDirty();
    }

    private void updateAvailableRecipes() {
        if (world == null) return;

        ItemStack input = getStack(INPUT_SLOT);
        if (input.isEmpty()) {
            availableRecipes.clear();
            virtualRecipes.clear();
            selectedRecipe = -1;
            updateOutputSlot();
            return;
        }

        availableRecipes = world.getRecipeManager().getAllMatches(
                TransmutationRecipes.TYPE,
                new SingleStackRecipeInput(input),
                world
        );

        virtualRecipes.clear();
        var items = world.getRegistryManager()
                .getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ITEM);

        for (RecipeEntry<TransmutationRecipe> entry : availableRecipes) {
            TransmutationRecipe recipe = entry.value();

            items.getOptional(recipe.resultTag()).ifPresent(resultItems -> {
                for (RegistryEntry<Item> itemEntry : resultItems) {
                    virtualRecipes.add(new VirtualTransmutationRecipe(recipe, itemEntry.value()));
                }
            });
        }

        if (selectedRecipe < 0 || selectedRecipe >= availableRecipes.size()) {
            selectedRecipe = -1;
        }

        updateOutputSlot();
    }

    private void updateOutputSlot() {
        if (world == null || selectedRecipe < 0 || selectedRecipe >= availableRecipes.size()) {
            setStack(OUTPUT_SLOT, ItemStack.EMPTY);
            return;
        }

        ItemStack result = availableRecipes
                .get(selectedRecipe)
                .value()
                .getResult(world.getRegistryManager())
                .copy();

        setStack(OUTPUT_SLOT, result);
    }

    private boolean hasRecipe() {
        if (world == null || selectedRecipe < 0 || selectedRecipe >= availableRecipes.size()) {
            return false;
        }

        ItemStack input = getStack(INPUT_SLOT);
        if (input.isEmpty()) return false;

        ItemStack result = availableRecipes
                .get(selectedRecipe)
                .value()
                .getResult(world.getRegistryManager());

        return canInsertItemIntoOutputSlot(result)
                && canInsertAmountIntoOutputSlot(result.getCount());
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack stack) {
        ItemStack output = getStack(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.areItemsAndComponentsEqual(output, stack);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack output = getStack(OUTPUT_SLOT);
        int max = output.isEmpty() ? output.getMaxCount() : output.getMaxCount();
        return output.getCount() + count <= max;
    }

    public void setSelectedRecipe(int index) {
        this.selectedRecipe = index;
        updateOutputSlot();
        markDirty();
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getSelectedRecipe() {
        return selectedRecipe;
    }

    public List<VirtualTransmutationRecipe> getAvailableRecipes() {
        return virtualRecipes;
    }

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.transmutated.transmutation_table.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TransmutationTableScreenHandler(syncId, playerInventory, this.pos);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        Inventories.writeNbt(nbt, inventory, lookup);
        nbt.putInt("Progress", progress);
        nbt.putInt("SelectedRecipe", selectedRecipe);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        Inventories.readNbt(nbt, inventory, lookup);
        progress = nbt.getInt("Progress");
        selectedRecipe = nbt.getInt("SelectedRecipe");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }
}
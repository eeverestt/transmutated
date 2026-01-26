package com.everest.transmutated.recipe;

import com.everest.transmutated.init.TransmutationRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public record TransmutationRecipe(Ingredient ingredient, ItemStack result)
        implements Recipe<SingleStackRecipeInput> {

    @Override
    public boolean matches(SingleStackRecipeInput input, World world) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return result.copy();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        ingredients.add(ingredient);
        return ingredients;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TransmutationRecipes.SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TransmutationRecipes.TYPE;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }


    public static class Serializer implements RecipeSerializer<TransmutationRecipe> {

        public static final MapCodec<TransmutationRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(TransmutationRecipe::ingredient),
                        ItemStack.CODEC.fieldOf("result").forGetter(TransmutationRecipe::result)
                ).apply(inst, TransmutationRecipe::new));

        public static final PacketCodec<RegistryByteBuf, TransmutationRecipe> PACKET_CODEC =
                PacketCodec.tuple(
                        Ingredient.PACKET_CODEC, TransmutationRecipe::ingredient,
                        ItemStack.PACKET_CODEC, TransmutationRecipe::result,
                        TransmutationRecipe::new
                );

        @Override
        public MapCodec<TransmutationRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, TransmutationRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}

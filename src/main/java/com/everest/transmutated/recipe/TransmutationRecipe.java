package com.everest.transmutated.recipe;

import com.everest.transmutated.init.TransmutationRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public record TransmutationRecipe(
        TagKey<Item> ingredientTag,
        TagKey<Item> resultTag
) implements Recipe<SingleStackRecipeInput> {

    @Override
    public boolean matches(SingleStackRecipeInput input, World world) {
        return input.item().isIn(ingredientTag);
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        // Get the first item from the result tag as a default
        var items = lookup.getWrapperOrThrow(RegistryKeys.ITEM);
        return items.getOptional(resultTag)
                .flatMap(entries -> entries.stream().findFirst())
                .map(entry -> new ItemStack(entry.value()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
        return craft(null, lookup);
    }

    public List<Item> getAllResults(RegistryWrapper.WrapperLookup lookup) {
        RegistryWrapper<Item> items = lookup.getWrapperOrThrow(RegistryKeys.ITEM);
        return items.getOptional(resultTag)
                .stream()
                .flatMap(entries -> entries.stream())
                .map(RegistryEntry::value)
                .toList();
    }

    @Override
    public DefaultedList<net.minecraft.recipe.Ingredient> getIngredients() {
        var ingredient = net.minecraft.recipe.Ingredient.fromTag(ingredientTag);
        return DefaultedList.copyOf(ingredient);
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
                        Identifier.CODEC.fieldOf("ingredient")
                                .xmap(id -> TagKey.of(RegistryKeys.ITEM, id), TagKey::id)
                                .forGetter(TransmutationRecipe::ingredientTag),

                        Identifier.CODEC.fieldOf("result")
                                .xmap(id -> TagKey.of(RegistryKeys.ITEM, id), TagKey::id)
                                .forGetter(TransmutationRecipe::resultTag)
                ).apply(inst, TransmutationRecipe::new));

        public static final PacketCodec<RegistryByteBuf, TransmutationRecipe> PACKET_CODEC =
                PacketCodec.tuple(
                        Identifier.PACKET_CODEC, r -> r.ingredientTag().id(),
                        Identifier.PACKET_CODEC, r -> r.resultTag().id(),
                        (i, r) -> new TransmutationRecipe(
                                TagKey.of(RegistryKeys.ITEM, i),
                                TagKey.of(RegistryKeys.ITEM, r)
                        )
                );

        @Override public MapCodec<TransmutationRecipe> codec() { return CODEC; }
        @Override public PacketCodec<RegistryByteBuf, TransmutationRecipe> packetCodec() { return PACKET_CODEC; }
    }
}
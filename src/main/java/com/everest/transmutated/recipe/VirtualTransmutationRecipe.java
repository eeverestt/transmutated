package com.everest.transmutated.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

public record VirtualTransmutationRecipe(
        TransmutationRecipe base,
        Item result
) {}


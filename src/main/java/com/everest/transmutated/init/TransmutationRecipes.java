package com.everest.transmutated.init;

import com.everest.transmutated.Transmutated;
import com.everest.transmutated.recipe.TransmutationRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class TransmutationRecipes {

    public static final RecipeSerializer<TransmutationRecipe> SERIALIZER =
            Registry.register(
                    Registries.RECIPE_SERIALIZER,
                    Transmutated.id("transmutation"),
                    new TransmutationRecipe.Serializer()
            );

    public static final RecipeType<TransmutationRecipe> TYPE =
            Registry.register(
                    Registries.RECIPE_TYPE,
                    Transmutated.id("transmutation"),
                    new RecipeType<>() {
                        @Override
                        public String toString() {
                            return "transmutation";
                        }
                    }
            );

    public static void registerAll() {}
}

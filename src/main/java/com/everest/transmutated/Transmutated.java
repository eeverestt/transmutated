package com.everest.transmutated;

import com.everest.transmutated.client.init.TransmutatedScreenHandlers;
import com.everest.transmutated.init.TransmutatedBlockEntities;
import com.everest.transmutated.init.TransmutatedBlocks;
import com.everest.transmutated.init.TransmutationRecipes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Transmutated implements ModInitializer {
    public static String MODID = "transmutated";

    @Override
    public void onInitialize() {
        TransmutatedBlocks.registerAll();
        TransmutatedBlockEntities.registerAll();
        TransmutationRecipes.registerAll();
        TransmutatedScreenHandlers.registerAll();
    }

    public static Identifier id(String s) {
        return Identifier.of(MODID, s);
    }
}
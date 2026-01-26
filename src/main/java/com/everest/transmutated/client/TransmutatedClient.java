package com.everest.transmutated.client;

import com.everest.transmutated.client.init.TransmutatedScreenHandlers;
import com.everest.transmutated.client.screen.TransmutationTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class TransmutatedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(
                TransmutatedScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER,
                TransmutationTableScreen::new
        );
    }
}

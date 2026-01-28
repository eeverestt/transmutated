package com.everest.transmutated.client.init;

import com.everest.transmutated.Transmutated;
import com.everest.transmutated.client.screen.TransmutationTableScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TransmutatedScreenHandlers {
    public static final ExtendedScreenHandlerType<TransmutationTableScreenHandler, BlockPos>
            TRANSMUTATION_TABLE_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(Transmutated.MODID, "transmutation_table"),
                    new ExtendedScreenHandlerType<>(
                            TransmutationTableScreenHandler::new,
                            BlockPos.PACKET_CODEC
                    )
            );


    public static void registerAll() {}
}
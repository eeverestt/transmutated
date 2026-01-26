package com.everest.transmutated.client.init;

import com.everest.transmutated.Transmutated;
import com.everest.transmutated.client.screen.TransmutationTableScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TransmutatedScreenHandlers {
    public static final ScreenHandlerType<TransmutationTableScreenHandler> TRANSMUTATION_TABLE_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(Transmutated.MODID, "transmutation_table"),
                    new ExtendedScreenHandlerType<TransmutationTableScreenHandler, BlockPos>(
                            (syncId, inventory, pos) -> new TransmutationTableScreenHandler(syncId, inventory, pos),
                            BlockPos.PACKET_CODEC
                    )
            );



    public static void registerAll() {}
}
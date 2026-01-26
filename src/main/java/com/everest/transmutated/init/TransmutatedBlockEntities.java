package com.everest.transmutated.init;

import com.everest.transmutated.Transmutated;
import com.everest.transmutated.block.entity.TransmutationTableBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class TransmutatedBlockEntities {
    public static final BlockEntityType<TransmutationTableBlockEntity> TRANSMUTATION_TABLE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Transmutated.id("transmutation_table_be"),
                    BlockEntityType.Builder.create(TransmutationTableBlockEntity::new, TransmutatedBlocks.TRANSMUTATION_TABLE).build(null));

    public static void registerAll() {}
}

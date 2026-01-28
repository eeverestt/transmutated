package com.everest.transmutated.init;

import com.everest.transmutated.Transmutated;
import com.everest.transmutated.block.TransmutationTable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class TransmutatedBlocks {
    public static final Block TRANSMUTATION_TABLE = registerBlock("transmutation_table",
            new TransmutationTable(AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().emissiveLighting(Blocks::always).luminance((state) -> 5).strength(5.0F, 1200.0F)));

    private static Block registerBlockWithoutBlockItem(String name, Block block) {
        return Registry.register(Registries.BLOCK, Transmutated.id(name), block);
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Transmutated.id(name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Transmutated.id(name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerAll() {}
}

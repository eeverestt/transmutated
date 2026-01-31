package com.everest.transmutated.init;

import com.everest.transmutated.Transmutated;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TransmutatedTags {
    public static final TagKey<Item> STONES = TagKey.of(RegistryKeys.ITEM, Transmutated.id("stones"));
    public static final TagKey<Item> NETHER_STONES = TagKey.of(RegistryKeys.ITEM, Transmutated.id("nether_stones"));
    public static final TagKey<Item> SAPLINGS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("saplings"));
    public static final TagKey<Item> LOGS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("logs"));
    public static final TagKey<Item> LEAVES = TagKey.of(RegistryKeys.ITEM, Transmutated.id("leaves"));
    public static final TagKey<Item> CROPS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("crops"));
    public static final TagKey<Item> SEEDS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("seeds"));
    public static final TagKey<Item> SMALL_FLOWERS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("small_flowers"));
    public static final TagKey<Item> TALL_FLOWERS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("tall_flowers"));
    public static final TagKey<Item> DYES = TagKey.of(RegistryKeys.ITEM, Transmutated.id("dyes"));
    public static final TagKey<Item> SANDS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("sands"));
    public static final TagKey<Item> SOULS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("souls"));
    public static final TagKey<Item> GROUNDS = TagKey.of(RegistryKeys.ITEM, Transmutated.id("grounds"));
}
package com.everest.transmutated.init;

import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

import java.util.HashMap;
import java.util.Map;

public final class TransmutationXpCosts {

    private static final Map<TagKey<Item>, Integer> COSTS = new HashMap<>();

    public static void register(TagKey<Item> tag, int xp) {
        COSTS.put(tag, xp);
    }

    public static int getCost(TagKey<Item> tag) {
        return COSTS.getOrDefault(tag, 1);
    }

    private TransmutationXpCosts() {}
}

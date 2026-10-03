package com.achilles.registry;

import com.achilles.Achilles;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Achilles.MODID);

    /** Solo para creativo / administradores: las estatuas "de verdad" aparecen solas con el mundo. */
    public static final RegistryObject<Item> STATUE = ITEMS.register("achilles_statue",
            () -> new BlockItem(ModBlocks.STATUE.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> ACHILLES_SPAWN_EGG = ITEMS.register("achilles_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ACHILLES, 0xC8963C, 0xB22222, new Item.Properties()));
}

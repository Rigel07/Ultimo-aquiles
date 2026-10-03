package com.achilles.registry;

import com.achilles.Achilles;
import com.achilles.block.StatueBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Achilles.MODID);

    public static final RegistryObject<BlockEntityType<StatueBlockEntity>> STATUE = BLOCK_ENTITIES.register("statue",
            () -> BlockEntityType.Builder.of(StatueBlockEntity::new, ModBlocks.STATUE.get()).build(null));
}

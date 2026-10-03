package com.achilles.registry;

import com.achilles.Achilles;
import com.achilles.block.AchillesStatueBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Achilles.MODID);

    /** Indestructible: dureza -1 y resistencia enorme (además se cancela el evento de romper). */
    public static final RegistryObject<Block> STATUE = BLOCKS.register("achilles_statue",
            () -> new AchillesStatueBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> 6)));
}

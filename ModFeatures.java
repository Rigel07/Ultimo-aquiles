package com.achilles.registry;

import com.achilles.Achilles;
import com.achilles.worldgen.StatueFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Achilles.MODID);

    public static final RegistryObject<Feature<?>> STATUE = FEATURES.register("statue", StatueFeature::new);
}

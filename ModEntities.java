package com.achilles.registry;

import com.achilles.Achilles;
import com.achilles.entity.AchillesEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Achilles.MODID);

    public static final RegistryObject<EntityType<AchillesEntity>> ACHILLES = ENTITIES.register("achilles",
            () -> EntityType.Builder.of(AchillesEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.9F)
                    .clientTrackingRange(8)
                    .build(Achilles.MODID + ":achilles"));
}

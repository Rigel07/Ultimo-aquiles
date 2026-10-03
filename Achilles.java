package com.achilles;

import com.achilles.entity.AchillesEntity;
import com.achilles.events.ModEvents;
import com.achilles.network.NetworkHandler;
import com.achilles.registry.ModBlockEntities;
import com.achilles.registry.ModBlocks;
import com.achilles.registry.ModCreativeTabs;
import com.achilles.registry.ModEntities;
import com.achilles.registry.ModFeatures;
import com.achilles.registry.ModItems;
import com.achilles.registry.ModSounds;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Achilles.MODID)
public class Achilles {
    public static final String MODID = "achilles";

    public Achilles() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::registerSpawnPlacements);

        MinecraftForge.EVENT_BUS.register(new ModEvents());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ACHILLES.get(), AchillesEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.ACHILLES.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AchillesEntity::checkSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }
}

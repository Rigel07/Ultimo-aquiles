package com.achilles.registry;

import com.achilles.Achilles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Achilles.MODID);

    public static final RegistryObject<SoundEvent> ACHILLES_AMBIENT = register("entity.achilles.ambient");
    public static final RegistryObject<SoundEvent> ACHILLES_HURT = register("entity.achilles.hurt");
    public static final RegistryObject<SoundEvent> ACHILLES_DEATH = register("entity.achilles.death");
    public static final RegistryObject<SoundEvent> ACHILLES_TRIP = register("entity.achilles.trip");
    public static final RegistryObject<SoundEvent> ACHILLES_GET_UP = register("entity.achilles.get_up");
    public static final RegistryObject<SoundEvent> ACHILLES_CLANG = register("entity.achilles.clang");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Achilles.MODID, name)));
    }
}

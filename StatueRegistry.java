package com.achilles.data;

import com.achilles.block.StatueBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Estatuas cargadas ahora mismo (se registran al cargar el chunk y se quitan al descargarlo). */
public final class StatueRegistry {
    private static final Set<StatueBlockEntity> LOADED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private StatueRegistry() {
    }

    public static void add(StatueBlockEntity statue) {
        LOADED.add(statue);
    }

    public static void remove(StatueBlockEntity statue) {
        LOADED.remove(statue);
    }

    /** Estatua cuya zona contiene esa posición (o null). */
    @Nullable
    public static StatueBlockEntity findCovering(Level level, Vec3 pos) {
        for (StatueBlockEntity statue : LOADED) {
            if (statue.getLevel() == level && !statue.isRemoved() && statue.contains(pos)) {
                return statue;
            }
        }
        return null;
    }
}

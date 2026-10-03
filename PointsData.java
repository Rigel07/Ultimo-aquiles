package com.achilles.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Puntos de estatua por jugador (1 por estatua reclamada). Se guarda en el mundo (overworld). */
public class PointsData extends SavedData {
    private static final String NAME = "achilles_points";

    /** UUID -> { ganados, gastados } */
    private final Map<UUID, int[]> points = new HashMap<>();

    public static PointsData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PointsData::load, PointsData::new, NAME);
    }

    public static PointsData load(CompoundTag tag) {
        PointsData data = new PointsData();
        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.points.put(UUID.fromString(entry.getString("Id")),
                    new int[]{entry.getInt("Earned"), entry.getInt("Spent")});
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, int[]> e : points.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", e.getKey().toString());
            entry.putInt("Earned", e.getValue()[0]);
            entry.putInt("Spent", e.getValue()[1]);
            list.add(entry);
        }
        tag.put("Players", list);
        return tag;
    }

    private int[] entry(UUID id) {
        return points.computeIfAbsent(id, k -> new int[]{0, 0});
    }

    public void addPoint(UUID id) {
        entry(id)[0]++;
        setDirty();
    }

    public int available(UUID id) {
        int[] e = entry(id);
        return Math.max(0, e[0] - e[1]);
    }

    public void spend(UUID id, int amount) {
        entry(id)[1] += amount;
        setDirty();
    }
}

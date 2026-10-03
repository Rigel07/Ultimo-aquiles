package com.achilles.worldgen;

import com.achilles.block.AchillesStatueBlock;
import com.achilles.entity.AchillesEntity;
import com.achilles.registry.ModBlocks;
import com.achilles.registry.ModEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Genera UNA estatua por región de 16x16 chunks, siempre en la zona central de la región
 * (margen de 5 chunks). Así dos estatuas están siempre a más de ~150 bloques entre sí
 * y sus zonas (radio máx. 58) no pueden chocar.
 */
public class StatueFeature extends Feature<NoneFeatureConfiguration> {
    private static final int REGION = 16;
    private static final int MARGIN = 5;

    public StatueFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());

        int regionX = Math.floorDiv(chunk.x, REGION);
        int regionZ = Math.floorDiv(chunk.z, REGION);
        RandomSource random = RandomSource.create(level.getSeed()
                ^ (regionX * 341873128712L) ^ (regionZ * 132897987541L) ^ 0x5DEECE66DL);

        int chosenX = MARGIN + random.nextInt(REGION - 2 * MARGIN);
        int chosenZ = MARGIN + random.nextInt(REGION - 2 * MARGIN);
        if (Math.floorMod(chunk.x, REGION) != chosenX || Math.floorMod(chunk.z, REGION) != chosenZ) {
            return false;
        }

        int x = chunk.getMinBlockX() + 2 + random.nextInt(12);
        int z = chunk.getMinBlockZ() + 2 + random.nextInt(12);
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos base = new BlockPos(x, y, z);

        if (y < level.getMinBuildHeight() + 5 || y > level.getMaxBuildHeight() - 12) return false;
        if (!level.getFluidState(base).isEmpty() || !level.getFluidState(base.below()).isEmpty()) return false;

        // Plataforma de ladrillos 5x5 con cimientos, y espacio libre encima
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    BlockPos under = base.offset(dx, -1 - dy, dz);
                    if (dy == 0 || level.getBlockState(under).canBeReplaced()) {
                        level.setBlock(under, pickBrick(random), 2);
                    }
                }
                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos above = base.offset(dx, dy, dz);
                    if (!level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Pilares rotos en las esquinas
        for (int sx = -2; sx <= 2; sx += 4) {
            for (int sz = -2; sz <= 2; sz += 4) {
                int height = 1 + random.nextInt(3);
                for (int i = 0; i < height; i++) {
                    level.setBlock(base.offset(sx, i, sz), pickBrick(random), 2);
                }
            }
        }

        // La estatua (2 bloques de alto) mirando en una dirección al azar
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockState lower = ModBlocks.STATUE.get().defaultBlockState()
                .setValue(AchillesStatueBlock.FACING, facing)
                .setValue(AchillesStatueBlock.HALF, DoubleBlockHalf.LOWER);
        level.setBlock(base, lower, 2);
        level.setBlock(base.above(), lower.setValue(AchillesStatueBlock.HALF, DoubleBlockHalf.UPPER), 2);

        // Uno o dos mini Aquiles haciendo guardia (torpemente)
        int guards = 1 + random.nextInt(2);
        for (int i = 0; i < guards; i++) {
            AchillesEntity guard = ModEntities.ACHILLES.get().create(level.getLevel());
            if (guard == null) continue;
            guard.moveTo(x + 0.5D + (random.nextDouble() - 0.5D) * 3.0D, y,
                    z + 0.5D + (random.nextDouble() - 0.5D) * 3.0D, random.nextFloat() * 360.0F, 0.0F);
            guard.finalizeSpawn(level, level.getCurrentDifficultyAt(base), MobSpawnType.STRUCTURE, null, null);
            guard.setPersistenceRequired();
            level.addFreshEntity(guard);
        }
        return true;
    }

    private static BlockState pickBrick(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.2F) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (roll < 0.4F) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        return Blocks.STONE_BRICKS.defaultBlockState();
    }
}

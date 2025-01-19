package net.backstube.structuresaver.structureloader

import net.minecraft.datafixer.fix.ChunkPalettedStorageFix.Facing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

data class StructureLoaderData(
    var pos: BlockPos,
    var name: String,
    var shouldIncludeEntities: Boolean,
    var direction: Int
)
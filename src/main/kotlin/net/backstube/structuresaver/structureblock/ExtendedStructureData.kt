package net.backstube.structuresaver.structureblock

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

data class ExtendedStructureData(
    var pos: BlockPos,
    var name: String,
    var offset: BlockPos,
    var size: Vec3i,
    var shouldIncludeEntities: Boolean,
    var shouldIgnoreAir: Boolean,
    var shouldSaveOnServer: Boolean,
    var showBoundingBox: Boolean
)
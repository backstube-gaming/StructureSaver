package net.backstube.structuresaver

import net.backstube.structuresaver.structuresaveritem.StructureSaverItem.Companion.writeTemplate
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.structure.StructureTemplate
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

class Exporter {
    companion object {
        public val EXPORT_PATH: Path = Path.of(StructureSaver.MODID + "_exports")

        fun export(
            world: World,
            name: String?,
            origin: BlockPos,
            dimensions: Vec3i,
            shouldIncludeEntities: Boolean,
            shouldIngoreAir: Boolean,
            shouldSaveOnServer: Boolean,
            asSnbt: Boolean
        ): Path? {
            if (EXPORT_PATH.notExists()) {
                EXPORT_PATH.createDirectories()
            }
            val template = StructureTemplate()
            template.saveFromWorld(world, origin, dimensions, shouldIncludeEntities,
                if(shouldIngoreAir) Blocks.AIR else null)
            val tag = template.writeNbt(NbtCompound())
            val exportPath: Path;
            try {
                exportPath = writeTemplate(EXPORT_PATH, name, tag, asSnbt)
                return exportPath
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                return null
            }
        }
    }
}
package net.backstube.structuresaver

import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtIo
import net.minecraft.structure.StructureTemplate
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.pathString

class Exporter {
    companion object {
        val EXPORT_PATH: Path = Path.of("structure_exports")

        fun getExportPath(name: String): String{
            return EXPORT_PATH.resolve(name).pathString;
        }

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
            val exportPath: Path
            try {
                exportPath = writeTemplate(EXPORT_PATH, name, tag, asSnbt)
                return exportPath
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                return null
            }
        }

        private fun writeTemplate(exportPath: Path, name: String?, template: NbtCompound?, asSnbt: Boolean): Path {
            val path = makeSpaceForFileAndGetPath(exportPath, name, asSnbt)
            try {
                if (asSnbt) {
                    val snbt: String = NbtHelper.toNbtProviderString(template)
                    Files.writeString(path, snbt)
                } else {
                    val outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)
                    NbtIo.writeCompressed(template, outputStream)
                    outputStream.close()
                }
            } catch (e: IOException) {
                throw java.lang.IllegalStateException("Failed to create template file. Reason: " + e.message, e)
            }
            return path
        }

        private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
        private fun makeSpaceForFileAndGetPath(parentFolder: Path, name: String?, asSnbt: Boolean): Path {
            var extension = if (asSnbt) ".snbt" else ".nbt"
            if (name != null && (name.endsWith(".nbt") || name.endsWith(".snbt")))
                extension = ""

            val filename = (if (name == null) "template" else normalize(name))
            val filepath = parentFolder.resolve(filename + extension)

            try {
                if (Files.exists(filepath)) {
                    val attrs = Files.readAttributes(filepath, BasicFileAttributes::class.java)
                    val timestamp = attrs.creationTime()
                    val date = Date(timestamp.toMillis())
                    val newPath = parentFolder.resolve(filename + "_backup_" + fileDateFormat.format(date) + extension)
                    Files.move(filepath, newPath)
                }
            } catch (e: IOException) {
                throw java.lang.IllegalStateException("Failed to rename old file. Reason: " + e.message, e)
            }
            return filepath
        }

        private fun normalize(s: String): String {
            return s.lowercase().replace("\\W+".toRegex(), "_")
        }
    }
}
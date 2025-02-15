package net.backstube.structuresaver.structuresaveritem

import net.backstube.structuresaver.BoundingBox
import net.backstube.structuresaver.Exporter
import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.components.SaverItemData
import net.backstube.structuresaver.components.StructureSaverComponents
import net.minecraft.client.item.TooltipType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtIo
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class StructureSaverItem(settings: Settings?) : Item(settings) {


    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        val pos = context!!.blockPos;
        val player: PlayerEntity? = context.player

        if (!context.world.isClient && player != null && player.isSneaking) {
            val stack: ItemStack = context.stack

            val component = stack.get(StructureSaverComponents.SAVER_ITEM_COMPONENT);
            if (component?.position1 == null) {
                stack.set(
                    StructureSaverComponents.SAVER_ITEM_COMPONENT,
                    SaverItemData(pos, component?.position2, component?.canSave ?: false)
                )
                player.sendMessage(
                    Text.translatable("structuresaver.structure_saver.pos", 1, pos.x, pos.y, pos.z),
                    false
                )
                return ActionResult.SUCCESS
            }
            if (component.position2 == null) {
                stack.set(
                    StructureSaverComponents.SAVER_ITEM_COMPONENT,
                    SaverItemData(component.position1, pos, component.canSave)
                )
                player.sendMessage(
                    Text.translatable("structuresaver.structure_saver.pos", 2, pos.x, pos.y, pos.z),
                    false
                )
                return ActionResult.SUCCESS
            }
        }
        return ActionResult.PASS
    }

    override fun use(world: World?, player: PlayerEntity?, hand: Hand?): TypedActionResult<ItemStack> {
        val stack: ItemStack = player!!.mainHandStack
        val component = stack.get(StructureSaverComponents.SAVER_ITEM_COMPONENT);

        if (component?.position1 != null && component.position2 != null) {
            // prevent instant save
            if (!component.canSave) {
                stack.set(
                    StructureSaverComponents.SAVER_ITEM_COMPONENT,
                    SaverItemData(component.position1, component.position2, true)
                )
                return TypedActionResult.pass(stack)
            }
            if (world!!.isClient) {
                //val serverPlayer = world.server!!.playerManager.getPlayer(player.uuid)
                //if (serverPlayer != null)
                //MessageSender.openScreen(serverPlayer, stack)
                StructureSaver.getClientHooks().openStructureSaverScreen(stack)
            }
            return TypedActionResult.success(stack)
        }

        return TypedActionResult.pass(stack)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        context: TooltipContext?,
        tooltip: MutableList<Text>?,
        type: TooltipType?
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        if (tooltip == null)
            return

        val component = stack?.get(StructureSaverComponents.SAVER_ITEM_COMPONENT);
        val pos1 = component?.position1;
        if(pos1!= null){
            tooltip.add(
                Text.translatable(
                    "structuresaver.item.structure_saver.position.tooltip",
                    1,
                    pos1.x,
                    pos1.y,
                    pos1.z
                )
            )
        }
        val pos2 = component?.position2;
        if(pos2!= null){
            tooltip.add(
                Text.translatable(
                    "structuresaver.item.structure_saver.position.tooltip",
                    2,
                    pos2.x,
                    pos2.y,
                    pos2.z
                )
            )
        }
        if (component?.canSave == true) {
            tooltip.add(TOOLTIP_SAVE)
        } else {
            tooltip.add(TOOLTIP_INFO)
        }
    }

    companion object {
        private val TOOLTIP_INFO: Text = Text.translatable("structuresaver.item.structure_saver.info.tooltip")
        private val TOOLTIP_SAVE: Text = Text.translatable("structuresaver.item.structure_saver.save.tooltip")

        fun getArea(stack: ItemStack): BoundingBox? {
            val component = stack.get(StructureSaverComponents.SAVER_ITEM_COMPONENT);
            val pos1 = component?.position1
            val pos2 = component?.position2
            if (pos1 == null || pos2 == null) {
                return null
            }
            val minX = min(pos1.x, pos2.x)
            val minY = min(pos1.y, pos2.y)
            val minZ = min(pos1.z, pos2.z)
            val maxX = max(pos1.x, pos2.x)
            val maxY = max(pos1.y, pos2.y)
            val maxZ = max(pos1.z, pos2.z)
            return BoundingBox(BlockPos(minX, minY, minZ), BlockPos(maxX, maxY, maxZ))
        }

        private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        private fun makeSpaceForFileAndGetPath(parentFolder: Path, name: String?, asSnbt: Boolean): Path {
            var extension = if (asSnbt) ".snbt" else ".nbt";
            if (name != null && (name.endsWith(".nbt") || name.endsWith(".snbt")))
                extension = "";

            val filename = (if (name == null) "template" else normalize(name));
            val filepath = parentFolder.resolve(filename + extension)

            try {
                if (Files.exists(filepath)) {
                    val attrs = Files.readAttributes(filepath, BasicFileAttributes::class.java)
                    val timestamp = attrs.creationTime();
                    val date = Date(timestamp.toMillis());
                    val newPath = parentFolder.resolve(filename + "_backup_" + fileDateFormat.format(date) + extension)
                    Files.move(filepath, newPath);
                }
            } catch (e: IOException) {
                throw java.lang.IllegalStateException("Failed to rename old file. Reason: " + e.message, e)
            }
            return filepath
        }

        private fun normalize(s: String): String {
            return s.lowercase().replace("\\W+".toRegex(), "_")
        }

        fun writeTemplate(exportPath: Path, name: String?, template: NbtCompound?, asSnbt: Boolean): Path {
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
            return path;
        }

        fun saveSchematic(
            world: World,
            stack: ItemStack,
            includeEntities: Boolean,
            ignoreAir: Boolean,
            saveOnServer: Boolean,
            name: String?
        ): String? {
            val boundingBox = getArea(stack) ?: return null
            val origin = BlockPos(boundingBox.origin.x, boundingBox.origin.y, boundingBox.origin.z)
            val bounds = BlockPos(boundingBox.span.x + 1, boundingBox.span.y + 1, boundingBox.span.z + 1)
            val dimensions = bounds.subtract(origin)
            val exportPath = Exporter.export(
                world, name, origin, dimensions,
                includeEntities, ignoreAir, saveOnServer, false
            )
            return exportPath?.fileName.toString()
        }

        fun removeTags(stack: ItemStack): ItemStack {
            stack.remove(StructureSaverComponents.SAVER_ITEM_COMPONENT)
            return stack
        }
    }
}
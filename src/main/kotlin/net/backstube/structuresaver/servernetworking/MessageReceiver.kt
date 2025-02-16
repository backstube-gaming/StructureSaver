package net.backstube.structuresaver.servernetworking

import net.backstube.structuresaver.Exporter
import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.networking.*
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.backstube.structuresaver.structureloader.StructureLoaderBlockEntity
import net.backstube.structuresaver.structuresaveritem.StructureSaverItem
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.structure.StructurePlacementData
import net.minecraft.structure.StructureTemplate
import net.minecraft.text.Text
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import java.util.*

object MessageReceiver {
    fun setupReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(DeleteTagsPayload.ID) { payload, context ->
            if(payload?.itemStack == null)
                return@registerGlobalReceiver
            context.server().execute {
                val stackWithoutTags = StructureSaverItem.removeTags(payload.itemStack)
                context.player().inventory.setStack(context.player().inventory.selectedSlot, stackWithoutTags)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(
            SaveStructurePayload.ID
        ) { payload, context ->
            if(payload?.itemStack == null)
                return@registerGlobalReceiver

            if (!context.player().isCreativeLevelTwoOp) {
                context.player().sendMessage(Text.literal("Error: You need permission level 2"))
                return@registerGlobalReceiver
            }

            if (payload.saveOnServer && !context.player().hasPermissionLevel(4)) {
                context.player().sendMessage(Text.literal("Error: You need permission level 4"))
                return@registerGlobalReceiver
            }

            context.server().execute {
                val savedName = StructureSaverItem.saveSchematic(
                    context.player().world,
                    payload.itemStack,
                    payload.includeEntities,
                    payload.ignoreAir,
                    payload.saveOnServer,
                    payload.name
                )
                if (savedName == null) {
                    context.player().sendMessage(Text.literal("Failed to save, look at latest.log for more information"), false)
                    return@execute
                }
                val stackWithoutTags = StructureSaverItem.removeTags(payload.itemStack)
                context.player().inventory.setStack(context.player().inventory.selectedSlot, stackWithoutTags)
                context.player().sendMessage(Text.translatable("structuresaver.schematic.saved", payload.name), true)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(
            UpdateExtendedStructureBlockPayload.ID
        ) { payload, context ->
            if(payload?.pos == null)
                return@registerGlobalReceiver

            if (!context.player().isCreativeLevelTwoOp) {
                context.player().sendMessage(Text.literal("Error: You need permission level 2"))
                return@registerGlobalReceiver
            }
            if (payload.saveOnServer && !context.player().hasPermissionLevel(4)) {
                context.player().sendMessage(Text.literal("Error: You need permission level 4 to save on the server"))
                return@registerGlobalReceiver
            }

            context.player().sendMessage(Text.literal("Starting export ..."))

            context.server().execute {
                val blockEntity = context.player().serverWorld.getBlockEntity(payload.pos)
                if (blockEntity !is ExtendedStructureBlockEntity)
                    return@execute

                blockEntity.data.name = payload.text
                blockEntity.data.offset = payload.offset
                blockEntity.data.size = payload.size
                blockEntity.data.shouldIncludeEntities = payload.includeEntities
                blockEntity.data.shouldIgnoreAir = payload.ignoreAir
                blockEntity.data.shouldSaveOnServer = payload.saveOnServer
                blockEntity.markDirty()
                val dimensions =
                    Vec3i(Math.round(payload.size.x), Math.round(payload.size.y), Math.round(payload.size.z))

                if (payload.actionName == StructureBlockBlockEntity.Action.SAVE_AREA.name) {
                    val exportPath = Exporter.export(
                        context.player().world, payload.text, payload.pos.add(payload.offset), dimensions,
                        payload.includeEntities, payload.ignoreAir, payload.saveOnServer, false
                    )
                    context.player().sendMessage(Text.literal("Saved to '$exportPath'"))
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(
            UpdateStructureLoaderBlockPayload.ID
        ) { payload, context ->
            if(payload?.data?.pos == null)
                return@registerGlobalReceiver

            val blockPos = payload.data.pos
            if (!context.player().isCreativeLevelTwoOp) {
                context.player().sendMessage(Text.literal("Error: You need permission level 2"))
                return@registerGlobalReceiver
            }

            context.server().execute {
                val blockEntity = context.player().serverWorld.getBlockEntity(blockPos)
                if (blockEntity !is StructureLoaderBlockEntity)
                    return@execute

                blockEntity.data = payload.data
                blockEntity.markDirty()

                if (payload.actionName == StructureBlockBlockEntity.Action.LOAD_AREA.name) {
                    context.player().sendMessage(Text.literal("Started loading structure ..."))
                    val rotation: BlockRotation
                    when (payload.data.direction) {
                        0 -> { // east-south
                            rotation = BlockRotation.NONE
                        }

                        1 -> { // west-south
                            rotation = BlockRotation.CLOCKWISE_90
                        }

                        2 -> { // west-north
                            rotation = BlockRotation.CLOCKWISE_180
                        }

                        else -> { // east-north
                            rotation = BlockRotation.COUNTERCLOCKWISE_90
                        }
                    }


                    val structureId = Identifier(payload.data.name)
                    val placementData = StructurePlacementData()
                        .setPosition(BlockPos(0, 0, 0))
                        .setPlaceFluids(true)
                        .setIgnoreEntities(!payload.data.shouldIncludeEntities)
                        .setUpdateNeighbors(false)
                        .setInitializeMobs(true)
                        .setRotation(rotation)

                    context.player().sendMessage(Text.literal("Preparing to place structure $structureId at $blockPos ... (the server will not respond until finished)"))
                    val structure: Optional<StructureTemplate> = context.player().serverWorld.structureTemplateManager
                        .getTemplate(structureId)
                    if (structure.isEmpty) {
                        context.player().sendMessage(Text.literal("Structure $structureId not found or empty"))
                        StructureSaver.logger.error("Structure '{}' not found or empty", structureId)
                        return@execute
                    } else {
                        StructureSaver.logger.info("Structure '{}' template read successfully", structureId)
                    }
                    context.player().sendMessage(Text.literal("Placing structure $structureId at $blockPos ... (the server will not respond until finished)"))
                    val structureResult = structure.get()
                    val wasPlaced = structureResult.place(
                        context.player().serverWorld,
                        BlockPos(blockEntity.pos.x, blockEntity.pos.y + 1, blockEntity.pos.z),
                        BlockPos(0, 0, 0),
                        placementData,
                        StructureSaver.random,
                        Block.NOTIFY_NEIGHBORS
                    )

                    if (wasPlaced) {
                        context.player().sendMessage(Text.literal("Structure $structureId was placed at $blockPos"))
                        context.player().sendMessage(Text.literal("You might need to reload to see all changed blocks."))
                        StructureSaver.logger.info("Structure {} was placed at {}", structureId, blockPos)
                    } else {
                        context.player().sendMessage(Text.literal("Could not place structure $structureId"))
                        StructureSaver.logger.error("Could not place structure {}", structureId)
                    }
                }
            }
        }
    }
}
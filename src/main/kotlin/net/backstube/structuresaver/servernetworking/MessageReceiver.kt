package net.backstube.structuresaver.servernetworking

import net.backstube.structuresaver.Exporter
import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.networking.Packets
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.backstube.structuresaver.structureloader.StructureLoaderBlockEntity
import net.backstube.structuresaver.structuresaveritem.StructureSaverItem
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.structure.StructurePlacementData
import net.minecraft.structure.StructureTemplate
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import java.util.*

object MessageReceiver {
    fun setupReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
            Packets.C2S_DELETE_TAGS
        ) { server: MinecraftServer, player: ServerPlayerEntity, _: ServerPlayNetworkHandler?, buf: PacketByteBuf, _: PacketSender? ->
            val stack = buf.readItemStack()
            server.execute {
                val stackWithoutTags = StructureSaverItem.removeTags(stack)
                //TODO: check
                player.inventory.setStack(player.inventory.selectedSlot, stackWithoutTags);
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(
            Packets.C2S_SAVE_STRUCTURE
        ) { server: MinecraftServer, player: ServerPlayerEntity, _: ServerPlayNetworkHandler?, buf: PacketByteBuf, _: PacketSender? ->
            if (!player.isCreativeLevelTwoOp) {
                player.sendMessage(Text.literal("Error: You need permission level 2"))
                return@registerGlobalReceiver
            }
            val stack = buf.readItemStack()
            val name = buf.readString()
            val includeEntities = buf.readBoolean()
            val ignoreAir = buf.readBoolean()
            val saveOnServer = buf.readBoolean()
            if (saveOnServer && !player.hasPermissionLevel(4)) {
                player.sendMessage(Text.literal("Error: You need permission level 4"))
                return@registerGlobalReceiver
            }

            server.execute {
                val savedName = StructureSaverItem.saveSchematic(
                    player.world,
                    stack,
                    includeEntities,
                    ignoreAir,
                    saveOnServer,
                    name
                )
                if (savedName == null) {
                    player.sendMessage(Text.literal("Failed to save, look at latest.log for more information"), false)
                    return@execute
                }
                val stackWithoutTags = StructureSaverItem.removeTags(stack)
                player.inventory.setStack(player.inventory.selectedSlot, stackWithoutTags);
                player.sendMessage(Text.translatable("structuresaver.schematic.saved", name), true)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(
            Packets.C2S_UPDATE_EXTENDED_STRUCTURE_BLOCK
        ) { server: MinecraftServer, player: ServerPlayerEntity, _: ServerPlayNetworkHandler?, buf: PacketByteBuf, _: PacketSender? ->
            if (!player.isCreativeLevelTwoOp) {
                player.sendMessage(Text.literal("Error: You need permission level 2"))
                return@registerGlobalReceiver;
            }
            val action = buf.readString()
            val blockPos = buf.readBlockPos()
            val text = buf.readString()
            val offset = buf.readBlockPos()
            val size = buf.readVec3d()
            val shouldIncludeEntities = buf.readBoolean()
            val shouldIgnoreAir = buf.readBoolean()
            val shouldSaveOnServer = buf.readBoolean()
            if (shouldSaveOnServer && !player.hasPermissionLevel(4)) {
                player.sendMessage(Text.literal("Error: You need permission level 4 to save on the server"))
                return@registerGlobalReceiver;
            }

            server.execute {
                val blockEntity = player.serverWorld.getBlockEntity(blockPos)
                if (blockEntity !is ExtendedStructureBlockEntity)
                    return@execute

                blockEntity.data.name = text
                blockEntity.data.offset = offset
                blockEntity.data.size =
                    Vec3i(Math.round(size.x).toInt(), Math.round(size.y).toInt(), Math.round(size.z).toInt())
                blockEntity.data.shouldIncludeEntities = shouldIncludeEntities
                blockEntity.data.shouldIgnoreAir = shouldIgnoreAir
                blockEntity.data.shouldSaveOnServer = shouldSaveOnServer
                blockEntity.markDirty()
                val dimensions =
                    Vec3i(blockEntity.data.size.x, blockEntity.data.size.y, blockEntity.data.size.z)

                if (action == StructureBlockBlockEntity.Action.SAVE_AREA.name) {
                    val exportPath = Exporter.export(
                        player.world, text, blockPos.add(offset), dimensions,
                        shouldIncludeEntities, shouldIgnoreAir, shouldSaveOnServer, false
                    )
                    player.sendMessage(Text.literal("Saved to '$exportPath'"))
                }
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(
            Packets.C2S_UPDATE_STRUCTURE_LOADER_BLOCK
        ) { server: MinecraftServer, player: ServerPlayerEntity, _: ServerPlayNetworkHandler?, buf: PacketByteBuf, _: PacketSender? ->
            if (!player.isCreativeLevelTwoOp) {
                player.sendMessage(Text.literal("Error: You need permission level 2"))
                return@registerGlobalReceiver;
            }
            val action = buf.readString()
            val blockPos = buf.readBlockPos()
            val text = buf.readString()
            val shouldIncludeEntities = buf.readBoolean()

            server.execute {
                val blockEntity = player.serverWorld.getBlockEntity(blockPos)
                if (blockEntity !is StructureLoaderBlockEntity)
                    return@execute

                blockEntity.data.name = text
                blockEntity.data.shouldIncludeEntities = shouldIncludeEntities
                blockEntity.markDirty()

                if (action == StructureBlockBlockEntity.Action.LOAD_AREA.name) {
                    val structureId = Identifier(blockEntity.data.name)
                    val placementData = StructurePlacementData()
                        .setPosition(BlockPos(0,0,0))
                        .setPlaceFluids(true)
                        .setIgnoreEntities(!blockEntity.data.shouldIncludeEntities)
                        .setUpdateNeighbors(false)
                        .setInitializeMobs(true)

                    player.sendMessage(Text.literal("Preparing to place structure $structureId at $blockPos ... (the server will not respond until finished)"))
                    val structure: Optional<StructureTemplate> = player.serverWorld.structureTemplateManager
                        .getTemplate(structureId)
                    if (structure.isEmpty) {
                        player.sendMessage(Text.literal("Structure $structureId not found or empty"))
                        StructureSaver.logger.error("Structure '{}' not found or empty", structureId)
                        return@execute;
                    }else{
                        StructureSaver.logger.info("Structure '{}' template read successfully", structureId)
                    }
                    player.sendMessage(Text.literal("Placing structure $structureId at $blockPos ... (the server will not respond until finished)"))
                    val structureResult = structure.get()
                    val wasPlaced = structureResult.place(
                        player.serverWorld, BlockPos(blockEntity.pos.x, blockEntity.pos.y, blockEntity.pos.z), BlockPos(0, 0, 0),
                        placementData, StructureSaver.random, Block.NOTIFY_NEIGHBORS
                    )

                    if (wasPlaced) {
                        player.sendMessage(Text.literal("Structure $structureId was placed at $blockPos"))
                        StructureSaver.logger.info("Structure {} was placed at {}", structureId, blockPos)
                    }else {
                        player.sendMessage(Text.literal("Could not place structure $structureId"))
                        StructureSaver.logger.error("Could not place structure {}", structureId)
                    }
                }
            }
        }
    }
}
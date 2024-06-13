package net.backstube.structuresaver.servernetworking

import net.backstube.structuresaver.Exporter
import net.backstube.structuresaver.structuresaveritem.StructureSaverItem
import net.backstube.structuresaver.networking.Packets
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockEntity
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3i

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
    }
}
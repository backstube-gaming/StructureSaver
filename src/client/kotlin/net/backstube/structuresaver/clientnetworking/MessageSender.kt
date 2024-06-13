package net.backstube.structuresaver.clientnetworking

import io.netty.buffer.Unpooled
import net.backstube.structuresaver.networking.Packets
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object MessageSender {
    fun deleteTags(stack: ItemStack?) {
        val passedData = PacketByteBuf(Unpooled.buffer())
        passedData.writeItemStack(stack)
        ClientPlayNetworking.send(Packets.C2S_DELETE_TAGS, passedData)
    }

    fun saveStructure(stack: ItemStack?, name: String?, includeEntities: Boolean, ignoreAir: Boolean, saveOnServer: Boolean) {
        val passedData = PacketByteBuf(Unpooled.buffer())
        passedData.writeItemStack(stack)
        passedData.writeString(name)
        passedData.writeBoolean(includeEntities)
        passedData.writeBoolean(ignoreAir)
        passedData.writeBoolean(saveOnServer)
        ClientPlayNetworking.send(Packets.C2S_SAVE_STRUCTURE, passedData)

    }

    fun updateExtendedStructureBlock(action: StructureBlockBlockEntity.Action,
                                     pos: BlockPos?,
                                     text: String?,
                                     offset: BlockPos,
                                     size: Vec3d,
                                     shouldIncludeEntities: Boolean,
                                     shouldIgnoreAir: Boolean,
                                     shouldSaveOnServer: Boolean) {
        val passedData = PacketByteBuf(Unpooled.buffer())
        passedData.writeString(action.name)
        passedData.writeBlockPos(pos)
        passedData.writeString(text)
        passedData.writeBlockPos(offset)
        passedData.writeVec3d(size)
        passedData.writeBoolean(shouldIncludeEntities)
        passedData.writeBoolean(shouldIgnoreAir)
        passedData.writeBoolean(shouldSaveOnServer)
        ClientPlayNetworking.send(Packets.C2S_UPDATE_EXTENDED_STRUCTURE_BLOCK, passedData)
    }
}
package net.backstube.structuresaver.networking

import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.structureloader.StructureLoaderData
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class UpdateStructureLoaderBlockPayload(
    val actionName: String,
    val data: StructureLoaderData
) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val PACKET_ID: Identifier = Identifier(StructureSaver.MODID, "update_structure_loader_block")
        val ID: CustomPayload.Id<UpdateStructureLoaderBlockPayload?> = CustomPayload.Id(PACKET_ID)

        val CODEC: PacketCodec<in RegistryByteBuf, UpdateStructureLoaderBlockPayload?> = PacketCodec.tuple(
            PacketCodecs.STRING, UpdateStructureLoaderBlockPayload::actionName,
            StructureLoaderData.PACKET_CODEC, UpdateStructureLoaderBlockPayload::data,
            ::UpdateStructureLoaderBlockPayload
        )
    }
}
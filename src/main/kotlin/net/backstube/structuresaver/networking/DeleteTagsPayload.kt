package net.backstube.structuresaver.networking

import net.backstube.structuresaver.StructureSaver
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class DeleteTagsPayload(val itemStack: ItemStack?) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val PACKET_ID: Identifier = Identifier(StructureSaver.MODID, "delete_tags")
        val ID: CustomPayload.Id<DeleteTagsPayload?> = CustomPayload.Id(PACKET_ID)

        val CODEC: PacketCodec<in RegistryByteBuf, DeleteTagsPayload?> = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, DeleteTagsPayload::itemStack, ::DeleteTagsPayload)
    }
}
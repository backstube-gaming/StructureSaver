package net.backstube.structuresaver.networking

import net.backstube.structuresaver.StructureSaver
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class SaveStructurePayload(
    val itemStack: ItemStack?,
    val name: String,
    val includeEntities: Boolean,
    val ignoreAir: Boolean,
    val saveOnServer: Boolean,
) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val PACKET_ID: Identifier = Identifier(StructureSaver.MODID, "save_structure")
        val ID: CustomPayload.Id<SaveStructurePayload?> = CustomPayload.Id(PACKET_ID)

        val CODEC: PacketCodec<in RegistryByteBuf, SaveStructurePayload?> = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, SaveStructurePayload::itemStack,
            PacketCodecs.STRING, SaveStructurePayload::name,
            PacketCodecs.BOOL, SaveStructurePayload::includeEntities,
            PacketCodecs.BOOL, SaveStructurePayload::ignoreAir,
            PacketCodecs.BOOL, SaveStructurePayload::saveOnServer,
            ::SaveStructurePayload
        )
    }
}
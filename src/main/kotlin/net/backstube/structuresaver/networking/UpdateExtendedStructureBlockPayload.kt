package net.backstube.structuresaver.networking

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.Vector3fCodec
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.joml.Vector3f

@JvmRecord
data class UpdateExtendedStructureBlockPayload(
    val actionName: String,
    val pos: BlockPos?,
    val text: String,
    val offset: BlockPos,
    val size: Vector3f,
    val includeEntities: Boolean,
    val ignoreAir: Boolean,
    val saveOnServer: Boolean,
) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val PACKET_ID: Identifier = Identifier(StructureSaver.MODID, "update_structure_export_block")
        val ID: CustomPayload.Id<UpdateExtendedStructureBlockPayload?> = CustomPayload.Id(PACKET_ID)

        var CODEC: Codec<UpdateExtendedStructureBlockPayload> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.STRING.fieldOf("actionName").forGetter(UpdateExtendedStructureBlockPayload::actionName),
                BlockPos.CODEC.fieldOf("pos").forGetter(UpdateExtendedStructureBlockPayload::pos),
                Codec.STRING.fieldOf("text").forGetter(UpdateExtendedStructureBlockPayload::text),
                BlockPos.CODEC.fieldOf("offset").forGetter(UpdateExtendedStructureBlockPayload::offset),
                Vector3fCodec.CODEC.fieldOf("size").forGetter(UpdateExtendedStructureBlockPayload::size),
                Codec.BOOL.fieldOf("shouldIncludeEntities").forGetter(UpdateExtendedStructureBlockPayload::includeEntities),
                Codec.BOOL.fieldOf("shouldIgnoreAir").forGetter(UpdateExtendedStructureBlockPayload::ignoreAir),
                Codec.BOOL.fieldOf("shouldSaveOnServer").forGetter(UpdateExtendedStructureBlockPayload::saveOnServer),
            ).apply(i, ::UpdateExtendedStructureBlockPayload)
        }

        val PACKET_CODEC: PacketCodec<in RegistryByteBuf, UpdateExtendedStructureBlockPayload?> = PacketCodecs.codec(CODEC)
    }
}
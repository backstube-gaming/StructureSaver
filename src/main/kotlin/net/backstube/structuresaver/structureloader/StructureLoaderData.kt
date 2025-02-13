package net.backstube.structuresaver.structureloader

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.math.BlockPos

data class StructureLoaderData(
    var pos: BlockPos,
    var name: String,
    var shouldIncludeEntities: Boolean,
    var direction: Int
){
    companion object {
        var CODEC: Codec<StructureLoaderData> = RecordCodecBuilder.create { i ->
            i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(StructureLoaderData::pos),
                Codec.STRING.fieldOf("name").forGetter(StructureLoaderData::name),
                Codec.BOOL.fieldOf("shouldIncludeEntities").forGetter(StructureLoaderData::shouldIncludeEntities),
                Codec.INT.fieldOf("direction").forGetter(StructureLoaderData::direction),
            ).apply(i, ::StructureLoaderData)
        }

        val PACKET_CODEC: PacketCodec<ByteBuf, StructureLoaderData> = PacketCodecs.codec(CODEC)
    }
}
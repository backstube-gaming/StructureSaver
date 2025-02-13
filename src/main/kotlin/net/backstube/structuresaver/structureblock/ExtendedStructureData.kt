package net.backstube.structuresaver.structureblock

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.backstube.structuresaver.Vector3fCodec
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.math.BlockPos
import org.joml.Vector3f

data class ExtendedStructureData(
    var pos: BlockPos,
    var name: String,
    var offset: BlockPos,
    var size: Vector3f,
    var shouldIncludeEntities: Boolean,
    var shouldIgnoreAir: Boolean,
    var shouldSaveOnServer: Boolean,
    var showBoundingBox: Boolean,
) {
    companion object {

        var CODEC: Codec<ExtendedStructureData> = RecordCodecBuilder.create { i ->
            i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(ExtendedStructureData::pos),
                Codec.STRING.fieldOf("name").forGetter(ExtendedStructureData::name),
                BlockPos.CODEC.fieldOf("offset").forGetter(ExtendedStructureData::offset),
                Vector3fCodec.CODEC.fieldOf("size").forGetter(ExtendedStructureData::size),
                Codec.BOOL.fieldOf("shouldIncludeEntities").forGetter(ExtendedStructureData::shouldIncludeEntities),
                Codec.BOOL.fieldOf("shouldIgnoreAir").forGetter(ExtendedStructureData::shouldIgnoreAir),
                Codec.BOOL.fieldOf("shouldSaveOnServer").forGetter(ExtendedStructureData::shouldSaveOnServer),
                Codec.BOOL.fieldOf("showBoundingBox").forGetter(ExtendedStructureData::showBoundingBox),
            ).apply(i, ::ExtendedStructureData)
        }

        val PACKET_CODEC: PacketCodec<ByteBuf, ExtendedStructureData> = PacketCodecs.codec(CODEC)
    }
}
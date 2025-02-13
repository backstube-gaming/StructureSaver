package net.backstube.structuresaver

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.joml.Vector3f

class Vector3fCodec {
    companion object {
        var CODEC: Codec<Vector3f> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.FLOAT.fieldOf("x").forGetter { v -> v.x() },
                Codec.FLOAT.fieldOf("y").forGetter { v -> v.y() },
                Codec.FLOAT.fieldOf("z").forGetter { v -> v.z() },
            ).apply(i, ::Vector3f)
        }
    }
}
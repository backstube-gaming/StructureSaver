package net.backstube.structuresaver.structuresaveritem

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos

data class SaverItemData(var position1: BlockPos?, var position2: BlockPos?, var canSave: Boolean) {
    companion object{
        var CODEC: Codec<SaverItemData> = RecordCodecBuilder.create { i ->
            i.group(
                BlockPos.CODEC.fieldOf("position1").forGetter(SaverItemData::position1),
                BlockPos.CODEC.fieldOf("position2").forGetter(SaverItemData::position2),
                Codec.BOOL.fieldOf("canSave").forGetter(SaverItemData::canSave),
            ).apply(i, ::SaverItemData)
        }
    }
}
package net.backstube.structuresaver

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import org.slf4j.Logger
import java.io.InputStream
import java.io.InputStreamReader

object CodecHelper {
    fun <T> decodeJson(codec: Codec<T>, stream: InputStream, logPrefix: String?, logId: String?, logger: Logger): T? {
        try {
            InputStreamReader(stream).use { reader ->
                val json = JsonParser.parseReader(reader)
                val result = codec.decode(JsonOps.INSTANCE, json)
                if (result.isError || result.result().isEmpty) {
                    if (result.error().isPresent) {
                        logger.error(
                            "{} - Error occurred while decoding json '{}' - {}",
                            logPrefix,
                            logId,
                            result.error().get().message()
                        )
                    } else {
                        logger.error("{} - Unknown error occurred while decoding json '{}'", logPrefix, logId)
                    }
                    return null
                }
                logger.info("{} - Successfully decoded '{}'", logPrefix, logId)
                return result.result().get().first
            }
        } catch (e: Exception) {
            logger.error("{} - Reader Error occurred while loading resource json {}", logPrefix, logId, e)
            return null
        }
    }

    fun <T> decodeNbt(codec: Codec<T>, nbt: NbtElement, logPrefix: String?, logId: String?, logger: Logger): T? {
        try {
            val result = codec.decode(NbtOps.INSTANCE, nbt)
            if (result.isError || result.result().isEmpty) {
                if (result.error().isPresent) {
                    logger.error(
                        "{} - Error occurred while decoding nbt '{}' - {}",
                        logPrefix,
                        logId,
                        result.error().get().message()
                    )
                } else {
                    logger.error("{} - Unknown error occurred while decoding nbt '{}'", logPrefix, logId)
                }
                return null
            }
            return result.result().get().first
        } catch (e: Exception) {
            logger.error("{} - Unknown Error occurred while loading nbt {}", logPrefix, logId, e)
            return null
        }
    }

    fun <T> encodeNbt(
        codec: Codec<T>,
        dataComponent: T,
        logPrefix: String?,
        logId: String?,
        logger: Logger
    ): NbtElement? {
        try {
            val result = codec.encodeStart(NbtOps.INSTANCE, dataComponent)
            if (result.isError || result.result().isEmpty) {
                if (result.error().isPresent) {
                    logger.error(
                        "{} - Error occurred while encoding nbt '{}' - {}",
                        logPrefix,
                        logId,
                        result.error().get().message()
                    )
                } else {
                    logger.error("{} - Unknown error occurred while encoding nbt '{}'", logPrefix, logId)
                }
                return null
            }
            return result.result().get()
        } catch (e: Exception) {
            logger.error("{} - Unknown Error occurred while encoding nbt {}", logPrefix, logId, e)
            return null
        }
    }
}
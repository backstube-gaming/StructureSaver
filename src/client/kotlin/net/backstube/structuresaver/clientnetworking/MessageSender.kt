package net.backstube.structuresaver.clientnetworking

import net.backstube.structuresaver.networking.*
import net.backstube.structuresaver.structureloader.StructureLoaderData
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import org.joml.Vector3f

object MessageSender {
    fun deleteTags(stack: ItemStack?) {
        ClientPlayNetworking.send(DeleteTagsPayload(stack))
    }

    fun saveStructure(
        stack: ItemStack?,
        name: String,
        includeEntities: Boolean,
        ignoreAir: Boolean,
        saveOnServer: Boolean
    ) {
        ClientPlayNetworking.send(SaveStructurePayload(stack, name, includeEntities, ignoreAir, saveOnServer))
    }

    fun updateExtendedStructureBlock(
        action: StructureBlockBlockEntity.Action,
        pos: BlockPos?,
        text: String,
        offset: BlockPos,
        size: Vector3f,
        shouldIncludeEntities: Boolean,
        shouldIgnoreAir: Boolean,
        shouldSaveOnServer: Boolean
    ) {
        ClientPlayNetworking.send(
            UpdateExtendedStructureBlockPayload(
                action.name,
                pos,
                text,
                offset,
                size,
                shouldIncludeEntities,
                shouldIgnoreAir,
                shouldSaveOnServer
            )
        )
    }

    fun updateStructureLoaderBlock(
        action: StructureBlockBlockEntity.Action,
        data: StructureLoaderData
    ) {
        ClientPlayNetworking.send(UpdateStructureLoaderBlockPayload(action.name, data))
    }
}
package net.backstube.structuresaver.networking

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object Packets {

    fun register(){
        PayloadTypeRegistry.playC2S().register(DeleteTagsPayload.ID, DeleteTagsPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(SaveStructurePayload.ID, SaveStructurePayload.CODEC)
        PayloadTypeRegistry.playC2S().register(UpdateExtendedStructureBlockPayload.ID, UpdateExtendedStructureBlockPayload.PACKET_CODEC)
        PayloadTypeRegistry.playC2S().register(UpdateStructureLoaderBlockPayload.ID, UpdateStructureLoaderBlockPayload.CODEC)
    }

    //public val S2C_OPEN_SCREEN = Identifier(StructureSaver.MODID, "open_screen")
    //public val S2C_INIT_EXTENDED_STRUCTURE_BLOCK = Identifier(StructureSaver.MODID, "init_structure_export_block")
}
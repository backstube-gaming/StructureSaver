package net.backstube.structuresaver.networking

import net.backstube.structuresaver.StructureSaver
import net.minecraft.util.Identifier

object Packets {
    public val C2S_DELETE_TAGS = Identifier(StructureSaver.MODID, "delete_tags")
    public val C2S_SAVE_STRUCTURE = Identifier(StructureSaver.MODID, "save_structure")
    public val C2S_UPDATE_EXTENDED_STRUCTURE_BLOCK = Identifier(StructureSaver.MODID, "update_extended_structure_block")

    public val S2C_OPEN_SCREEN = Identifier(StructureSaver.MODID, "open_screen")
    public val S2C_INIT_EXTENDED_STRUCTURE_BLOCK = Identifier(StructureSaver.MODID, "init_extended_structure_block")
}
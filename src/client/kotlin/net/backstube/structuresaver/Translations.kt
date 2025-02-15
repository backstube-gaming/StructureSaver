package net.backstube.structuresaver

import net.minecraft.text.Text

class Translations {
    companion object {
        val INCLUDE_ENTITIES_TEXT: Text = Text.translatable("structuresaver.item.structure_saver.include_entities.text")
        val INCLUDE_ENTITIES_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.include_entities.tooltip")
        val IGNORE_AIR_TEXT = Text.translatable("structuresaver.item.structure_saver.ignore_air.text")
        val IGNORE_AIR_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.ignore_air.tooltip")
        val SAVE_ON_SERVER_TEXT = Text.translatable("structuresaver.item.structure_saver.save_on_server.desc")
        val SAVE_ON_SERVER_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.save_on_server.tooltip")
    }
}
package net.backstube.structuresaver

import net.minecraft.text.Text

final class Translations {
    companion object {
        public val INCLUDE_ENTITIES_TEXT: Text = Text.translatable("structuresaver.item.structure_saver.include_entities.text")
        public val INCLUDE_ENTITIES_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.include_entities.tooltip")
        public val IGNORE_AIR_TEXT = Text.translatable("structuresaver.item.structure_saver.ignore_air.text")
        public val IGNORE_AIR_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.ignore_air.tooltip")
        public val SNBT_TEXT = Text.translatable("structuresaver.item.structure_saver.nbt_to_snbt.desc")
        public val SNBT_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.nbt_to_snbt.tooltip")
        public val SAVE_ON_SERVER_TEXT = Text.translatable("structuresaver.item.structure_saver.save_on_server.desc")
        public val SAVE_ON_SERVER_TOOLTIP = Text.translatable("structuresaver.item.structure_saver.save_on_server.tooltip")
    }
}
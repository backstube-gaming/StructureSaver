package net.backstube.structuresaver

import net.minecraft.text.Style
import net.minecraft.text.TextColor


class TextStyles {
    companion object{
        val gray = Style.EMPTY.withColor(TextColor.fromRgb(0x999999))
        val red = Style.EMPTY.withColor(TextColor.fromRgb(0xAA4839)).withBold(true)
        val green = Style.EMPTY.withColor(TextColor.fromRgb(0x2A7E43))
    }
}
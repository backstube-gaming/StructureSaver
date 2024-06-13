package net.backstube.structuresaver

import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Util
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ClientUtility {
    companion object {

        fun openPath(path: Path) {
            try {
                Files.createDirectories(path)
                Util.getOperatingSystem().open(path.toUri())
            } catch (e: IOException) {
                MinecraftClient.getInstance().player!!.sendMessage(
                        Text.translatable("structuresaver.screen.open_folder.error", path), false)
            }
        }
    }
}
package net.backstube.structuresaver.screens

import com.mojang.blaze3d.systems.RenderSystem
import net.backstube.structuresaver.ClientUtility
import net.backstube.structuresaver.Exporter
import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.Translations
import net.backstube.structuresaver.clientnetworking.MessageSender
import net.backstube.structuresaver.structuresaveritem.StructureSaverItem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.CheckboxWidget
import net.minecraft.client.gui.widget.EditBoxWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class StructureSaverScreen(private val stack: ItemStack, title: Text?) : Screen(title) {
    private val xSize = 174
    private val ySize = 180
    private var relX = 0
    private var relY = 0
    private var name: EditBoxWidget? = null
    private var includeEntities: CheckboxWidget? = null
    private var ignoreAir: CheckboxWidget? = null
    private var saveOnServer: CheckboxWidget? = null

    public override fun init() {
        relX = (this.width - xSize) / 2
        relY = (this.height - ySize) / 2
        name = EditBoxWidget(
            this.textRenderer, relX + 11, relY + 25, 125, 17,
            Text.translatable("structuresaver.screen.widget.structure_name"),
            Text.translatable("structuresaver.screen.widget.structure_name")
        )
        name!!.setMaxLength(Int.MAX_VALUE)
        name!!.isFocused = true
        name!!.text = name!!.text // move cursor to the end
        this.addDrawableChild(name)

        val openFolderButton = ButtonWidget.builder(
            Text.literal("...")
        ) { _ -> ClientUtility.openPath(Exporter.EXPORT_PATH) }
            .position(relX + 144, relY + 23)
            .size(20, 20)
            .tooltip(Tooltip.of(Text.translatable("structuresaver.screen.button.open_folder.tooltip")))
            .build()
        this.addDrawableChild(openFolderButton)

        val saveButton = ButtonWidget.builder(Text.translatable("structuresaver.screen.button.save")) { _ ->
            MessageSender.saveStructure(
                stack, name!!.text.ifEmpty { "template" },
                includeEntities?.isChecked ?: true,
                ignoreAir?.isChecked ?: true,
                saveOnServer?.isChecked ?: false
            )
            this.clearItem(stack)
            this.close()
        }.position(relX + 10, relY + 55)
            .size(60, 20)
            .build()
        this.addDrawableChild(saveButton)

        val deleteButton = ButtonWidget.builder(Text.translatable("structuresaver.screen.button.delete")) { _ ->
            MessageSender.deleteTags(stack)
            this.close()
        }.position(relX + 77, relY + 55)
            .size(60, 20)
            .build()
        this.addDrawableChild(deleteButton)

        val includeEntities = CheckboxWidget.builder(Translations.INCLUDE_ENTITIES_TEXT, this.textRenderer)
            .pos(relX + 10, relY + 100)
            .tooltip(Tooltip.of(Translations.INCLUDE_ENTITIES_TOOLTIP))
            .checked(true)
            .build()
        this.includeEntities = this.addDrawableChild(includeEntities)

        val ignoreAir = CheckboxWidget.builder(Translations.IGNORE_AIR_TEXT, this.textRenderer)
            .pos(relX + 10, relY + 120)
            .tooltip(Tooltip.of(Translations.IGNORE_AIR_TOOLTIP))
            .checked(true)
            .build()
        this.ignoreAir = this.addDrawableChild(ignoreAir)

        /*val saveOnServer = CheckboxWidget.builder(Translations.SAVE_ON_SERVER_TEXT, this.textRenderer)
            .pos(relX + 10, relY + 140)
            .tooltip(Tooltip.of(Translations.SAVE_ON_SERVER_TOOLTIP))
            .checked(true)
            .build()*/
        // this.saveOnServer = this.addDrawableChild(saveOnServer) -> not implemented yet

        this.addDrawableChild(TextWidget(relX, relY + 8, 100, 15, this.title, textRenderer))
    }

    override fun renderInGameBackground(context: DrawContext?) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        context?.drawTexture(SCREEN_TEXTURE, relX, relY, 0, 0, xSize, ySize)
    }

    override fun getFocused(): Element? {
        return name
    }

    private fun clearItem(stack: ItemStack){
        if(!stack.isOf(StructureSaver.Entries.StructureSaverItem))
            return
        StructureSaverItem.removeTags(stack)
        MessageSender.deleteTags(stack)
    }

    companion object {
        private val SCREEN_TEXTURE = Identifier(StructureSaver.MODID, "textures/gui/structure_saver.png")
    }
}

package net.backstube.structuresaver.screens

import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.Translations
import net.backstube.structuresaver.clientnetworking.MessageSender
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockScreenHandler
import net.backstube.structuresaver.structureloader.LoaderScreenHandler
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.CheckboxWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class StructureLoaderScreen(private val handler: LoaderScreenHandler) : ScreenHandlerProvider<LoaderScreenHandler>,
    Screen(Text.translatable(StructureSaver.Entries.StructureLoaderBlock.translationKey)) {

    companion object {
        private val STRUCTURE_NAME_TEXT: Text = Text.translatable("structure_block.structure_name")
    }

    private var inputName: TextFieldWidget? = null
    private var buttonPlace: ButtonWidget? = null
    private var checkboxIncludeEntities: CheckboxWidget? = null
    private val decimalFormat: DecimalFormat = DecimalFormat("0.0###")

    init {
        decimalFormat.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ROOT)
    }


    private fun done() {
        if (this.updateStructureBlock(StructureBlockBlockEntity.Action.UPDATE_DATA)) {
            client!!.setScreen(null as Screen?)
        }
    }

    private fun cancel() {
        client!!.setScreen(null as Screen?)
    }

    override fun init() {

        val includeEntities = CheckboxWidget.builder(Translations.INCLUDE_ENTITIES_TEXT, this.textRenderer)
            .pos(this.width / 2 - 153, 150)
            .tooltip(Tooltip.of(Translations.INCLUDE_ENTITIES_TOOLTIP))
            .checked(handler.data.shouldIncludeEntities)
            .build()
        this.checkboxIncludeEntities = this.addDrawableChild(includeEntities)

        this.buttonPlace = addDrawableChild(
            ButtonWidget.builder(
                Text.translatable("structuresaver.structure_loader_block.place")
            ) { _: ButtonWidget? ->
                this.updateStructureBlock(StructureBlockBlockEntity.Action.LOAD_AREA)
                client!!.setScreen(null as Screen?)
            }.dimensions(this.width / 2 - 153, 174, 60, 20).build()
        )

        this.addDrawableChild(
            ButtonWidget.builder(
                ScreenTexts.DONE
            ) { _: ButtonWidget? ->
                this.done()
            }.dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build()
        )
        this.addDrawableChild(
            ButtonWidget.builder(
                ScreenTexts.CANCEL
            ) { _: ButtonWidget? ->
                this.cancel()
            }.dimensions(this.width / 2 + 4, 210, 150, 20).build()
        )

        this.inputName = object : TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 152,
            40,
            300,
            20,
            Text.translatable("structure_block.structure_name")
        ) {
            override fun charTyped(chr: Char, modifiers: Int): Boolean {
                return if (!this@StructureLoaderScreen.isValidCharacterForName(
                        this.text,
                        chr,
                        this.cursor
                    )
                ) false else super.charTyped(chr, modifiers)
            }
        }
        inputName?.setMaxLength(128)
        inputName?.text = handler.data.name
        this.addSelectableChild(this.inputName)
        this.setInitialFocus(this.inputName)
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val string = inputName!!.text
        this.init(client, width, height)
        inputName!!.text = string
    }

    private fun updateStructureBlock(action: StructureBlockBlockEntity.Action): Boolean {
        handler.data.name = inputName!!.text
        handler.data.shouldIncludeEntities = checkboxIncludeEntities?.isChecked ?: handler.data.shouldIncludeEntities
        handler.saveToBlockEntity()
        MessageSender.updateStructureLoaderBlock(
            action,
            handler.data.pos,
            handler.data.name,
            handler.data.shouldIncludeEntities
        );
        return true
    }

    override fun close() {
        this.cancel()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true
        } else if (keyCode != 257 && keyCode != 335) {
            return false
        } else {
            this.done()
            return true
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 16777215)
        context.drawTextWithShadow(
            this.textRenderer, STRUCTURE_NAME_TEXT,
            this.width / 2 - 153, 30, 10526880
        )
        inputName!!.render(context, mouseX, mouseY, delta)

        val explanationLines = Text.translatable("structuresaver.structure_loader_block.explanation").string.split("\n");
        for((index, line) in explanationLines.withIndex()){
            context.drawTextWithShadow(
                this.textRenderer, Text.literal(line),
                this.width / 2 - 85, 174 + index * 10, 10526880
            )
        }
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun getScreenHandler(): LoaderScreenHandler {
        return handler
    }
}
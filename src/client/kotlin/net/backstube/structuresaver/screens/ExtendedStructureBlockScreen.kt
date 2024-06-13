package net.backstube.structuresaver.screens

import net.backstube.structuresaver.StructureSaver
import net.backstube.structuresaver.Translations
import net.backstube.structuresaver.clientnetworking.MessageSender
import net.backstube.structuresaver.structureblock.ExtendedStructureBlockScreenHandler
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

class ExtendedStructureBlockScreen(private val handler: ExtendedStructureBlockScreenHandler) : ScreenHandlerProvider<ExtendedStructureBlockScreenHandler>,
    Screen(Text.translatable(StructureSaver.Entries.ExtendedStructureBlock.translationKey)) {

    companion object {
        private val STRUCTURE_NAME_TEXT: Text = Text.translatable("structure_block.structure_name")
        private val POSITION_TEXT: Text = Text.translatable("structure_block.position")
        private val SIZE_TEXT: Text = Text.translatable("structure_block.size")
    }

    private var inputName: TextFieldWidget? = null
    private var inputPosX: TextFieldWidget? = null
    private var inputPosY: TextFieldWidget? = null
    private var inputPosZ: TextFieldWidget? = null
    private var inputSizeX: TextFieldWidget? = null
    private var inputSizeY: TextFieldWidget? = null
    private var inputSizeZ: TextFieldWidget? = null
    private var buttonExport: ButtonWidget? = null
    private var checkboxIncludeEntities: CheckboxWidget? = null
    private var checkboxIgnoreAir: CheckboxWidget? = null
    private var checkBoxSaveOnServer: CheckboxWidget? = null
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

        val ignoreAir = CheckboxWidget.builder(Translations.IGNORE_AIR_TEXT, this.textRenderer)
            .pos(this.width / 2 - 40, 150)
            .tooltip(Tooltip.of(Translations.IGNORE_AIR_TOOLTIP))
            .checked(handler.data.shouldIgnoreAir)
            .build()
        this.checkboxIgnoreAir = this.addDrawableChild(ignoreAir)

        val saveOnServer = CheckboxWidget.builder(Translations.SAVE_ON_SERVER_TEXT, this.textRenderer)
            .pos(width / 2 + 100, 150)
            .tooltip(Tooltip.of(Translations.SAVE_ON_SERVER_TOOLTIP))
            .checked(handler.data.shouldSaveOnServer)
            .build()
        // this.checkBoxSaveOnServer = this.addDrawableChild(saveOnServer) -> not implemented yet

        this.buttonExport = addDrawableChild(
            ButtonWidget.builder(
                Text.translatable("structuresaver.structure_export_block.export")
            ) { _: ButtonWidget? ->
                this.updateStructureBlock(StructureBlockBlockEntity.Action.SAVE_AREA)
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
                return if (!this@ExtendedStructureBlockScreen.isValidCharacterForName(
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
        val blockPos = handler.data.offset
        this.inputPosX = TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 152,
            80,
            80,
            20,
            Text.translatable("structure_block.position.x")
        )
        inputPosX!!.setMaxLength(15)
        inputPosX!!.text = blockPos.x.toString()
        this.addSelectableChild(this.inputPosX)
        this.inputPosY = TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 72,
            80,
            80,
            20,
            Text.translatable("structure_block.position.y")
        )
        inputPosY!!.setMaxLength(15)
        inputPosY!!.text = blockPos.y.toString()
        this.addSelectableChild(this.inputPosY)
        this.inputPosZ = TextFieldWidget(
            this.textRenderer,
            this.width / 2 + 8,
            80,
            80,
            20,
            Text.translatable("structure_block.position.z")
        )
        inputPosZ!!.setMaxLength(15)
        inputPosZ!!.text = blockPos.z.toString()
        this.addSelectableChild(this.inputPosZ)
        val vec3i = handler.data.size
        this.inputSizeX = TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 152,
            120,
            80,
            20,
            Text.translatable("structure_block.size.x")
        )
        inputSizeX!!.setMaxLength(15)
        inputSizeX!!.text = vec3i.x.toString()
        this.addSelectableChild(this.inputSizeX)
        this.inputSizeY = TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 72,
            120,
            80,
            20,
            Text.translatable("structure_block.size.y")
        )
        inputSizeY!!.setMaxLength(15)
        inputSizeY!!.text = vec3i.y.toString()
        this.addSelectableChild(this.inputSizeY)
        this.inputSizeZ = TextFieldWidget(
            this.textRenderer,
            this.width / 2 + 8,
            120,
            80,
            20,
            Text.translatable("structure_block.size.z")
        )
        inputSizeZ!!.setMaxLength(15)
        inputSizeZ!!.text = vec3i.z.toString()
        this.addSelectableChild(this.inputSizeZ)

        this.setInitialFocus(this.inputName)
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        val string = inputName!!.text
        val string2 = inputPosX!!.text
        val string3 = inputPosY!!.text
        val string4 = inputPosZ!!.text
        val string5 = inputSizeX!!.text
        val string6 = inputSizeY!!.text
        val string7 = inputSizeZ!!.text
        this.init(client, width, height)
        inputName!!.text = string
        inputPosX!!.text = string2
        inputPosY!!.text = string3
        inputPosZ!!.text = string4
        inputSizeX!!.text = string5
        inputSizeY!!.text = string6
        inputSizeZ!!.text = string7
    }

    private fun updateStructureBlock(action: StructureBlockBlockEntity.Action): Boolean {
        val offset = BlockPos(
            this.parseInt(inputPosX!!.text),
            this.parseInt(inputPosY!!.text),
            this.parseInt(inputPosZ!!.text)
        )
        val sizeAsDouble = Vec3d(
            this.parseDouble(inputSizeX!!.text),
            this.parseDouble(inputSizeY!!.text),
            this.parseDouble(inputSizeZ!!.text)
        )
        handler.data.name = inputName!!.text
        handler.data.offset = offset
        handler.data.size = Vec3i(sizeAsDouble.x.toInt(), sizeAsDouble.y.toInt(), sizeAsDouble.z.toInt())
        handler.data.shouldIncludeEntities = checkboxIncludeEntities?.isChecked ?: handler.data.shouldIncludeEntities
        handler.data.shouldIgnoreAir = checkboxIgnoreAir?.isChecked ?: handler.data.shouldIgnoreAir
        handler.data.shouldSaveOnServer = checkBoxSaveOnServer?.isChecked ?: handler.data.shouldSaveOnServer
        handler.saveToBlockEntity()
        MessageSender.updateExtendedStructureBlock(
            action,
            handler.data.pos,
            handler.data.name,
            handler.data.offset,
            sizeAsDouble,
            handler.data.shouldIncludeEntities,
            handler.data.shouldIgnoreAir,
            handler.data.shouldSaveOnServer
        );
        return true
    }

    private fun parseInt(string: String): Int {
        return try {
            string.toInt()
        } catch (var3: NumberFormatException) {
            0
        }
    }

    private fun parseDouble(string: String): Double {
        return try {
            string.toDouble()
        } catch (var3: NumberFormatException) {
            0.0
        }
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

        context.drawTextWithShadow(
            this.textRenderer, POSITION_TEXT,
            this.width / 2 - 153, 70, 10526880
        )
        inputPosX!!.render(context, mouseX, mouseY, delta)
        inputPosY!!.render(context, mouseX, mouseY, delta)
        inputPosZ!!.render(context, mouseX, mouseY, delta)
        context.drawTextWithShadow(
            this.textRenderer, SIZE_TEXT,
            this.width / 2 - 153, 110, 10526880
        )
        inputSizeX!!.render(context, mouseX, mouseY, delta)
        inputSizeY!!.render(context, mouseX, mouseY, delta)
        inputSizeZ!!.render(context, mouseX, mouseY, delta)

        val explanationLines = Text.translatable("structuresaver.structure_export_block.explanation").string.split("\n");
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

    override fun getScreenHandler(): ExtendedStructureBlockScreenHandler {
        return handler
    }
}
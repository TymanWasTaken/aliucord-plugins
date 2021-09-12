package tech.tyman.plugins.texter

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.CommandContext
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption

@AliucordPlugin
class Texter : Plugin() {
    override fun start(context: Context) {
        registerConverterCommand(
                "small",
                "Turns your text into small letters", emptyList()
        ) { ctx: CommandContext -> getResult(Maps.smallLetters, ctx.getString("text")!!) }
        registerConverterCommand(
                "smaller",
                "Turns your text into smaller letters", emptyList()
        ) { ctx: CommandContext -> getResult(Maps.smallerLetters, ctx.getString("text")!!) }
        registerConverterCommand(
                "fullwidth",
                "Turns your text into full width letters", listOf(
                "fw"
        )
        ) { ctx: CommandContext -> getResult(Maps.fullWidthLetters, ctx.getString("text")!!) }
        registerConverterCommand(
                "emoji",
                "Turns your text into emoji letters",
                listOf(
                        "emojify",
                        "blockify"
                )
        ) { ctx: CommandContext -> getResult(Maps.emojiLetters, ctx.getString("text")!!) }
        registerConverterCommand(
                "flip",
                "Flips your text upside down"
        ) { ctx: CommandContext -> getResult(Maps.flippedLetters, ctx.getString("text")!!.reverse()) }
        registerConverterCommand(
                "clap",
                "Adds clapping icons to your text"
        ) { ctx: CommandContext -> getResult(ctx.getString("text")!!.clapify()) }
        registerConverterCommand(
                "reverse",
                "Reverses your text"
        ) { ctx: CommandContext -> getResult(ctx.getString("text")!!.reverse()) }
        registerConverterCommand(
                "space",
                "Spaces out your text"
        ) { ctx: CommandContext -> getResult(ctx.getString("text")!!.chunked(1).joinToString(" ")) }
        registerConverterCommand(
                "mock",
                "Capitalizes random parts of your text"
        ) { ctx: CommandContext -> getResult(ctx.getString("text")!!.mock()) }
        registerConverterCommand(
                "leet",
                "Makes your text 1337 style", listOf(
                "leetify"
        )
        ) { ctx: CommandContext -> getResult(Maps.leetLetters, ctx.getString("text")!!) }
    }

    private fun registerConverterCommand(name: String, description: String, execute: (CommandContext) -> CommandsAPI.CommandResult) {
        commands.registerCommand(
                name,
                description, listOf(
                ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to change", null, true, true, null, null)
        ),
                execute
        )
    }
    private fun registerConverterCommand(name: String, description: String, aliases: List<String>, execute: (CommandContext) -> CommandsAPI.CommandResult) {
        commands.registerCommand(
                name,
                description, listOf(
                ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to change", null, true, true, null, null)
        ),
                execute
        )
        for (alias in aliases) {
            commands.registerCommand(
                    alias,
                    description, listOf(
                    ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to change", null, true, true, null, null)
            ),
                    execute
            )
        }
    }

    override fun stop(context: Context) {
        commands.unregisterAll()
    }

    private fun getResult(map: Map<String, String>, textToMap: String): CommandsAPI.CommandResult {
        return CommandsAPI.CommandResult(Maps.getMappedString(map, textToMap), null, true)
    }

    private fun getResult(textToSend: String): CommandsAPI.CommandResult {
        return CommandsAPI.CommandResult(textToSend, null, true)
    }
}
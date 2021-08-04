package com.aliucord.plugins;

import android.content.Context;
import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.CommandContext;
import com.aliucord.entities.Plugin;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.models.commands.ApplicationCommandOption;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
public class Texter extends Plugin {
    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{new Manifest.Author("Tyman", 487443883127472129L)};
        manifest.description = "A port of Texter for powercord to Aliucord";
        manifest.version = "0.0.2";
        manifest.updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json";
        return manifest;
    }


    @Override
    public void start(Context context) {
        registerConverterCommand(
                "small",
                "Turns your text into small letters",
                Collections.emptyList(),
                ctx -> getResult(Maps.smallLetters, ctx.getString("text"))
        );
        registerConverterCommand(
                "smaller",
                "Turns your text into smaller letters",
                Collections.emptyList(),
                ctx -> getResult(Maps.smallerLetters, ctx.getString("text"))
        );
        registerConverterCommand(
                "fullwidth",
                "Turns your text into smaller letters",
                Collections.singletonList(
                        "fw"
                ),
                ctx -> getResult(Maps.fullWidthLetters, ctx.getString("text"))
        );
        registerConverterCommand(
                "emoji",
                "Turns your text into emoji letters",
                Arrays.asList(
                        "emojify",
                        "blockify"
                ),
                ctx -> getResult(Maps.emojiLetters, ctx.getString("text"))
        );
    }

    public void registerConverterCommand(String name, String description, List<String> aliases, Function1<CommandContext, CommandsAPI.CommandResult> execute) {
        commands.registerCommand(
                name,
                description,
                Collections.singletonList(
                        new ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to change", null, true, true, null, null)
                ),
                execute
        );
        for (String alias : aliases) {
            commands.registerCommand(
                    alias,
                    description,
                    Collections.singletonList(
                            new ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to change", null, true, true, null, null)
                    ),
                    execute
            );
        }
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
    }

    public CommandsAPI.CommandResult getResult(String map, String textToMap) {
        return new CommandsAPI.CommandResult(Maps.getMappedString(map, textToMap), null, true);
    }
}

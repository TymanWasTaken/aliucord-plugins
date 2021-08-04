package com.aliucord.plugins;

// Import several packages such as Aliucord's CommandApi and the Plugin class
import android.content.Context;
import androidx.annotation.NonNull;

import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.models.commands.ApplicationCommandOption;

import java.util.Collections;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
public class Texter extends Plugin {
    @NonNull
    @Override
    // Plugin Manifest - Required
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{new Manifest.Author("Tyman", 487443883127472129L)};
        manifest.description = "A port of Texter for powercord to Aliucord";
        manifest.version = "1.0.0";
        manifest.updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json?token=AHZF3HFNLTMCX3OZXO6JNW3A5TL4Y";
        return manifest;
    }


    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {
        // Registers a command with the name hello, the description "Say hello to the world" and no options
        commands.registerCommand(
                "small",
                "Turns your text into small letters",
                Collections.singletonList(
                        new ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to make small", null, true, true, null, null)
                ),
                ctx -> getResult(Maps.smallLetters, ctx.getString("text"))
        );

        commands.registerCommand(
                "smaller",
                "Turns your text into smaller letters",
                Collections.singletonList(
                        new ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to make small", null, true, true, null, null)
                ),
                ctx -> getResult(Maps.smallerLetters, ctx.getString("text"))
        );
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Unregisters all commands
        commands.unregisterAll();
    }

    public CommandsAPI.CommandResult getResult(String map, String textToMap) {
        return new CommandsAPI.CommandResult(Maps.getMappedString(map, textToMap), null, true);
    }
}

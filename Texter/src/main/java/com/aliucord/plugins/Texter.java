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
                // Return a command result with Hello World! as the content, no embeds and send set to false
                args -> {
                    String text = (String) args.get("text");
                    String[] split = text.split("(?!^)");
                    StringBuilder newText = new StringBuilder();
                    for (String character : split) {
                        newText.append(Maps.getMappedChar(Maps.smallLetters, character));
                    }
                    return new CommandsAPI.CommandResult(newText.toString(), null, true);
                }
        );
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Unregisters all commands
        commands.unregisterAll();
    }
}

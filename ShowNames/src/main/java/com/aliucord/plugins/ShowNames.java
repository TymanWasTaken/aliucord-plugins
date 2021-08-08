package com.aliucord.plugins;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PinePatchFn;
import com.discord.models.member.GuildMember;
import com.discord.stores.StoreStream;

@SuppressWarnings("unused")
public class ShowNames extends Plugin {

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{new Manifest.Author("Tyman", 487443883127472129L)};
        manifest.description = "A plugin that changes the color of usernames to stop them from blending into the background.";
        manifest.version = "1.0.2";
        manifest.updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json";
        try {
            manifest.changelog = "# Version 1.0.0\nInitial release\n# Version 1.0.1\nFixed changing color when on amoled mode and user has no role color";
        } catch (NoSuchMethodError ignored) {}
        return manifest;
    }


    @Override
    public void start(Context context) {
        var className = "com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage";
        var methodName = "getAuthorTextColor";
        var methodArguments = new Class<?>[] { GuildMember.class };
        patcher.patch(className, methodName, methodArguments, new PinePatchFn(callFrame -> {
            var member = (GuildMember) callFrame.args[0];
            var color = member.getColor();
            var theme = StoreStream.getUserSettingsSystem().getTheme();
            if (color == -16777216) { // Default (no role) color
                return;
            }
            var colorBrightness = ColorUtils.calculateLuminance(color);
            if (colorBrightness < 0.01 && theme.equals("pureEvil")) { // pureEvil = AMOLED
                callFrame.setResult(0xFF333333);
            } else if (colorBrightness > 0.99 && theme.equals("light")) {
                callFrame.setResult(0xFFDEDEDE);
            } else if (colorBrightness > 0.035 && colorBrightness < 0.045 && theme.equals("dark")) { // not sure about these numbers but they work ¯\_(ツ)_/¯
                callFrame.setResult(0xFF4E535A);
            }
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}

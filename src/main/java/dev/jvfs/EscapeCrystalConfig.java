package dev.jvfs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("escape_crystal")
public interface EscapeCrystalConfig extends Config {
    @ConfigItem(
            keyName = "highlightAutoTele",
            name = "Highlight auto-tele crystal",
            description = "Highlight the escape crystal when the auto-tele function is activated"
    )
    default boolean highlightAutoTele() {
        return true;
    }
}

package dev.jvfs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("escape_crystal")
public interface EscapeCrystalConfig extends Config {
    String GROUP = "escapeCrystal";
    String AUTO_TELE_STATUS_KEY = "autoTeleStatus";

    @ConfigItem(
            keyName = "highlightAutoTele",
            name = "Highlight auto-tele crystal",
            description = "Highlight the escape crystal when the auto-tele function is activated"
    )
    default boolean highlightAutoTele() {
        return true;
    }

    @ConfigItem(
            keyName = "autoTeleActiveText",
            name = "Auto-tele active text",
            description = "Text to display by the escape crystal when auto-tele is active"
    )
    default String autoTeleActiveText() {
        return "A";
    }

    @ConfigItem(
            keyName = "autoTeleStatusColor",
            name = "Auto-tele active color",
            description = "The color of the overlay when auto-tele is active"
    )
    default Color autoTeleStatusColor() {
        return Color.GREEN;
    }
}

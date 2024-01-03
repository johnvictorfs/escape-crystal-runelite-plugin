package dev.jvfs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("escape_crystal")
public interface EscapeCrystalConfig extends Config {
    String GROUP = "escapeCrystal";
    String AUTO_TELE_STATUS_KEY = "autoTeleStatus";
    String AUTO_TELE_TIMER_KEY = "autoTeleTimer";

    @ConfigItem(
            keyName = "highlightAutoTele",
            name = "Highlight auto-tele crystal",
            description = "Highlight the escape crystal when the auto-tele function is activated",
            position = 1
    )
    default boolean highlightAutoTele() {
        return true;
    }

    @ConfigItem(
            keyName = "autoTeleActiveText",
            name = "Auto-tele active text",
            description = "Text to display by the escape crystal when auto-tele is active",
            position = 2
    )
    default String autoTeleActiveText() {
        return "Active";
    }

    @ConfigItem(
            keyName = "autoTeleStatusColor",
            name = "Auto-tele active color",
            description = "The color of the overlay when auto-tele is active",
            position = 3
    )
    default Color autoTeleStatusColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "autoTeleTimer",
            name = "Auto-tele timer",
            description = "Display a infbox with a timer for when auto-tele is gonna trigger",
            position = 4
    )
    default boolean autoTeleTimer() {
        return true;
    }
}

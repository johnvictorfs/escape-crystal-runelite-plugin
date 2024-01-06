package dev.jvfs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

@ConfigGroup("escape_crystal")
public interface EscapeCrystalConfig extends Config {
    String GROUP = "escapeCrystal";
    String AUTO_TELE_STATUS_KEY = "autoTeleStatus";
    String AUTO_TELE_TIMER_KEY = "autoTeleTimer";

    @ConfigSection(
            name = "Auto-tele Crystal Overlay",
            description = "Configuration the Escape Crystal Overlay",
            position = 1
    )
    String crystalOverlaySection = "crystalOverlaySection";

    enum CrystalHighlightOverlay {
        ACTIVE,
        INACTIVE,
        BOTH,
        NEVER
    }

    @ConfigItem(
            keyName = "highlightAutoTele",
            name = "Highlight auto-tele",
            description = "Highlight the escape crystal when the auto-tele function is active/inactive",
            position = 2,
            section = crystalOverlaySection
    )
    default CrystalHighlightOverlay highlightAutoTele() {
        return CrystalHighlightOverlay.ACTIVE;
    }

    @ConfigItem(
            keyName = "autoTeleActiveText",
            name = "Active text",
            description = "Text to display by the escape crystal when auto-tele is active",
            position = 3,
            section = crystalOverlaySection
    )
    default String autoTeleActiveText() {
        return "Active";
    }

    @ConfigItem(
            keyName = "autoTeleActiveStatusColor",
            name = "Active color",
            description = "The color of the overlay when auto-tele is active",
            position = 4,
            section = crystalOverlaySection
    )
    default Color autoTeleActiveStatusColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "autoTeleInactiveText",
            name = "Inactive text",
            description = "Text to display by the escape crystal when auto-tele is inactive",
            position = 3,
            section = crystalOverlaySection
    )
    default String autoTeleInactiveText() {
        return "Inactive";
    }

    @ConfigItem(
            keyName = "autoTeleInactiveStatusColor",
            name = "Inactive color",
            description = "The color of the overlay when auto-tele is inactive",
            position = 4,
            section = crystalOverlaySection
    )
    default Color autoTeleInactiveStatusColor() {
        return Color.RED;
    }

    @ConfigSection(
            name = "Auto-tele timer",
            description = "Configuration for the auto-tele timer infobox",
            position = 5
    )
    String autoTeleTimerSection = "autoTeleTimerSection";

    @ConfigItem(
            keyName = "autoTeleTimer",
            name = "Auto-tele timer infobox",
            description = "Display an infobox with a timer for when auto-tele is gonna trigger",
            position = 6,
            section = autoTeleTimerSection
    )
    default boolean autoTeleTimer() {
        return true;
    }

    @ConfigItem(
            keyName = "autoTeleTimerAlertTime",
            name = "Infobox Alert time",
            description = "When the auto-tele timer is below this time (in seconds), the timer will turn a different color. Keep at 0 to disable.",
            position = 7,
            section = autoTeleTimerSection
    )
    default int autoTeleTimerAlertTime() {
        return 3;
    }

    @ConfigItem(
            keyName = "autoTeleTimerAlertColor",
            name = "Infobox Alert color",
            description = "The color of the timer when auto-tele is active",
            position = 8,
            section = autoTeleTimerSection
    )
    default Color autoTeleTimerAlertColor() {
        return Color.RED.brighter();
    }
}

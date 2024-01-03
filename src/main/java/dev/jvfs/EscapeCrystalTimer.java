package dev.jvfs;

import net.runelite.client.ui.overlay.infobox.Timer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class EscapeCrystalTimer extends Timer {
    private final EscapeCrystalConfig config;
    private final EscapeCrystalPlugin plugin;

    EscapeCrystalTimer(Duration duration, BufferedImage image, EscapeCrystalPlugin plugin, EscapeCrystalConfig config) {
        super(duration.toMillis(), ChronoUnit.MILLIS, image, plugin);
        this.config = config;
        this.plugin = plugin;
        setTooltip("Time until the escape crystal teleports you if you take damage");
    }

    @Override
    public Color getTextColor() {
        Duration timeLeft = Duration.between(Instant.now(), getEndTime());

        if (timeLeft.getSeconds() < 10) {
            return Color.RED.brighter();
        }

        return Color.WHITE;
    }

    @Override
    public boolean render() {
        return config.autoTeleTimer() && this.plugin.getAutoTeleStatus() == EscapeCrystalOverlay.AutoTeleStatus.ACTIVE;
    }
}

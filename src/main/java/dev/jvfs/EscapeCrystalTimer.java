package dev.jvfs;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Timer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class EscapeCrystalTimer extends Timer {
    EscapeCrystalTimer(Duration duration, BufferedImage image, Plugin plugin) {
        super(duration.toMillis(), ChronoUnit.MILLIS, image, plugin);
        setTooltip("Time until the escape crystal teleports you");
    }

    @Override
    public Color getTextColor() {
        Duration timeLeft = Duration.between(Instant.now(), getEndTime());

        if (timeLeft.getSeconds() < 10) {
            return Color.RED.brighter();
        }

        return Color.WHITE;
    }
}

package dev.jvfs;

import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;

public class EscapeCrystalTimer extends InfoBox {
    private final EscapeCrystalConfig config;
    private final EscapeCrystalPlugin plugin;


    private final Instant endTime;

    EscapeCrystalTimer(Duration duration, BufferedImage image, EscapeCrystalPlugin plugin, EscapeCrystalConfig config) {
        super(image, plugin);
        Instant startTime = Instant.now();
        endTime = startTime.plus(Duration.of(duration.toMillis(), ChronoUnit.MILLIS));

        this.config = config;
        this.plugin = plugin;
        setTooltip("Time until the escape crystal teleports you if you take damage");
    }

    @Override
    public String getText() {
        if (this.plugin.getAutoTeleStatus() == EscapeCrystalOverlay.AutoTeleStatus.INACTIVE) {
            // Using 'config.autoTeleInactiveText' could result in overflowing off the infobox
            return "Off";
        } else if (this.plugin.getAutoTeleStatus() == EscapeCrystalOverlay.AutoTeleStatus.UNKNOWN) {
            return "?";
        }


        if(!config.autoTeleTimerTicks()) {
            Duration timeLeft = Duration.between(Instant.now(), endTime);

            int seconds = (int) (timeLeft.toMillis() / 1000L);

            if (seconds <= 0) {
                return "Tele";
            }

            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;

            return String.format("%d:%02d", minutes, secs);
        }
        else {
            return String.format("%d", plugin.getAutoTeleTicks());
        }
    }

    @Override
    public Color getTextColor() {
        Duration timeLeft = Duration.between(Instant.now(), endTime);

        if (config.autoTeleTimerAlertTime() == 0) {
            return Color.WHITE;
        }

        if (timeLeft.getSeconds() < config.autoTeleTimerAlertTime()) {
            return config.autoTeleTimerAlertColor();
        }

        return Color.WHITE;
    }

    @Override
    public boolean render() {
        return config.autoTeleTimer() &&
                plugin.hasEscapeCrystal() &&
                (this.plugin.getAutoTeleStatus() == EscapeCrystalOverlay.AutoTeleStatus.ACTIVE || config.autoTeleTimerWhenInactive());
    }
}

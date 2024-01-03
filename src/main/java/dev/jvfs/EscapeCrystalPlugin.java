package dev.jvfs;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Constants;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
        name = "Escape Crystal"
)
public class EscapeCrystalPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private EscapeCrystalConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private EscapeCrystalOverlay overlay;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Getter
    private long lastIdleDuration = -1;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        removeAutoTeleTimer();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.SPAM) {
            return;
        }

        String message = Text.removeTags(event.getMessage());

        if (message.contains("Your escape crystals will now auto-activate")) {
            Pattern pattern = Pattern.compile("(\\d+) seconds");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                String seconds = matcher.group(1);
                int duration = Integer.parseInt(seconds);
                setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus.ACTIVE, duration);
            }
        } else if (message.contains("Your escape crystals will no longer auto-activate")) {
            setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus.INACTIVE, -1);
        }
    }

    private void setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus status, int duration) {
        configManager.setRSProfileConfiguration(EscapeCrystalConfig.GROUP, EscapeCrystalConfig.AUTO_TELE_STATUS_KEY, status);
        configManager.setRSProfileConfiguration(EscapeCrystalConfig.GROUP, EscapeCrystalConfig.AUTO_TELE_TIMER_KEY, duration);
    }

    private int getIdleTicks() {
        return Math.min(client.getKeyboardIdleTicks(), client.getMouseIdleTicks());
    }

    public EscapeCrystalOverlay.AutoTeleStatus getAutoTeleStatus() {
        String status = configManager.getRSProfileConfiguration(EscapeCrystalConfig.GROUP, EscapeCrystalConfig.AUTO_TELE_STATUS_KEY);

        if (status == null) {
            return EscapeCrystalOverlay.AutoTeleStatus.UNKNOWN;
        }

        switch (status) {
            case "ACTIVE":
                return EscapeCrystalOverlay.AutoTeleStatus.ACTIVE;
            case "INACTIVE":
                return EscapeCrystalOverlay.AutoTeleStatus.INACTIVE;
            default:
                return EscapeCrystalOverlay.AutoTeleStatus.UNKNOWN;
        }
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (!config.autoTeleTimer()) {
            removeAutoTeleTimer();
        }

        if (getAutoTeleStatus() == EscapeCrystalOverlay.AutoTeleStatus.INACTIVE) {
            removeAutoTeleTimer();
            return;
        }

        // Update auto-tele timer infobox when idle timer resets
        String autoTeleTimerValue = configManager.getRSProfileConfiguration(EscapeCrystalConfig.GROUP, EscapeCrystalConfig.AUTO_TELE_TIMER_KEY);

        if (autoTeleTimerValue == null || autoTeleTimerValue.equals("-1")) {
            return;
        }

        int autoTeleTimer = Integer.parseInt(autoTeleTimerValue);
        final int durationMillis = Constants.CLIENT_TICK_LENGTH * ((autoTeleTimer * 50) - getIdleTicks()) + 999;

        if (durationMillis < 0) {
            return;
        }

        if (lastIdleDuration == -1 || durationMillis < lastIdleDuration) {
            createAutoTeleTimer(Duration.ofMillis(durationMillis));
        }

        lastIdleDuration = durationMillis;
    }

    private void removeAutoTeleTimer() {
        infoBoxManager.removeIf(t -> t instanceof EscapeCrystalTimer);
    }

    private void createAutoTeleTimer(Duration duration) {
        removeAutoTeleTimer();

        if (duration.isNegative()) {
            return;
        }

        BufferedImage image = itemManager.getImage(ItemID.ESCAPE_CRYSTAL);
        infoBoxManager.addInfoBox(new EscapeCrystalTimer(duration, image, this));
    }

    @Provides
    EscapeCrystalConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EscapeCrystalConfig.class);
    }
}

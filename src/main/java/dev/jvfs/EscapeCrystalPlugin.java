package dev.jvfs;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

import java.awt.image.BufferedImage;
import java.time.Duration;
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

    @Inject
    private Notifier notifier;

    private final Pattern AUTO_TELE_UPDATE_TIMER_PATTERN = Pattern.compile("The inactivity period for auto-activation is now (\\d+)s");
    private final Pattern AUTO_TELE_STATUS_TIME_PATTERN = Pattern.compile("(\\d+) seconds");

    @Getter
    private long lastIdleDuration = -1;

    @Getter
    private boolean notifiedThisTimer = false;

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
            Matcher matcher = AUTO_TELE_STATUS_TIME_PATTERN.matcher(message);

            if (matcher.find()) {
                String seconds = matcher.group(1);
                int duration = Integer.parseInt(seconds);
                setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus.ACTIVE, duration);
            }
        } else if (message.contains("Your escape crystals will no longer auto-activate")) {
            setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus.INACTIVE, -1);
        }
    }

    public boolean hasEscapeCrystal() {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        if (inventory != null) {
            return inventory.contains(ItemID.ESCAPE_CRYSTAL);
        }

        return false;
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

        // Chatbox for confirmation when updating auto-tele period
        // The text for this widget is always null when using 'onWidgetLoaded'
        Widget widget = client.getWidget(229, 1);
        if (widget != null) {
            // "The inactivity period for auto-activation is now <duration>s."
            String widgetText = Text.removeTags(widget.getText());
            Matcher matcher = AUTO_TELE_UPDATE_TIMER_PATTERN.matcher(widgetText);

            if (matcher.find()) {
                String seconds = matcher.group(1);
                int duration = Integer.parseInt(seconds);
                EscapeCrystalOverlay.AutoTeleStatus status = getAutoTeleStatus();
                setEscapeCrystalStatus(status, duration);
            }
        }

        // Update auto-tele timer infobox when idle timer resets
        final int durationMillis = getAutoTelePeriod();

        if (lastIdleDuration == -1 || durationMillis < lastIdleDuration) {
            createAutoTeleTimer(Duration.ofMillis(durationMillis));

            if (config.autoTeleNotification() && this.hasEscapeCrystal() && !notifiedThisTimer && durationMillis <= (config.autoTeleTimerAlertTime() * 1000)) {
                if (config.autoTeleNotificationInCombat() && !this.isInCombat()) {
                    return;
                }
                notifier.notify("Escape Crystal about to trigger");
                notifiedThisTimer = true;
            }
        }

        if (lastIdleDuration < durationMillis) {
            // Timer reset, can send notification again
            notifiedThisTimer = false;
        }

        lastIdleDuration = durationMillis;
    }

    private int getAutoTelePeriod() {
        String autoTeleTimerValue = configManager.getRSProfileConfiguration(EscapeCrystalConfig.GROUP, EscapeCrystalConfig.AUTO_TELE_TIMER_KEY);

        if (autoTeleTimerValue == null || autoTeleTimerValue.equals("-1")) {
            return -1;
        }

        int autoTeleTimer = Integer.parseInt(autoTeleTimerValue);

        return Constants.CLIENT_TICK_LENGTH * ((autoTeleTimer * 50) - getIdleTicks()) + 999;
    }

    private boolean isInCombat() {
        return client.getLocalPlayer().getHealthScale() != -1;
    }

    private void removeAutoTeleTimer() {
        infoBoxManager.removeIf(t -> t instanceof EscapeCrystalTimer);
    }

    private void createAutoTeleTimer(Duration duration) {
        removeAutoTeleTimer();

        BufferedImage image = itemManager.getImage(ItemID.ESCAPE_CRYSTAL);
        infoBoxManager.addInfoBox(new EscapeCrystalTimer(duration, image, this, this.config));
    }

    @Provides
    EscapeCrystalConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EscapeCrystalConfig.class);
    }
}

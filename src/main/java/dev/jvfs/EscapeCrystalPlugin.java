package dev.jvfs;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.GameTick;
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
    private static final int GAUNTLET_REGION = 7512;
    private static final int CORRUPTED_GAUNTLET_REGION = 7768;
    private static final int GAUNTLET_LOBBY_REGION = 11870;
    private static final int ESCAPE_CRYSTAL_INACTIVITY_TICKS_VARBIT = 14849;

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

    @Getter
    private boolean inGauntletWithCrystal = false;

    @Getter
    private boolean notifiedGauntlet = false;

    @Getter
    private int autoTeleTicks = 0;

    private int clientInactivityTicks;
    private int expectedServerInactivityTicks = 0;
    private int expectedTicksUntilTeleport;


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

        if (inventory != null && inventory.contains(ItemID.ESCAPE_CRYSTAL)) {
            return true;
        }

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

        if (equipment != null && equipment.contains(ItemID.ESCAPE_CRYSTAL)) {
            return true;
        }

        return this.isInsideGauntlet() && this.inGauntletWithCrystal;
    }

    public boolean hasActiveEscapeCrystal() {
        return this.hasEscapeCrystal() && this.getAutoTeleStatus() == EscapeCrystalOverlay.AutoTeleStatus.ACTIVE;
    }

    private void checkGauntlet() {
        if (!this.inGauntletWithCrystal) {
            if (this.isInGauntletLobby()) {
                if (this.hasActiveEscapeCrystal()) {
                    // Inside the gauntlet lobby with an active escape crystal
                    this.inGauntletWithCrystal = true;
                } else if (this.config.notifyGauntletWithoutCrystal()) {
                    // Inside the Gauntlet lobby without an escape crystal (or an inactive one)
                    if (!this.notifiedGauntlet) {
                        notifier.notify("Warning: You do not have an active escape crystal with you!");
                        this.notifiedGauntlet = true;
                    }
                }
            }
        } else {
            // Outside gauntlet and lobby
            if (!this.isInGauntletLobby() && !this.isInsideGauntlet()) {
                this.inGauntletWithCrystal = false;
            }
        }

        // Refresh notification to send if entering gauntlet without crystal
        if (this.notifiedGauntlet && !this.isInGauntletLobby() && !this.isInsideGauntlet()) {
            this.notifiedGauntlet = false;
        }
    }

    private boolean isInsideGauntlet() {
        return this.client.isInInstancedRegion()
                && this.client.getMapRegions().length > 0
                && (this.client.getMapRegions()[0] == GAUNTLET_REGION
                || this.client.getMapRegions()[0] == CORRUPTED_GAUNTLET_REGION);
    }

    private boolean isInGauntletLobby() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == GAUNTLET_LOBBY_REGION;
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
    public void onGameTick(GameTick gameTick) {
        autoTeleTicks = setAutoTeleTicks();

        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        this.checkGauntlet();

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
        int durationMillis = (autoTeleTicks * 600) + 999;

        if (lastIdleDuration == -1 || durationMillis <= lastIdleDuration) {
            createAutoTeleTimer(Duration.ofMillis(durationMillis));

            if (config.autoTeleNotification() && this.hasActiveEscapeCrystal() && !notifiedThisTimer && durationMillis <= (config.autoTeleTimerAlertTime() * 1000)) {
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

    private int setAutoTeleTicks() {
        int currentClientInactivityTicks = Math.min(client.getKeyboardIdleTicks(), client.getMouseIdleTicks());

        if (currentClientInactivityTicks > this.clientInactivityTicks) {
            this.expectedServerInactivityTicks += 1;
        } else {
            this.expectedServerInactivityTicks = 0;
        }

        this.clientInactivityTicks = currentClientInactivityTicks;
        this.expectedTicksUntilTeleport = client.getVarbitValue(ESCAPE_CRYSTAL_INACTIVITY_TICKS_VARBIT) - this.expectedServerInactivityTicks;

        if (this.expectedTicksUntilTeleport < 0) {
            this.expectedTicksUntilTeleport = 0;
        }

        return this.expectedTicksUntilTeleport;
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

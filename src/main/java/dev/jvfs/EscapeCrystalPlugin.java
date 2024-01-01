package dev.jvfs;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.util.Text;

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

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.SPAM) {
            return;
        }

        String message = Text.removeTags(event.getMessage());

        if (message.contains("Your escape crystals will now auto-activate")) {
            setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus.ACTIVE);
        } else if (message.contains("Your escape crystals will no longer auto-activate")) {
            setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus.INACTIVE);
        }
    }

    private void setEscapeCrystalStatus(EscapeCrystalOverlay.AutoTeleStatus status) {
        configManager.setRSProfileConfiguration(EscapeCrystalConfig.GROUP, EscapeCrystalConfig.AUTO_TELE_STATUS_KEY, status);
    }

    @Provides
    EscapeCrystalConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EscapeCrystalConfig.class);
    }
}

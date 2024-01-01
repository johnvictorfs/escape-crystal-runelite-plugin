package dev.jvfs;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "Escape Crystal"
)
public class EscapeCrystalPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private EscapeCrystalConfig config;

    @Override
    protected void startUp() throws Exception {
        log.info("EscapeCrystal plugin started!");
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("EscapeCrystal plugin stopped!");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says ", null);
        }
    }

    @Provides
    EscapeCrystalConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EscapeCrystalConfig.class);
    }
}

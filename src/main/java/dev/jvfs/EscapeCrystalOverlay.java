package dev.jvfs;

import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import java.awt.*;

public class EscapeCrystalOverlay extends WidgetItemOverlay {
    private final EscapeCrystalConfig config;
    private final EscapeCrystalPlugin plugin;

    public enum AutoTeleStatus {
        UNKNOWN,
        ACTIVE,
        INACTIVE;
    }

    @Inject
    EscapeCrystalOverlay(EscapeCrystalConfig config, EscapeCrystalPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
        showOnInventory();
        showOnBank();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        // Highlight for escape crystal if enabled and it is active
        if (itemId != ItemID.ESCAPE_CRYSTAL) {
            return;
        }

        if (config.highlightAutoTele() == EscapeCrystalConfig.CrystalHighlightOverlay.NEVER) {
            return;
        }

        AutoTeleStatus status = plugin.getAutoTeleStatus();

        String highlightText = "?";
        Color highlightColor = Color.WHITE;

        if (status == AutoTeleStatus.INACTIVE) {
            if (config.highlightAutoTele() == EscapeCrystalConfig.CrystalHighlightOverlay.ACTIVE) {
                return;
            }

            highlightText = config.autoTeleInactiveText();
            highlightColor = config.autoTeleInactiveStatusColor();
        } else if (status == AutoTeleStatus.ACTIVE) {
            if (config.highlightAutoTele() == EscapeCrystalConfig.CrystalHighlightOverlay.INACTIVE) {
                return;
            }

            highlightText = config.autoTeleActiveText();
            highlightColor = config.autoTeleActiveStatusColor();
        }

        graphics.setFont(FontManager.getRunescapeSmallFont());
        final Rectangle bounds = widgetItem.getCanvasBounds();
        final TextComponent textComponent = new TextComponent();
        textComponent.setPosition(new Point(bounds.x - 1, bounds.y + 35));
        textComponent.setText(highlightText);
        textComponent.setColor(highlightColor);
        textComponent.render(graphics);
    }
}

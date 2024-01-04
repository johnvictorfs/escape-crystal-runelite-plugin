package dev.jvfs;

import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

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
        if (!config.highlightAutoTele() || itemId != ItemID.ESCAPE_CRYSTAL) {
            return;
        }

        AutoTeleStatus status = plugin.getAutoTeleStatus();

        if (status == AutoTeleStatus.INACTIVE) {
            return;
        }

        graphics.setFont(FontManager.getRunescapeSmallFont());
        final Rectangle bounds = widgetItem.getCanvasBounds();
        final TextComponent textComponent = new TextComponent();
        textComponent.setPosition(new Point(bounds.x - 1, bounds.y + 35));
        textComponent.setText(status == AutoTeleStatus.UNKNOWN ? "?" : config.autoTeleActiveText());
        textComponent.setColor(config.autoTeleStatusColor());
        textComponent.render(graphics);
    }
}

package base.client.feature.impl.visual.hud.impl;

import base.client.Client;
import base.client.feature.impl.player.Scaffold;
import base.client.feature.impl.visual.hud.HudElement;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.RectHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.Window;

import java.awt.Color;

/**
 * block counter HUD element for scaffold
 * shows block texture + total count with blur background and rounded corners
 */
public class BlockCounterElement extends HudElement {

    private static final Minecraft mc = Minecraft.getInstance();

    // design constants
    private static final int PADDING = 6;
    private static final int ICON_SIZE = 16;
    private static final int GAP = 4;
    private static final float CORNER_RADIUS = 8f;
    private static final int BG_ALPHA = 120;
    private static final Color BG_COLOR = new Color(30, 30, 30, BG_ALPHA);

    public BlockCounterElement() {
        super("BlockCounter", "Block counter for Scaffold");
        position.set(10, 100);
    }

    @Override
    public void draw(GuiGraphics dc, double mouseX, double mouseY) {
        // only show when scaffold is enabled
        Scaffold scaffold = (Scaffold) Client.instance.featureManager.getModuleByClass(Scaffold.class);
        if (scaffold == null || !scaffold.getState()) {
            return;
        }

        // get total blocks
        int totalBlocks = Scaffold.getTotalBlocks();

        // find first block item to render
        ItemStack blockStack = findFirstBlockStack();

        // calculate dimensions
        String countText = String.valueOf(totalBlocks);
        int textWidth = FontHelper.getStringWidth(countText);
        int textHeight = FontHelper.getLineHeight();

        int totalWidth = PADDING + ICON_SIZE + GAP + textWidth + PADDING;
        int totalHeight = PADDING + Math.max(ICON_SIZE, textHeight) + PADDING;

        // position centered above hotbar
        Window window = mc.getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        // hotbar is 182 pixels wide and positioned at bottom center
        // hearts are at ~screenHeight - 39, armor at ~screenHeight - 49
        float x = (screenWidth - totalWidth) / 2f;
        float y = screenHeight - 55 - totalHeight; // above hearts and armor

        // draw blurred background (semi-transparent rounded rect)
        RectHelper.drawRoundedRect(dc, x, y, totalWidth, totalHeight, CORNER_RADIUS, BG_COLOR.getRGB());

        // draw block icon
        if (blockStack != null && !blockStack.isEmpty()) {
            int iconX = (int) x + PADDING;
            int iconY = (int) y + (totalHeight - ICON_SIZE) / 2;
            dc.renderItem(blockStack, iconX, iconY);
        } else {
            // draw placeholder square if no blocks
            int iconX = (int) x + PADDING;
            int iconY = (int) y + (totalHeight - ICON_SIZE) / 2;
            dc.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0xFF444444);
        }

        // draw count text
        int textX = (int) x + PADDING + ICON_SIZE + GAP;
        int textY = (int) y + (totalHeight - textHeight) / 2;

        // color based on count
        int textColor;
        if (totalBlocks <= 0) {
            textColor = 0xFFFF4444; // red when empty
        } else if (totalBlocks < 32) {
            textColor = 0xFFFFAA00; // orange when low
        } else {
            textColor = 0xFFFFFFFF; // white otherwise
        }

        FontHelper.drawString(dc, countText, textX, textY, textColor, true);

        // update size for dragging
        size.set(totalWidth, totalHeight);
    }

    /**
     * find first block stack in hotbar for icon rendering
     */
    private ItemStack findFirstBlockStack() {
        if (mc.player == null)
            return null;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return stack;
            }
        }

        // check main inventory
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return stack;
            }
        }

        return null;
    }
}

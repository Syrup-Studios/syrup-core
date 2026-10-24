package net.syrupstudios.syrupcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

public final class PlayTimeBadge {
    private PlayTimeBadge() {}

    public static int width(Minecraft minecraft, String label) {
        return 16 + minecraft.font.width(label);
    }

    public static void drawStar(GuiGraphics graphics, int x, int y, boolean pinned) {
        String[] star = {"....#....", "...###...", "..#####..", "#########", ".#######.", "..#####..", "..##.##..", ".##...##.", "##.....##"};
        for (int row = 0; row < star.length; row++) {
            for (int column = 0; column < star[row].length(); column++) {
                if (star[row].charAt(column) != '#') continue;
                boolean edge = column == 0 || column == 8 || row == 0 || row == 8
                        || star[row].charAt(column - 1) != '#' || star[row].charAt(column + 1) != '#'
                        || star[row - 1].charAt(column) != '#' || star[row + 1].charAt(column) != '#';
                if (pinned || edge) graphics.fill(x + column, y + row, x + column + 1, y + row + 1,
                        edge ? (pinned ? 0xFF4A3518 : 0xFF777777) : 0xFFFFC83D);
            }
        }
    }

    public static void drawRightAligned(GuiGraphics graphics, Minecraft minecraft, int right, int y, String label) {
        Font font = minecraft.font;
        int left = right - width(minecraft, label);
        int iconX = left + 3;
        int iconY = y + 2;
        int color = 0x99AAAAAA;
        graphics.fill(iconX + 1, iconY, iconX + 6, iconY + 1, color);
        graphics.fill(iconX, iconY + 1, iconX + 1, iconY + 6, color);
        graphics.fill(iconX + 6, iconY + 1, iconX + 7, iconY + 6, color);
        graphics.fill(iconX + 1, iconY + 6, iconX + 6, iconY + 7, color);
        graphics.fill(iconX + 3, iconY + 2, iconX + 4, iconY + 5, color);
        graphics.fill(iconX + 3, iconY + 4, iconX + 6, iconY + 5, color);
        graphics.drawString(font, label, left + 12, y + 1, color, false);
    }
}

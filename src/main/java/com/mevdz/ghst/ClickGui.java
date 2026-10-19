package com.mevdz.ghst;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGui extends GuiScreen {
    private static final int W = 200;

    private final List<int[]> moduleHits = new ArrayList<>();
    private final List<Module> moduleHitMods = new ArrayList<>();
    private final List<int[]> sliderHits = new ArrayList<>();
    private final List<Setting> sliderHitSets = new ArrayList<>();
    private Setting dragging;
    private int dragX;

    @Override
    public void drawScreen(int mx, int my, float pt) {
        Gui.drawRect(0, 0, width, height, 0x66000000);
        moduleHits.clear(); moduleHitMods.clear();
        sliderHits.clear(); sliderHitSets.clear();

        int x = width / 2 - W / 2;
        int y = 20;

        Gui.drawRect(x, y, x + W, y + 24, 0xFF1B1B2B);
        drawCenteredString(fontRendererObj, "\u00a7bMevdZ GHST \u00a7fBeta", width / 2, y + 8, 0xFFFFFF);
        y += 24;

        for (Module m : ModuleManager.modules) {
            Gui.drawRect(x, y, x + W, y + 16, m.isEnabled() ? 0xFF2E7D32 : 0xFF2A2A2A);
            fontRendererObj.drawStringWithShadow(m.getName(), x + 6, y + 4, 0xFFFFFF);
            String st = m.isEnabled() ? "ON" : "OFF";
            fontRendererObj.drawStringWithShadow(st, x + W - 6 - fontRendererObj.getStringWidth(st), y + 4, 0xFFFFFF);
            moduleHits.add(new int[]{x, y, x + W, y + 16});
            moduleHitMods.add(m);
            y += 16;

            for (Setting s : m.getSettings()) {
                Gui.drawRect(x, y, x + W, y + 14, 0xFF151515);
                if (s.bool) {
                    if (s.isOn()) Gui.drawRect(x + 4, y + 1, x + W - 4, y + 13, 0xFF3F51B5);
                } else {
                    double frac = (s.get() - s.min) / (s.max - s.min);
                    Gui.drawRect(x + 4, y + 1, (int) (x + 4 + (W - 8) * frac), y + 13, 0xFF3F51B5);
                }
                fontRendererObj.drawStringWithShadow(s.display(), x + 8, y + 3, 0xFFFFFF);
                sliderHits.add(new int[]{x, y, x + W, y + 14});
                sliderHitSets.add(s);
                y += 14;
            }
        }
        super.drawScreen(mx, my, pt);
    }

    private void updateSlider(Setting s, int mx) {
        double frac = (mx - dragX) / (double) (W - 8);
        frac = Math.max(0, Math.min(1, frac));
        s.set(s.min + (s.max - s.min) * frac);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        if (btn != 0) return;

        for (int i = 0; i < sliderHits.size(); i++) {
            int[] r = sliderHits.get(i);
            if (mx >= r[0] && mx <= r[2] && my >= r[1] && my < r[3]) {
                Setting s = sliderHitSets.get(i);
                if (s.bool) { s.toggle(); }
                else { dragging = s; dragX = r[0] + 4; updateSlider(s, mx); }
                return;
            }
        }
        for (int i = 0; i < moduleHits.size(); i++) {
            int[] r = moduleHits.get(i);
            if (mx >= r[0] && mx <= r[2] && my >= r[1] && my < r[3]) {
                moduleHitMods.get(i).toggle();
                return;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (dragging != null) updateSlider(dragging, mx);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        dragging = null;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void keyTyped(char c, int code) throws IOException {
        if (code == Keyboard.KEY_RSHIFT) { mc.displayGuiScreen(null); return; }
        super.keyTyped(c, code);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

package com.mevdz.ghst;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGui extends GuiScreen {
    private static final int BLUE = 0xFF1877F2;
    private static final int ROW_H = 26, GAP = 4, SET_H = 18, COLOR_H = 38;

    private static final int K_TAB = 0, K_ROW = 1, K_DOTS = 2, K_BIND_MOD = 3,
            K_BIND_SET = 4, K_BOOL = 5, K_SLIDER = 6, K_CHANNEL = 7;

    private static class Hit {
        final int kind, x, y, w, h;
        final boolean list;
        Module mod; Setting set; int ch; Category cat;

        Hit(int kind, int x, int y, int w, int h, boolean list) {
            this.kind = kind; this.x = x; this.y = y; this.w = w; this.h = h; this.list = list;
        }
        boolean contains(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    // menu kapanip acilinca durumlar kalsin
    private static Category tab = null;      // null = hepsi
    private static float scroll = 0;
    private static final Set<Module> open = new HashSet<>();

    private final List<Hit> hits = new ArrayList<>();
    private Hit drag;
    private Module bindMod;
    private Setting bindSet;
    private int viewY, viewH, maxScroll;

    // ---------------------------------------------------------------- cizim
    private static void rrect(int x1, int y1, int x2, int y2, int c) {
        if (x2 - x1 < 3 || y2 - y1 < 3) { Gui.drawRect(x1, y1, x2, y2, c); return; }
        Gui.drawRect(x1 + 1, y1, x2 - 1, y2, c);
        Gui.drawRect(x1, y1 + 1, x2, y2 - 1, c);
    }

    private void text(String s, int x, int y, int c) { fontRendererObj.drawStringWithShadow(s, x, y, c); }

    private void textRight(String s, int xr, int y, int c) {
        fontRendererObj.drawStringWithShadow(s, xr - fontRendererObj.getStringWidth(s), y, c);
    }

    private void textCenter(String s, int cx, int y, int c) {
        fontRendererObj.drawStringWithShadow(s, cx - fontRendererObj.getStringWidth(s) / 2, y, c);
    }

    private void toggleSwitch(int x, int y, boolean on, int onColor) {
        rrect(x, y, x + 24, y + 12, on ? onColor : 0xFF3A3A42);
        rrect(x + 1, y + 1, x + 23, y + 11, on ? onColor : 0xFF1C1C21);
        Gui.drawRect(on ? x + 14 : x + 2, y + 2, on ? x + 22 : x + 10, y + 10, on ? 0xFFFFFFFF : 0xFF8A8A92);
    }

    private static String keyName(int k) { return k == Keyboard.KEY_NONE ? "NONE" : Keyboard.getKeyName(k); }

    private void scissor(int x, int y, int w, int h) {
        int f = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * f, mc.displayHeight - (y + h) * f, w * f, h * f);
    }

    private Hit addHit(int kind, int x, int y, int w, int h, boolean list) {
        Hit hit = new Hit(kind, x, y, w, h, list);
        hits.add(hit);
        return hit;
    }

    private int blockHeight(Module m) {
        int h = SET_H + 8;
        for (Setting s : m.getSettings()) h += s.type == Setting.Type.COLOR ? COLOR_H : SET_H;
        return h;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        hits.clear();
        Gui.drawRect(0, 0, width, height, 0x66000000);

        int pw = Math.min(440, width - 16), ph = Math.min(280, height - 16);
        int x0 = (width - pw) / 2, y0 = (height - ph) / 2;
        rrect(x0 - 1, y0 - 1, x0 + pw + 1, y0 + ph + 1, 0xFF2A2A30);
        rrect(x0, y0, x0 + pw, y0 + ph, 0xFF101012);

        text("\u00a7lMevdZ GHST \u00a7r\u00a7bbeta", x0 + 14, y0 + 11, 0xFFFFFF);

        // ---- sol sekmeler
        String[] tabNames = {"All", "Combat", "Visual", "Utility"};
        Category[] tabCats = {null, Category.COMBAT, Category.VISUAL, Category.UTILITY};
        for (int i = 0; i < tabNames.length; i++) {
            int ty = y0 + 42 + i * 24;
            boolean sel = tab == tabCats[i];
            text(tabNames[i], x0 + 14, ty, sel ? 0xFFFFFFFF : 0xFF8A8A92);
            if (sel) Gui.drawRect(x0 + 14, ty + 12, x0 + 30, ty + 14, BLUE);
            Hit h = addHit(K_TAB, x0 + 8, ty - 4, 80, 22, false);
            h.cat = tabCats[i];
        }
        text("\u00a78RShift: kapat", x0 + 14, y0 + ph - 16, 0xFFFFFF);

        // ---- modul listesi
        int lx = x0 + 96, lw = pw - 96 - 16;
        viewY = y0 + 32;
        viewH = ph - 32 - 10;
        scissor(lx - 2, viewY, lw + 4, viewH);

        int startY = viewY - (int) scroll;
        int y = startY;

        for (Module m : ModuleManager.modules) {
            if (tab != null && m.getCategory() != tab) continue;
            boolean exp = open.contains(m);
            boolean vis = my >= viewY && my < viewY + viewH;
            boolean hover = vis && mx >= lx && mx < lx + lw && my >= y && my < y + ROW_H;

            rrect(lx, y, lx + lw, y + ROW_H, exp ? BLUE : hover ? 0xFF242429 : 0xFF1A1A1E);
            rrect(lx, y, lx + ROW_H, y + ROW_H, exp ? 0xFF2F86FF : 0xFF212126);
            textCenter(m.getName().substring(0, 1), lx + ROW_H / 2, y + 9, m.isEnabled() ? 0xFFFFFFFF : 0xFF8A8A92);

            int nameCol = (exp || m.isEnabled()) ? 0xFFFFFFFF : 0xFF9A9AA2;
            text(m.getName(), lx + ROW_H + 10, y + 9, nameCol);
            String info = m.getInfo();
            if (!info.isEmpty()) text(info, lx + ROW_H + 100, y + 9, exp ? 0xFFD6E6FF : 0xFF6E6E78);

            toggleSwitch(lx + lw - 50, y + (ROW_H - 12) / 2, m.isEnabled(), exp ? 0xFF7DB2FF : BLUE);
            for (int d = 0; d < 3; d++)
                Gui.drawRect(lx + lw - 14, y + 7 + d * 5, lx + lw - 12, y + 9 + d * 5, exp ? 0xFFFFFFFF : 0xFF8A8A92);

            Hit dots = addHit(K_DOTS, lx + lw - 24, y, 24, ROW_H, true); dots.mod = m;
            Hit row = addHit(K_ROW, lx, y, lw - 24, ROW_H, true); row.mod = m;
            y += ROW_H;

            if (exp) {
                int bh = blockHeight(m);
                rrect(lx, y, lx + lw, y + bh, 0xFF151518);
                int ix = lx + 8, iw = lw - 16;
                int sy = y + 4;

                // keybind satiri
                text("Keybind", ix, sy + 5, 0xFFB5B5BD);
                boolean listening = bindMod == m;
                rrect(ix + iw - 60, sy + 1, ix + iw, sy + SET_H - 1, listening ? BLUE : 0xFF26262C);
                textCenter(listening ? "..." : keyName(m.getKey()), ix + iw - 30, sy + 5, 0xFFFFFFFF);
                Hit bh1 = addHit(K_BIND_MOD, ix + iw - 60, sy, 60, SET_H, true); bh1.mod = m;
                sy += SET_H;

                for (Setting s : m.getSettings()) {
                    if (s.type == Setting.Type.SLIDER) {
                        double frac = (s.get() - s.min) / (s.max - s.min);
                        rrect(ix, sy + 2, ix + iw, sy + SET_H - 2, 0xFF202025);
                        int fw = (int) (iw * frac);
                        if (fw > 0) rrect(ix, sy + 2, ix + fw, sy + SET_H - 2, BLUE);
                        text(s.name, ix + 6, sy + 5, 0xFFFFFFFF);
                        textRight(s.valueStr(), ix + iw - 6, sy + 5, 0xFFFFFFFF);
                        Hit h = addHit(K_SLIDER, ix, sy, iw, SET_H, true); h.set = s;
                        sy += SET_H;
                    } else if (s.type == Setting.Type.BOOL) {
                        text(s.name, ix + 2, sy + 5, s.isOn() ? 0xFFFFFFFF : 0xFFB5B5BD);
                        toggleSwitch(ix + iw - 24, sy + 3, s.isOn(), BLUE);
                        boolean sl = bindSet == s;
                        rrect(ix + iw - 76, sy + 2, ix + iw - 32, sy + SET_H - 2, sl ? BLUE : 0xFF26262C);
                        textCenter(sl ? "..." : keyName(s.getKey()), ix + iw - 54, sy + 5, 0xFFB5B5BD);
                        Hit kb = addHit(K_BIND_SET, ix + iw - 76, sy, 44, SET_H, true); kb.set = s;
                        Hit h = addHit(K_BOOL, ix, sy, iw - 80, SET_H, true); h.set = s;
                        Hit h2 = addHit(K_BOOL, ix + iw - 28, sy, 28, SET_H, true); h2.set = s;
                        sy += SET_H;
                    } else { // COLOR
                        text(s.name, ix + 2, sy + 15, 0xFFFFFFFF);
                        int bx = ix + 52, bw = iw - 52 - 34;
                        String[] chn = {"R", "G", "B"};
                        int[] chc = {0xFFFF5555, 0xFF55FF55, 0xFF5599FF};
                        for (int c = 0; c < 3; c++) {
                            int by = sy + 4 + c * 11;
                            rrect(bx, by, bx + bw, by + 8, 0xFF202025);
                            int fw = (int) (bw * (s.getChannel(c) / 255.0));
                            if (fw > 0) rrect(bx, by, bx + fw, by + 8, chc[c]);
                            text(chn[c], bx - 9, by, chc[c]);
                            Hit h = addHit(K_CHANNEL, bx, by - 1, bw, 10, true); h.set = s; h.ch = c;
                        }
                        rrect(ix + iw - 28, sy + 5, ix + iw, sy + 33, 0xFF000000 | s.getColor());
                        sy += COLOR_H;
                    }
                }
                y += bh;
            }
            y += GAP;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // ---- scroll cubugu
        int contentH = y - startY;
        maxScroll = Math.max(0, contentH - viewH);
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;
        if (maxScroll > 0) {
            int bx = x0 + pw - 9;
            Gui.drawRect(bx, viewY, bx + 2, viewY + viewH, 0xFF1C1C21);
            int th = Math.max(18, viewH * viewH / contentH);
            int ty = viewY + (int) ((viewH - th) * (scroll / maxScroll));
            Gui.drawRect(bx, ty, bx + 2, ty + th, 0xFF4A4A54);
        }

        super.drawScreen(mx, my, pt);
    }

    // ---------------------------------------------------------------- girdi
    private void updateDrag(int mx) {
        if (drag == null) return;
        double frac = Math.max(0, Math.min(1, (mx - drag.x) / (double) drag.w));
        if (drag.kind == K_SLIDER) drag.set.set(drag.set.min + (drag.set.max - drag.set.min) * frac);
        else if (drag.kind == K_CHANNEL) drag.set.setChannel(drag.ch, (int) Math.round(frac * 255));
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        bindMod = null; bindSet = null; // baska yere tiklayinca bind iptal

        for (Hit h : hits) {
            if (!h.contains(mx, my)) continue;
            if (h.list && (my < viewY || my >= viewY + viewH)) continue;

            switch (h.kind) {
                case K_TAB:
                    if (btn == 0) { tab = h.cat; scroll = 0; }
                    break;
                case K_ROW:
                    if (btn == 0) h.mod.toggle();
                    else if (btn == 1) flip(h.mod);
                    break;
                case K_DOTS:
                    flip(h.mod);
                    break;
                case K_BIND_MOD:
                    if (btn == 0) bindMod = h.mod;
                    else if (btn == 1) h.mod.setKey(Keyboard.KEY_NONE);
                    break;
                case K_BIND_SET:
                    if (btn == 0) bindSet = h.set;
                    else if (btn == 1) h.set.setKey(Keyboard.KEY_NONE);
                    break;
                case K_BOOL:
                    if (btn == 0) h.set.toggle();
                    break;
                case K_SLIDER:
                case K_CHANNEL:
                    if (btn == 0) { drag = h; updateDrag(mx); }
                    break;
                default:
                    break;
            }
            return;
        }
    }

    private void flip(Module m) {
        if (!open.remove(m)) open.add(m);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        updateDrag(mx);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        drag = null;
        super.mouseReleased(mx, my, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d != 0) {
            scroll -= (d > 0 ? 1 : -1) * 24f;
            if (scroll > maxScroll) scroll = maxScroll;
            if (scroll < 0) scroll = 0;
        }
    }

    @Override
    protected void keyTyped(char c, int code) throws IOException {
        if (bindMod != null || bindSet != null) {
            if (code != Keyboard.KEY_RSHIFT) {
                boolean clear = code == Keyboard.KEY_ESCAPE || code == Keyboard.KEY_DELETE || code == Keyboard.KEY_BACK;
                int k = clear ? Keyboard.KEY_NONE : code;
                if (bindMod != null) bindMod.setKey(k); else bindSet.setKey(k);
            }
            bindMod = null; bindSet = null;
            return;
        }
        if (code == Keyboard.KEY_RSHIFT) { mc.displayGuiScreen(null); return; }
        super.keyTyped(c, code);
    }

    @Override
    public void onGuiClosed() {
        Config.save();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

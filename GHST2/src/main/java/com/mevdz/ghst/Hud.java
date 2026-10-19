package com.mevdz.ghst;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Sag ustteki bilgi paneli: baslik + acik modullerin listesi. */
public class Hud {
    private static final int BLUE = 0xFF1877F2;

    private static String label(Module m) {
        String info = m.getInfo();
        return m.getName() + (info.isEmpty() ? "" : " " + info);
    }

    public static void render() {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.showDebugInfo) return;
        final FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);

        String title = "\u00a7f\u00a7lMevdZ GHST \u00a7r\u00a7bbeta";

        List<Module> on = new ArrayList<>();
        for (Module m : ModuleManager.modules) if (m.isEnabled()) on.add(m);
        Collections.sort(on, new Comparator<Module>() {
            @Override
            public int compare(Module a, Module b) {
                return fr.getStringWidth(label(b)) - fr.getStringWidth(label(a));
            }
        });

        int pad = 5, lineH = 11;
        int w = fr.getStringWidth(title);
        for (Module m : on) w = Math.max(w, fr.getStringWidth(label(m)));
        w += pad * 2 + 2;
        int h = pad * 2 + 10 + (on.isEmpty() ? 0 : 4 + on.size() * lineH);

        int x = sr.getScaledWidth() - 4 - w;
        int y = 4;

        Gui.drawRect(x, y, x + w, y + h, 0x99101014);
        Gui.drawRect(x + w - 2, y, x + w, y + h, BLUE);
        fr.drawStringWithShadow(title, x + pad, y + pad, 0xFFFFFF);

        if (on.isEmpty()) return;
        Gui.drawRect(x + pad, y + pad + 11, x + w - pad - 2, y + pad + 12, 0x33FFFFFF);
        int ty = y + pad + 15;
        for (Module m : on) {
            String info = m.getInfo();
            String s = "\u00a7f" + m.getName() + (info.isEmpty() ? "" : " \u00a77" + info);
            fr.drawStringWithShadow(s, x + pad, ty, 0xFFFFFF);
            ty += lineH;
        }
    }
}

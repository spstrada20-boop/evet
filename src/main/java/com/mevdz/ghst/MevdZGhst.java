package com.mevdz.ghst;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "mevdzghst", name = "MevdZ GHST Beta", version = "1.0-beta")
public class MevdZGhst {

    public static final String NAME = "MevdZ GHST Beta";

    @EventHandler
    public void init(FMLInitializationEvent e) {
        ModuleManager.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        if (!Keyboard.getEventKeyState()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;
        int k = Keyboard.getEventKey();
        if (k == Keyboard.KEY_RSHIFT) {
            mc.displayGuiScreen(new ClickGui());
            return;
        }
        for (Module m : ModuleManager.modules)
            if (m.getKey() == k) m.toggle();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text e) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawStringWithShadow("\u00a7bMevdZ GHST \u00a7fBeta", 4, 4, 0xFFFFFF);
        fr.drawStringWithShadow("\u00a77RSHIFT = menu", 4, 14, 0xFFFFFF);
        int y = 28;
        for (Module m : ModuleManager.modules) {
            if (!m.isEnabled()) continue;
            fr.drawStringWithShadow(m.getName(), 4, y, 0x55FF55);
            y += 10;
        }
    }
}

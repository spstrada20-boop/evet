package com.mevdz.ghst;

import net.minecraft.client.Minecraft;
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
        Config.load();
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
        if (k == Keyboard.KEY_NONE) return;
        for (Module m : ModuleManager.modules) {
            if (m.getKey() == k) m.toggle();
            // ayarlara bagli tuslar (sadece ON/OFF ayarlar)
            for (Setting s : m.getSettings())
                if (s.type == Setting.Type.BOOL && s.getKey() == k) s.toggle();
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text e) {
        Hud.render();
    }
}

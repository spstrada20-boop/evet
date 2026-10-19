package com.mevdz.ghst.modules;

import com.mevdz.ghst.Category;
import com.mevdz.ghst.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class Fullbright extends Module {
    private float oldGamma = 1f;

    public Fullbright() { super("Fullbright", Category.VISUAL, Keyboard.KEY_G); }

    @Override
    public void onEnable() {
        super.onEnable();
        oldGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
        Minecraft.getMinecraft().gameSettings.gammaSetting = 100f;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().gameSettings.gammaSetting = oldGamma;
    }
}

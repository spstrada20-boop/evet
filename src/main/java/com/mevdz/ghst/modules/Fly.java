package com.mevdz.ghst.modules;

import com.mevdz.ghst.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {
    public Fly() { super("Fly", Keyboard.KEY_F); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        if (p != null) p.capabilities.allowFlying = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        if (p != null && !p.capabilities.isCreativeMode) {
            p.capabilities.allowFlying = false;
            p.capabilities.isFlying = false;
        }
    }
}

package com.mevdz.ghst.modules;

import com.mevdz.ghst.Category;
import com.mevdz.ghst.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ToggleSprint extends Module {
    public ToggleSprint() { super("ToggleSprint", Category.UTILITY, Keyboard.KEY_R); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        if (p == null) return;
        if (p.moveForward > 0 && !p.isSneaking()
                && !p.isCollidedHorizontally && p.getFoodStats().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
    }
}

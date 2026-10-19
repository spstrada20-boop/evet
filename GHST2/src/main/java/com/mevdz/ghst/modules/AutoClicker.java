package com.mevdz.ghst.modules;

import com.mevdz.ghst.Category;
import com.mevdz.ghst.Module;
import com.mevdz.ghst.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class AutoClicker extends Module {
    private final Setting cps        = new Setting("CPS", 10, 1, 20, 1);
    private final Setting skipBlocks = Setting.bool("SkipBlocks", true);
    private double acc;

    public AutoClicker() {
        super("AutoClicker", Category.COMBAT, Keyboard.KEY_K);
        addSetting(cps, skipBlocks);
    }

    @Override
    public String getInfo() { return cps.valueStr() + "cps"; }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) { acc = 0; return; }
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) { acc = 0; return; }
        if (skipBlocks.isOn() && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return;

        acc += cps.get() / 20.0;
        if (acc >= 1.0) {
            acc -= 1.0;
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
        }
    }
}

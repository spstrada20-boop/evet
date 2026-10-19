package com.mevdz.ghst.modules;

import com.mevdz.ghst.Category;
import com.mevdz.ghst.Module;
import com.mevdz.ghst.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class AimAssist extends Module {
    private final Setting range     = new Setting("Range", 4.0, 1.0, 6.0, 0.5);
    private final Setting speed     = new Setting("Speed", 8, 1, 30, 1);
    private final Setting fov       = new Setting("FOV", 90, 10, 360, 10);
    private final Setting clickOnly = Setting.bool("ClickOnly", true);
    private final Setting visible   = Setting.bool("VisibleOnly", true);

    public AimAssist() {
        super("AimAssist", Category.COMBAT, Keyboard.KEY_J);
        addSetting(range, speed, fov, clickOnly, visible);
    }

    @Override
    public String getInfo() { return "Range " + range.valueStr(); }

    private static float yawTo(EntityPlayerSP p, Entity t) {
        double dx = t.posX - p.posX, dz = t.posZ - p.posZ;
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    private static float pitchTo(EntityPlayerSP p, Entity t) {
        double dx = t.posX - p.posX, dz = t.posZ - p.posZ;
        double ty = (t.getEntityBoundingBox().minY + t.getEntityBoundingBox().maxY) / 2.0;
        double dy = ty - (p.posY + p.getEyeHeight());
        return (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null || mc.currentScreen != null) return;
        if (clickOnly.isOn() && !mc.gameSettings.keyBindAttack.isKeyDown()) return;

        Entity best = null;
        float bestDiff = Float.MAX_VALUE;
        for (Entity en : mc.theWorld.loadedEntityList) {
            if (!(en instanceof EntityLivingBase) || en == p || en instanceof EntityArmorStand) continue;
            EntityLivingBase t = (EntityLivingBase) en;
            if (t.isDead || t.getHealth() <= 0) continue;
            if (p.getDistanceToEntity(t) > range.get()) continue;
            if (visible.isOn() && !p.canEntityBeSeen(t)) continue;
            float yawDiff = Math.abs(MathHelper.wrapAngleTo180_float(yawTo(p, t) - p.rotationYaw));
            if (yawDiff > fov.get() / 2.0) continue;
            if (yawDiff < bestDiff) { bestDiff = yawDiff; best = t; }
        }
        if (best == null) return;

        float f = speed.getF() / 100f;
        float dYaw = MathHelper.wrapAngleTo180_float(yawTo(p, best) - p.rotationYaw);
        float dPitch = pitchTo(p, best) - p.rotationPitch;
        p.rotationYaw += dYaw * f;
        p.rotationPitch = MathHelper.clamp_float(p.rotationPitch + dPitch * f, -90f, 90f);
    }
}

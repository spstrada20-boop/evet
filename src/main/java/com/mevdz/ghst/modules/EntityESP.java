package com.mevdz.ghst.modules;

import com.mevdz.ghst.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class EntityESP extends Module {
    public EntityESP() { super("ESP", Keyboard.KEY_H); }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        float pt = e.partialTicks;
        Entity v = mc.thePlayer;
        double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
        double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
        double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GL11.glLineWidth(2f);

        for (Entity en : mc.theWorld.loadedEntityList) {
            if (!(en instanceof EntityLivingBase) || en == mc.thePlayer) continue;

            double x = en.lastTickPosX + (en.posX - en.lastTickPosX) * pt - vx;
            double y = en.lastTickPosY + (en.posY - en.lastTickPosY) * pt - vy;
            double z = en.lastTickPosZ + (en.posZ - en.lastTickPosZ) * pt - vz;

            AxisAlignedBB bb = en.getEntityBoundingBox();
            AxisAlignedBB box = new AxisAlignedBB(
                    bb.minX - en.posX + x, bb.minY - en.posY + y, bb.minZ - en.posZ + z,
                    bb.maxX - en.posX + x, bb.maxY - en.posY + y, bb.maxZ - en.posZ + z);

            RenderGlobal.drawOutlinedBoundingBox(box, 255, 60, 60, 255);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}

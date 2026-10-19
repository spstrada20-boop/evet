package com.mevdz.ghst.modules;

import com.mevdz.ghst.Category;
import com.mevdz.ghst.Module;
import com.mevdz.ghst.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class EntityESP extends Module {
    private final Setting color   = Setting.color("Color", 0xFF3C3C);
    private final Setting rainbow = Setting.bool("Rainbow", false);
    private final Setting players = Setting.bool("Players", true);
    private final Setting mobs    = Setting.bool("Mobs", true);
    private final Setting fill    = Setting.bool("Fill", false);
    private final Setting walls   = Setting.bool("ThroughWalls", true);
    private final Setting width   = new Setting("Width", 2.0, 1.0, 5.0, 0.5);

    public EntityESP() {
        super("ESP", Category.VISUAL, Keyboard.KEY_H);
        addSetting(color, rainbow, players, mobs, fill, walls, width);
    }

    @Override
    public String getInfo() { return rainbow.isOn() ? "Rainbow" : color.valueStr(); }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int rgb = rainbow.isOn()
                ? (Color.HSBtoRGB((System.currentTimeMillis() % 3000L) / 3000f, 0.8f, 1f) & 0xFFFFFF)
                : color.getColor();
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;

        float pt = e.partialTicks;
        Entity v = mc.thePlayer;
        double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
        double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
        double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (walls.isOn()) GlStateManager.disableDepth();
        GL11.glLineWidth(width.getF());

        for (Entity en : mc.theWorld.loadedEntityList) {
            if (!(en instanceof EntityLivingBase) || en == mc.thePlayer || en instanceof EntityArmorStand) continue;
            boolean isPlayer = en instanceof EntityPlayer;
            if (isPlayer && !players.isOn()) continue;
            if (!isPlayer && !mobs.isOn()) continue;

            double x = en.lastTickPosX + (en.posX - en.lastTickPosX) * pt - vx;
            double y = en.lastTickPosY + (en.posY - en.lastTickPosY) * pt - vy;
            double z = en.lastTickPosZ + (en.posZ - en.lastTickPosZ) * pt - vz;

            AxisAlignedBB bb = en.getEntityBoundingBox();
            AxisAlignedBB box = new AxisAlignedBB(
                    bb.minX - en.posX + x, bb.minY - en.posY + y, bb.minZ - en.posZ + z,
                    bb.maxX - en.posX + x, bb.maxY - en.posY + y, bb.maxZ - en.posZ + z);

            if (fill.isOn()) drawFilled(box, r / 255f, g / 255f, b / 255f, 0.25f);
            RenderGlobal.drawOutlinedBoundingBox(box, r, g, b, 255);
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawFilled(AxisAlignedBB bb, float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        // alt / ust
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        // on / arka
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        // sol / sag
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ); GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ); GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ); GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glEnd();
    }
}

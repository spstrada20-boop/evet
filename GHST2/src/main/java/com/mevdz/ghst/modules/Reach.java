package com.mevdz.ghst.modules;

import com.mevdz.ghst.Category;
import com.mevdz.ghst.Module;
import com.mevdz.ghst.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Random;

/**
 * Vurus menzilini uzatir. Her tick Min-Max arasinda rastgele bir menzil secilir.
 * Tek kisilik dunyada sunucu menzil siniri 6 blok (gorus hatti varsa), o yuzden slider 6'da biter.
 */
public class Reach extends Module {
    private final Setting min = new Setting("MinReach", 3.0, 3.0, 6.0, 0.1);
    private final Setting max = new Setting("MaxReach", 3.2, 3.0, 6.0, 0.1);
    private final Random rnd = new Random();

    public Reach() {
        super("Reach", Category.COMBAT, Keyboard.KEY_NONE);
        addSetting(min, max);
    }

    @Override
    public String getInfo() { return min.valueStr() + "-" + max.valueStr(); }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null || mc.currentScreen != null) return;

        // zaten normal menzilde bir varlik hedefteyse dokunma
        if (mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) return;

        double lo = Math.min(min.get(), max.get());
        double hi = Math.max(min.get(), max.get());
        double reach = lo + rnd.nextDouble() * (hi - lo);
        if (reach <= 3.0) return;

        Vec3 eyes = p.getPositionEyes(1.0F);
        Vec3 look = p.getLook(1.0F);
        double lx = look.xCoord * reach, ly = look.yCoord * reach, lz = look.zCoord * reach;
        Vec3 end = eyes.addVector(lx, ly, lz);

        List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                p, p.getEntityBoundingBox().addCoord(lx, ly, lz).expand(1.0, 1.0, 1.0));

        Entity best = null;
        Vec3 bestHit = null;
        double bestDist = reach;
        for (Entity en : list) {
            if (!(en instanceof EntityLivingBase) || en instanceof EntityArmorStand) continue;
            if (!en.canBeCollidedWith()) continue;
            float b = en.getCollisionBorderSize();
            AxisAlignedBB bb = en.getEntityBoundingBox().expand(b, b, b);
            MovingObjectPosition mop = bb.calculateIntercept(eyes, end);
            Vec3 hit;
            double d;
            if (bb.isVecInside(eyes)) { hit = eyes; d = 0; }
            else if (mop != null)     { hit = mop.hitVec; d = eyes.distanceTo(hit); }
            else continue;
            if (d < bestDist) { bestDist = d; best = en; bestHit = hit; }
        }
        if (best == null) return;

        // arada blok varsa vurma
        if (mc.theWorld.rayTraceBlocks(eyes, bestHit, false, true, false) != null) return;
        // sunucu 3 blokun otesinde gorus hatti ister
        if (bestDist > 2.9 && !p.canEntityBeSeen(best)) return;

        mc.objectMouseOver = new MovingObjectPosition(best, bestHit);
    }
}

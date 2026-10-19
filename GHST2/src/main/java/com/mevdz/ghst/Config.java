package com.mevdz.ghst;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Keybind ve ayarlari .minecraft/config/mevdzghst.properties dosyasina kaydeder. */
public class Config {

    private static File file() {
        return new File(Minecraft.getMinecraft().mcDataDir, "config/mevdzghst.properties");
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : ModuleManager.modules) {
            p.setProperty(m.getName() + ".key", String.valueOf(m.getKey()));
            for (Setting s : m.getSettings()) {
                p.setProperty(m.getName() + "." + s.name, s.serialize());
                if (s.type == Setting.Type.BOOL)
                    p.setProperty(m.getName() + "." + s.name + ".key", String.valueOf(s.getKey()));
            }
        }
        File f = file();
        try {
            f.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(f);
            try { p.store(out, "MevdZ GHST Beta"); } finally { out.close(); }
        } catch (IOException ignored) { }
    }

    public static void load() {
        File f = file();
        if (!f.exists()) return;
        Properties p = new Properties();
        try {
            FileInputStream in = new FileInputStream(f);
            try { p.load(in); } finally { in.close(); }
        } catch (IOException e) { return; }

        for (Module m : ModuleManager.modules) {
            m.setKey(readInt(p.getProperty(m.getName() + ".key"), m.getKey()));
            for (Setting s : m.getSettings()) {
                String v = p.getProperty(m.getName() + "." + s.name);
                if (v != null) s.deserialize(v);
                if (s.type == Setting.Type.BOOL)
                    s.setKey(readInt(p.getProperty(m.getName() + "." + s.name + ".key"), 0));
            }
        }
    }

    private static int readInt(String v, int def) {
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return def; }
    }
}

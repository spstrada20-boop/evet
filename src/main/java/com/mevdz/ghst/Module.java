package com.mevdz.ghst;

import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final int key;
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, int key) {
        this.name = name;
        this.key = key;
    }

    protected void addSetting(Setting... s) { for (Setting x : s) settings.add(x); }
    public List<Setting> getSettings() { return settings; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void onEnable()  { MinecraftForge.EVENT_BUS.register(this); }
    public void onDisable() { MinecraftForge.EVENT_BUS.unregister(this); }

    public String getName()    { return name; }
    public int getKey()        { return key; }
    public boolean isEnabled() { return enabled; }
}

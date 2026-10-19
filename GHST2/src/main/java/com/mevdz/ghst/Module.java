package com.mevdz.ghst;

import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final Category category;
    private int key;
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, Category category, int key) {
        this.name = name;
        this.category = category;
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

    /** HUD'da ismin yaninda gri yazan kisa bilgi (ornek: "10cps"). */
    public String getInfo() { return ""; }

    public String getName()      { return name; }
    public Category getCategory(){ return category; }
    public int getKey()          { return key; }
    public void setKey(int k)    { key = k; }
    public boolean isEnabled()   { return enabled; }
}

package com.mevdz.ghst;

import java.util.Locale;

public class Setting {
    public final String name;
    public final double min, max, step;
    public final boolean bool;
    private double value;

    public Setting(String name, double def, double min, double max, double step) {
        this.name = name; this.min = min; this.max = max; this.step = step; this.bool = false;
        set(def);
    }

    private Setting(String name, boolean def) {
        this.name = name; this.min = 0; this.max = 1; this.step = 1; this.bool = true;
        this.value = def ? 1 : 0;
    }

    public static Setting bool(String name, boolean def) { return new Setting(name, def); }

    public double get()     { return value; }
    public float getF()     { return (float) value; }
    public boolean isOn()   { return value >= 0.5; }
    public void toggle()    { value = isOn() ? 0 : 1; }

    public void set(double v) {
        v = Math.max(min, Math.min(max, v));
        if (step > 0) v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        if (bool) return name + ": " + (isOn() ? "ON" : "OFF");
        if (step >= 1) return name + ": " + (int) Math.round(value);
        return name + ": " + String.format(Locale.US, "%.1f", value);
    }
}
